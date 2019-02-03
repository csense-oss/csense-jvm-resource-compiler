package csense.javafx.resource.compiler.resource.handlers

import csense.javafx.resource.compiler.Languages
import csense.javafx.resource.compiler.bll.fileNameWithoutExtension
import csense.javafx.resource.compiler.bll.fileNameWithoutLanguage
import csense.javafx.resource.compiler.bll.loadProperties
import csense.javafx.resource.compiler.bll.onLanguageIdentifier
import csense.javafx.resource.compiler.mapAsyncAwait
import csense.javafx.resource.compiler.properties.PropertyReader
import csense.javafx.resource.compiler.setIfNotEmpty
import csense.kotlin.extensions.collections.map.useValueOr
import csense.kotlin.extensions.collections.set.symmetricDifference
import csense.kotlin.extensions.coroutines.asyncDefault
import csense.kotlin.extensions.coroutines.asyncIO
import csense.kotlin.extensions.measureTimeMillisResult
import csense.kotlin.logger.L
import csense.kotlin.logger.logClassDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import java.nio.file.Path

class PropertiesHandler(
    val coroutineScope: CoroutineScope
) : IResourceHandler<PropertyHandlerSuccessItem> {
    private val files: MutableList<Pair<Path, Deferred<PropertyReader>>> = mutableListOf()

    override fun acceptsFile(file: Path, fileName: String): Boolean {
        if (!fileName.endsWith(".properties")) {
            return false
        }
        val absPath = file.toAbsolutePath()
        files.add(Pair(absPath, coroutineScope.asyncIO {
            @Suppress("BlockingMethodInNonBlockingContext") //is in IO context
            absPath.loadProperties()
        }))
        return true
    }

    override fun onValidateAndCreateResult(): Deferred<ValidationSuccess<PropertyHandlerSuccessItem>> =
        coroutineScope.asyncDefault {
            val (timeForBundles, bundles) = measureTimeMillisResult { computeBundles() }
            L.debug("onValidateAndCreateResult", "Time for bundles: $timeForBundles ms")
            val (timeForAllSuccess, loadedAll) = measureTimeMillisResult { bundles.computeItemsSuccessAsync() }
            L.debug("onValidateAndCreateResult", "Time for converting all bundles: $timeForAllSuccess ms")
            return@asyncDefault ValidationSuccess(loadedAll)
        }

    private suspend fun List<PropertyBundle>.computeItemsSuccessAsync(): List<PropertyHandlerSuccessItem> =
        mapAsyncAwait(coroutineScope) { it.computeSuccessItem() }

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

    private suspend fun PropertyBundle.awaitAllFiles(): Pair<Set<String>, List<TranslationsForLanguage>> {
        val (subTimeInMs: Long, subFiles: List<TranslationsForLanguage>) = measureTimeMillisResult {
            subLanguages.mapAsyncAwait(coroutineScope, Dispatchers.Default) {
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
    private suspend fun PropertyBundle.computeSuccessItem(): PropertyHandlerSuccessItem {
        val (mainFile, subFiles) = awaitAllFiles()

        val (missingTranslationsInMain, missingInSubLanguageTranslations) =
            computeMissingTranslations(mainFile, subFiles)

        if (missingTranslationsInMain.isNotEmpty()) {
            throw PropertyHandlerFailed(missingTranslationsInMain)
        }

        return PropertyHandlerSuccessItem(
            main.path.fileNameWithoutExtension(),
            ""/*main.path.relativize(resourceRoot).toString()*/,
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
        val mapped = subFiles.mapAsyncAwait(coroutineScope, Dispatchers.Default) {
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
    override val name: String,
    override val relativeLocationToRoot: String,
    override val namesToLookup: Set<String>,
    override val localizedLanguages: Set<String>,
    val missingTranslationsInLanguages: Map<String, Set<String>>
) : BaseLocalizableValidationResultItem


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