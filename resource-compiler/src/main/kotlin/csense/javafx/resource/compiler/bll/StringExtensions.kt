package csense.javafx.resource.compiler.bll

/**
 * Converts a name to a "property" like name.
 * either via "_" or camelcase
 */
fun String.toPropertyName(shouldUseCamelCase: Boolean): String {

    //settings ? like use _ instead of camel casing ?
    if (!shouldUseCamelCase) {
        return replace(".", "_")
    }
    var resultingName = this
    val charsToReplace = listOf(".", "_")
    charsToReplace.forEach {
        var index = resultingName.indexOf(it)
        while (index >= 0 && index + 1 < resultingName.length) {
            val char = resultingName[index + 1].toUpperCase()
            resultingName = resultingName.replaceRange(index, index + 2, char.toString())
            index = resultingName.indexOf(it)
        }
    }
    return resultingName
    //camelcase
}