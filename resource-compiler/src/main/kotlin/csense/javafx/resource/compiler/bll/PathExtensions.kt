package csense.javafx.resource.compiler.bll

import csense.javafx.resource.compiler.properties.PropertyReader
import csense.javafx.resource.compiler.resource.handlers.PropertyLoadingItemLanguage
import csense.kotlin.Function1
import csense.kotlin.extensions.measureTimeMillisResult
import csense.kotlin.extensions.primitives.removeFileExtension
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

fun Path.fileNameWithoutExtension(): String = fileName.toString().removeFileExtension()

/**
 *
 * @receiver Path
 * @return Properties
 * @throws IOException
 */
@Throws(IOException::class)
fun Path.loadProperties(): PropertyReader = PropertyReader.read(this)


inline fun Path.onLanguageIdentifier(
    allLanguages: Set<String>,
    crossinline onMainLanguage: (path: Path) -> Unit,
    crossinline onSubLanguage: (path: Path, language: String, fileName: String) -> Unit
) {
    val name = fileName.toString().removeFileExtension()
    val lastUnderscore = name.lastIndexOf('_')
    if (lastUnderscore < 0 || lastUnderscore + 1 == name.length) { //if no "_" or if its the last char.
        onMainLanguage(this)
        return
    }
    val subPart = name.substring(lastUnderscore + 1)
    val isFound = allLanguages.contains(subPart)
    if (!isFound) {
        onMainLanguage(this)
        return
    } else {
        onSubLanguage(this, subPart, name)
    }
}


data class PathWithLang(
    val realPath: Path,
    val language: String?
)

/**
 * Computes the filename with the "_$language"
 * @receiver PropertyLoadingItemLanguage
 * @return String
 */
fun PropertyLoadingItemLanguage.fileNameWithoutLanguage(): String {
    return fileName.substring(0, fileName.length - (language.length + 1))
}