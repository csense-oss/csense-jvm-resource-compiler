package csense.javafx.resource.compiler.resource.handlers

import csense.javafx.resource.compiler.*
import csense.javafx.resource.compiler.bll.*
import csense.javafx.resource.compiler.kotlin.writer.*
import csense.javafx.resource.compiler.properties.*
import csense.javafx.resource.compiler.resource.handlers.base.*
import csense.javafx.resource.compiler.setIfNotEmpty
import csense.kotlin.extensions.*
import csense.kotlin.extensions.collections.map.useValueOr
import csense.kotlin.extensions.collections.set.*
import csense.kotlin.extensions.coroutines.*
import csense.kotlin.logger.*
import kotlinx.coroutines.*
import java.nio.file.*
import kotlin.collections.set

typealias  PropertyLoaderType = Pair<Path, Deferred<PropertyReader>>

class PropertiesHandler(
        val scope: CoroutineScope
) : BaseSingleAsyncFileHandler<PropertyHandlerSuccessItem, PropertyLoaderType>() {


    override val supportedFileExtension: String = ".properties"

    override fun asyncLoadFile(file: Path): PropertyLoaderType {
        val absPath = file.toAbsolutePath()
        return Pair(absPath, scope.asyncIO {
            @Suppress("BlockingMethodInNonBlockingContext") //is in IO context
            absPath.loadProperties()
        })
    }

    override fun onValidateAndCreateResult(resourceRoot: Path): Deferred<ValidationSuccess<PropertyHandlerSuccessItem>> =
            scope.asyncDefault {
                val (timeForBundles, bundles) = measureTimeMillisResult { computeBundles() }
                L.debug("onValidateAndCreateResult", "Time for bundles: $timeForBundles ms")
                val (timeForAllSuccess, loadedAll) = measureTimeMillisResult { bundles.computeItemsSuccessAsync(resourceRoot) }
                L.debug("onValidateAndCreateResult", "Time for converting all bundles: $timeForAllSuccess ms")
                return@asyncDefault ValidationSuccess(loadedAll)
            }

    private suspend fun List<PropertyBundle>.computeItemsSuccessAsync(resourceRoot: Path): List<PropertyHandlerSuccessItem> =
            mapAsyncAwait(scope) { it.computeSuccessItem(resourceRoot) }

    //file level. creates "bundles" for all file types , so
    // Main => main_da, main_de ,  (is a bundle)
    // SomeOther => SomeOther_da, SomeOther_en (is another bundle)
    private fun computeBundles(): List<PropertyBundle> {
        val subLangs = mutableListOf<PropertyLoadingItemLanguage>()
        val roots = mutableMapOf<String, RootComputation>()
        val allLangs = Languages.allIsoCodes
        files.forEach { (pathToFile, file) ->
            pathToFile.onLanguageIdentifier(
                    allLangs,
                    onMainLanguage = { path ->
                        roots[path.fileNameWithoutExtension()] = RootComputation(path, file, mutableListOf())
                    },
                    onSubLanguage = { path: Path, language: String, fileName: String ->
                        subLangs.add(PropertyLoadingItemLanguage(path, fileName, file, language))
                    })
        }

        subLangs.forEach {
            val root = it.fileNameWithoutLanguage()
            roots.useValueOr(root, { computation ->
                computation.subLangs.add(it)
            }, {
                throw Exception("Failed to find root for property file ${it.path.toAbsolutePath()}")
            })
        }
        return roots.values.map { value ->
            PropertyBundle(
                    PropertyLoadingItem(value.path, value.deferredLoading),
                    value.subLangs
            )
        }
    }


    override fun onCreateClassesFromValidation(items: ValidationSuccess<PropertyHandlerSuccessItem>): Deferred<List<KotlinResourceFile>> = scope.asyncDefault {
        items.item.map { successItem ->
            KotlinResourceFile(
                    "Strings",
                    listOf(
                            KotlinBlock(
                                    successItem.name,
                                    successItem.namesToLookup.map {
                                        KotlinProperty(it.toPropertyName(true), "String", KotlinAccessLevel.Public, true, "bundle.getString(\"$it\")")
                                    },
                                    listOf(),
                                    listOf(),
                                    "\nprivate val bundle:SmartResourceBundle = SmartResourceBundle(" +
                                            "\"${successItem.relativeLocationToRoot}${successItem.name}\"," +
                                            "setOf(\"${successItem.localizedLanguages.joinToString("\",\"")}\")" +
                                            ")\n"

                            ))
                    , listOf(), createSurroundingObject = true
            )
        }
    }


    private suspend fun PropertyBundle.awaitAllFiles(): Pair<Set<String>, List<TranslationsForLanguage>> {
        val (subTimeInMs: Long, subFiles: List<TranslationsForLanguage>) = measureTimeMillisResult {
            subLanguages.mapAsyncAwait(scope, Dispatchers.Default) {
                TranslationsForLanguage(it.deferredLoading.await().data.keys, it.language)
            }
        }
        logClassDebug("Time for loading all sub lang properties files: ${subTimeInMs}ms")
        val mainFile: Set<String> = main.deferredLoading.await().data.keys
        return Pair(mainFile, subFiles)
    }

    /**
     * Validation, and succcess computation.
     * @receiver PropertyBundle
     */
    private suspend fun PropertyBundle.computeSuccessItem(resourceRoot: Path): PropertyHandlerSuccessItem {
        val (mainFile, subFiles) = awaitAllFiles()

        val (missingTranslationsInMain, missingInSubLanguageTranslations) =
                computeMissingTranslations(mainFile, subFiles)

        if (missingTranslationsInMain.isNotEmpty()) {
            throw PropertyHandlerFailed(missingTranslationsInMain)
        }

        return PropertyHandlerSuccessItem(
                main.path.fileNameWithoutExtension(),
                resourceRoot.toAbsolutePath().computeRelativeWithNoFileName(main.path.toAbsolutePath()),
                main.path.toFile().extension,
                mainFile,
                subFiles.computeLanguages(),
                missingInSubLanguageTranslations
        )
    }

    private suspend fun computeMissingTranslations(
            mainFile: Set<String>,
            subFiles: List<TranslationsForLanguage>
    ): Pair<List<String>, Map<String, Set<String>>> {
        val missingTranslationsInMain = mutableListOf<String>()
        val missingInSubLanguageTranslations = mutableMapOf<String, Set<String>>()
        val mapped = subFiles.mapAsyncAwait(scope, Dispatchers.Default) {
            Pair(it, mainFile.symmetricDifference(it.translations))
        }
        //combine results.
        mapped.forEach { (language: TranslationsForLanguage, missings: Pair<Set<String>, Set<String>>) ->
            val (missingInSubLang: Set<String>, missingInMain: Set<String>) = missings
            missingTranslationsInMain.addAll(missingInMain)
            missingInSubLanguageTranslations.setIfNotEmpty(language.language, missingInSubLang)
        }
        return Pair(missingTranslationsInMain, missingInSubLanguageTranslations)
    }
}

