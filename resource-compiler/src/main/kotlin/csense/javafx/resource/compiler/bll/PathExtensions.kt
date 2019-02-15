package csense.javafx.resource.compiler.bll

import csense.javafx.resource.compiler.resource.handlers.*
import csense.kotlin.extensions.primitives.*
import java.io.*
import java.nio.file.*
import java.util.*

fun Path.fileNameWithoutExtension(): String = fileName.toString().removeFileExtension()

/**
 *
 * @receiver Path
 * @return Properties
 * @throws IOException
 */
@Throws(IOException::class)
fun Path.loadProperties(): Properties {
    val result = Properties()
    this.toFile().inputStream().use { result.load(it) }
    return result
}


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

/**
 * Computes the filename with the "_$language"
 * @receiver PropertyLoadingItemLanguage
 * @return String
 */
fun PropertyLoadingItemLanguage.fileNameWithoutLanguage(): String {
    return fileName.substring(0, fileName.length - (language.length + 1))
}

fun Path.computeRelativeWithNoFileName(subPath: Path): String {
    val withFilename = relativize(subPath).toString().replace("\\", "/")
    val lastPart = withFilename.lastIndexOf("/")
    return if (lastPart < 0) {
        ""
    } else {
        withFilename.substring(0, lastPart + 1) //to include the "/"
    }
}