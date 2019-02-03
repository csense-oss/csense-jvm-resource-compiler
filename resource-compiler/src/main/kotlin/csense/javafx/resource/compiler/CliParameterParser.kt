package csense.javafx.resource.compiler

import csense.javafx.resource.compiler.kotlin.writer.KotlinAccessLevel
import java.nio.file.Path
import java.nio.file.Paths


data class ParsedArgs(
    val rootProject: Path,
    val sourceRoot: Path,
    val resourceRoot: Path,
    val packageName: String,
    val resourceName: String,
    val resourceAccessLevel: KotlinAccessLevel,
    val useCamelCase: Boolean
)

fun Array<String>.parseArgs(): ParsedArgs? {

    val roothPath = "../example-app"
    return ParsedArgs(
        Paths.get(roothPath),
        Paths.get("$roothPath/src/main/kotlin"),
        Paths.get("$roothPath/src/main/resources/"),
        "csense.example.app.generated",
        "resources.kt",
        KotlinAccessLevel.public,
        true
    )
//
//    if (size < 5) {
//        println("Error: $size arguments, is not enough. need at least 5")
//        printHowTo()
//        exitProcess(-1)
//    }
//    val path = this[0]
//    val mainKotlinSrc = this[1]
//    val mainResSrc = this[2]
//    val packageName = this[3]
//    val resourceName = this[4]
//    val usePublic = if (size >= 5) {
//        "public".equals(this[5], true)
//    } else {
//        true
//    }
//    val useCamelCase = if (size >= 6) {
//        this[6].toBoolean()
//    } else {
//        true
//    }
//
//    return ParsedArgs(
//        Paths.get("$path/"),
//        Paths.get("$path/$mainKotlinSrc"),
//        Paths.get("$path/$mainResSrc"),
//        packageName,
//        resourceName,
//        usePublic.map(KotlinAccessLevel.public, KotlinAccessLevel.internal),
//        useCamelCase
//    )
}

fun printHowTo() {
    println("------------------------")
    println("Format of parameters:")
    println("\t - 1 arg is the path to the project (relative to where this file is located, which is = \"${Paths.get("").toAbsolutePath()}\")")
    println("\t - 2 arg is the subpath to the desired kotlin source root (eg \"src/main/kotlin\")")
    println("\t - 3 arg is the subpath to the resource main folder (eg \"src/main/resources/\"")
    println("\t - 4 arg is the package name to use when generating the code (eg \"something.app.generated\")")
    println("\t - 5 arg is the file name to use for the generated code (Eg \"resources.kt\"), nb this will be overwritten each time")
    println("\t - 6 (opt) arg is whenever you want the resource to be generated public or internal (either \"public\" or \"internal\")")
    println("\t - 7 (opt) arg is if the naming scheme should be camelcase (\"true\"), or if it should be underscores instead (\"false\")")
    println("------------------------")
}