data class TranslationsForLanguage(
        val translations: Set<String>,
        val language: String
)

data class RootComputation(
        val path: Path,
        val deferredLoading: Deferred<PropertyReader>,
        val subLangs: MutableList<PropertyLoadingItemLanguage>
)


data class PropertyLoadingItem(val path: Path, val deferredLoading: Deferred<PropertyReader>)

data class PropertyLoadingItemLanguage(
        val path: Path,
        val fileName: String,
        val deferredLoading: Deferred<PropertyReader>,
        val language: String
)


data class PropertyBundle(val main: PropertyLoadingItem, val subLanguages: List<PropertyLoadingItemLanguage>)
/**
 * The result(s) of validating and building a result of this handler
 */


class PropertyHandlerSuccessItem(
        name: String,
        relativeLocationToRoot: String,
        extension: String,
        namesToLookup: Set<String>,
        localizedLanguages: Set<String>,
        val missingTranslationsInLanguages: Map<String, Set<String>>
) : BaseLocalizableValidationResultItem(name, relativeLocationToRoot, extension, namesToLookup, localizedLanguages)


/**
 * Models a fatal issue with the given string(s) / properties files.
 * @property failedFiles List<Path>
 * @property missingDefaultTranslations List<PropertyString>
 * @constructor
 */
class PropertyHandlerFailed(
        val missingInMain: List<String>
) : Exception()

//TODO use from csense when available (0.0.14)
fun List<TranslationsForLanguage>.computeLanguages(): Set<String> = mapTo(mutableSetOf()) { it.language }


private fun ValidationSuccess<PropertyHandlerSuccessItem>.printWarnings() {
    item.forEach {
        if (it.missingTranslationsInLanguages.isNotEmpty()) {
            L.error(
                    "Translations",
                    "found missing translations in language files; ${it.missingTranslationsInLanguages}"
            )
        }
    }
}
