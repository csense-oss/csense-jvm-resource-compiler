package csense.javafx.resource.compiler.kotlin.writer

import csense.javafx.resource.compiler.*
import csense.javafx.resource.compiler.bll.*
import csense.kotlin.extensions.*
import csense.kotlin.extensions.coroutines.*
import kotlinx.coroutines.*
import java.nio.file.*

enum class KotlinAccessLevel(
        val stringValue: String
) {
    Public("public"),
    Internal("internal")
}


data class KotlinProperty(
        val name: String,
        val type: String,
        val accessLevel: KotlinAccessLevel,
        val isGetterProperty: Boolean,
        val code: String
)

data class KotlinBlock(
        val name: String,
        val properties: List<KotlinProperty>,
        val functions: List<KotlinFunction>,
        val innerBlock: List<KotlinBlock>,
        val rawStart: String,
        val isObject: Boolean = true
)

data class KotlinFunction(
        val name: String,
        val returnType: String,
        val parameters: String,
        val code: String,
        val accessLevel: KotlinAccessLevel
)


data class KotlinResourceFile(
        val name: String,
        val kotlinBlocks: List<KotlinBlock>,
        val imports: List<String>,
        val optSubNameSpace: String? = null,
        val createSurroundingObject: Boolean = true)


object KotlinWriter {

    /**
     * Produces the nessary file(s) in the target location(s)
     */
    fun createFileFrom(args: ParsedArgs,
                       scope: CoroutineScope,
                       header: String,
                       vararg classes: List<KotlinResourceFile>): List<Deferred<Unit>> = classes.map { rootClasses: List<KotlinResourceFile> ->
        scope.asyncDefault {
            val builder = StringBuilder()
            rootClasses.forEach {
                val resultingString = builder.append(
                        createPackage(args.packageName),
                        createImports(it.imports),
                        createObject(it.name, it.createSurroundingObject, args.resourceAccessLevel),
                        it.kotlinBlocks.joinToString("\n") { block: KotlinBlock -> block.toKotlin(indentation = 0) },
                        it.createSurroundingObject.map("\n}", "")).toString()
                createFile(args, resultingString, it.name + ".kt")
                builder.clear()
            }
        }
    }

    private fun createImports(imports: List<String>): String = imports.joinToString("") { "import $it\n" }


    private fun createFile(parsed: ParsedArgs, text: String, fileName: String) {
        Files.write(Paths.get(
                parsed.sourceRoot.toAbsolutePath().toString(),
                parsed.packageName.replace(".", "/"),
                fileName
        ), text.toByteArray(Charsets.UTF_8))
    }

    private fun createPackage(packageName: String): String {
        return "\npackage $packageName\n"
    }

    private fun createObject(name: String, createSurroundingObject: Boolean, accessLevel: KotlinAccessLevel): String {
        if (!createSurroundingObject) {
            return ""
        }
        val access = (accessLevel != KotlinAccessLevel.Public).map(accessLevel.stringValue, "")
        return "$access${createObject(name, true)}"
    }

    private fun createObject(name: String, isObject: Boolean): String {
        val type = isObject.map("object", "class")
        return "$type ${name.toPropertyName(true)} {"
    }

    private fun createGetterProperty(name: String, type: String, value: String, isGetter: Boolean, indentation: Int): String {
        return createNewLineWithIndentation(indentation) + "val $name: $type " +
                if (isGetter) {
                    createNewLineWithIndentation(indentation + 1) + " get () = $value"
                } else {
                    "= $value"
                }
    }

    private fun KotlinBlock.toKotlin(indentation: Int): String {
        val ourIndentation = indentation + 1
        val builder = StringBuilder()
        val newLineWithIndentation = createNewLineWithIndentation(ourIndentation)

        builder.append(newLineWithIndentation, createObject(name, isObject))

        builder.append(this.rawStart.replace("\n", createNewLineWithIndentation(ourIndentation + 1)))


        builder.append(this.properties.joinToString("\n") {
            createGetterProperty(it.name, it.type, it.code, it.isGetterProperty, ourIndentation + 1)
        })

        innerBlock.forEach {
            builder.append(it.toKotlin(indentation + 1))
        }

        functions.forEach {
            builder.append(it.createFunction(indentation + 2))
        }

        builder.append(newLineWithIndentation, "}")
        return builder.toString()
    }

    private fun KotlinFunction.createFunction(indentation: Int): String {
        val indentationText = createNewLineWithIndentation(indentation)
        val nextLevelIndentation = createNewLineWithIndentation(indentation + 1)
        val funcDecl = "fun $name($parameters): $returnType {$nextLevelIndentation"
        val codeIndented = code.replace("\n", nextLevelIndentation)
        @Suppress("ConvertToStringTemplate")
        return indentationText + funcDecl + codeIndented + indentationText + "}"
    }

    private fun createNewLineWithIndentation(indentation: Int): String {
        return "\n" + createIndentation(indentation)
    }

    private fun createIndentation(indentation: Int): String {
        return " ".repeat(indentation * 4)
    }

}

