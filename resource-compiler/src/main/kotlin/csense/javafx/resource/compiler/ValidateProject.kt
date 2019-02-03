//package csense.javafx.resource.compiler
//
//import csense.javafx.resource.compiler.bll.PathWithLang
//import csense.javafx.resource.compiler.bll.getLanguageIdentifier
//import csense.kotlin.extensions.collections.map.useValueOr
//import java.nio.file.Path
//
///**
// *
// */
//class ValidateProject(private val projet: ResourceFileRecorder) {
//    /**
//     *
//     */
//    fun validate(): ValidationResult {
//        //todo use MeasureTimeResult from csense
//        val categorizedProperties = projet.propertyFiles.categorizeByLanguage()
//        println("- found languages : " + categorizedProperties.languages)
//        println("- found translation files " + categorizedProperties.categorized.size)
//        if (categorizedProperties.danglingTranslations.isNotEmpty()) {
//            println("- found dangling translations : \n" +
//                    categorizedProperties.danglingTranslations.joinToString("\n") {
//                        "\t\t" + it.realPath.toAbsolutePath().toString()
//                    })
//            return ValidationResult.ValidationFailed(categorizedProperties.danglingTranslations)
//        }
//        //TODO kinda do the same for all other resource types.
//        return ValidationResult.ValidationSucceded(categorizedProperties)
//    }
//}
//
//
//interface CategorizedResource {
//    val default: PathWithLang
//    val localized: Set<Path>
//}
//
//data class LanguageCategorization(
//    /**
//     * A summary of all different found languages.
//     */
//    val languages: Set<String>,
//    /**
//     * Those who have a base / default and potentially more translations.
//     */
//    val categorized: Set<CategorizedResource>,
//    /**
//     * Those who did not have a base / default translation.
//     */
//    val danglingTranslations: Set<PathWithLang>
//)
//
//data class MutableCategorizedResource(
//    override var default: PathWithLang,
//    override val localized: MutableSet<Path>
//) : CategorizedResource
//
//private fun Iterable<Path>.categorizeByLanguage(): LanguageCategorization {
//    val startCompute = System.currentTimeMillis()
//    val (roots, translations) = computeRootsAndTranslations()
//    val timeCompute = System.currentTimeMillis() - startCompute
//    println("\tTime to compute roots and translations = ${timeCompute}ms")
//    val danglingTranslations = mutableSetOf<PathWithLang>()
//
//    val resultingCategorization = mutableMapOf<Path, MutableCategorizedResource>()
//    val discoveredLanguages = mutableSetOf<String>()
//
//    roots.forEach {
//        if (resultingCategorization.containsKey(it.realPath)) {
//            println("MULTIPLE RESOURCES WITH SAME NORMALIZED ROOT PATH; AKK; DUPLICATED RESOURCES")
//            throw RuntimeException()
//        }
//        resultingCategorization[it.realPath] =
//            MutableCategorizedResource(it, mutableSetOf())
//    }
//
//    translations.forEach { pathWithLang ->
//        pathWithLang.language?.let { lang -> discoveredLanguages.add(lang) }
//
//        resultingCategorization.useValueOr(
//            pathWithLang.realPath,
//            { it.localized.add(pathWithLang.realPath) },
//            { danglingTranslations.add(pathWithLang) })
//    }
//
//
//    return LanguageCategorization(discoveredLanguages, resultingCategorization.values.toSet(), danglingTranslations)
//}
//
//
//data class RootsAndTranslations(
//    val roots: Set<PathWithLang>,
//    val translations: Set<PathWithLang>
//)
//
//fun Iterable<Path>.computeRootsAndTranslations(): RootsAndTranslations {
//    val roots = mutableSetOf<PathWithLang>()
//    val editions = mutableSetOf<PathWithLang>()
//
//    forEach {
//        println(it.fileName.toString())
//        val langId = it.getLanguageIdentifier(Languages.allIsoCodes)
//        if (langId.language != null) {
//            //sub lang of a potential root.
//            editions.add(langId)
//        } else {
//            roots.add(langId)
//            //new root
//        }
//    }
//    return RootsAndTranslations(roots, editions)
//}
//
//
