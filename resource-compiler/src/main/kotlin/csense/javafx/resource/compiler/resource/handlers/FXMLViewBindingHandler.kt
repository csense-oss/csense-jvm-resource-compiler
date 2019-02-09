package csense.javafx.resource.compiler.resource.handlers

import csense.javafx.resource.compiler.bll.fileNameWithoutExtension
import csense.javafx.resource.compiler.bll.toPropertyName
import csense.javafx.resource.compiler.kotlin.writer.KotlinBlock
import csense.javafx.resource.compiler.kotlin.writer.KotlinResourceFile
import csense.javafx.resource.compiler.resource.handlers.base.*
import csense.kotlin.extensions.coroutines.asyncDefault
import csense.kotlin.extensions.coroutines.asyncIO
import csense.kotlin.extensions.generic.Generic
import csense.kotlin.extensions.generic.filter
import csense.kotlin.extensions.generic.forEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

class FXMLViewBindingHandler(val scope: CoroutineScope) : BaseSingleAsyncFileHandler<BaseValidationResultItem, Pair<Path, Deferred<String>>>() {

    override fun onCreateClassesFromValidation(items: ValidationSuccess<BaseValidationResultItem>): Deferred<List<KotlinResourceFile>> = scope.asyncDefault {
        files.map {
            val bindingName = it.first.fileNameWithoutExtension().toPropertyName(true) + "Binding"
            val xml = it.second.await()
            val (rootType, bindingProperties) = tryParseAsXmlAndCreateBindingProperties(xml)
            val propertiesString: String = "\nval root: $rootType\n " + bindingProperties.map { binding: Map.Entry<String, String> ->
                "\n\t\t val ${binding.key}: ${binding.value}"
            }.joinToString("\n")
            val initString: String = "\n init {\n" +
                    "        root = FXMLLoader.load(resourceUri)" +
                    bindingProperties.map { binding: Map.Entry<String, String> ->
                        "\n${binding.key} = root.lookup(\"#${binding.key}\") as ${binding.value}"
                    }.joinToString("\n") + "\n}"
            val companionObjectString = "\ncompanion object{ \n" +
                    "val resourcePath: String = \"${it.first.fileName}\"\n" +
                    "val resourceUri = $bindingName::class.java.classLoader.getResource(resourcePath) \n" +
                    "}"
            KotlinResourceFile(bindingName,
                    listOf(KotlinBlock(
                            bindingName,
                            listOf(),
                            listOf(),
                            listOf(),
                            propertiesString + initString + companionObjectString,
                            false
                    )),
                    listOf("javafx.fxml.*", "javafx.scene.control.*", "javafx.scene.layout.*"),
                    "bindings",
                    false
            )

        }

    }

    override val supportedFileExtension: String = ".fxml"

    override fun asyncLoadFile(file: Path): Pair<Path, Deferred<String>> = Pair(file, scope.asyncIO {
        @Suppress("BlockingMethodInNonBlockingContext")
        Files.readAllBytes(file).toString(Charsets.UTF_8)
    })

    override fun onValidateAndCreateResult(resourceRoot: Path): Deferred<ValidationSuccess<BaseValidationResultItem>> = scope.asyncDefault {


        //        tryParseAsXmlAndCreateBindingClass()

        ValidationSuccess(files.map { it.first }.toBaseValidationResultItems(resourceRoot))
    }

    private fun tryParseAsXmlAndCreateBindingProperties(xml: String): Pair<String, Map<String, String>> {
        return xml.byteInputStream().use { stream ->
            val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
            val idToTypes = computeBinding(xmlDoc)
            val root = computeRootType(xmlDoc)
            return@use Pair(root, idToTypes)
//            val crudeKotlinBinding = "class ${it.first.fileNameWithoutExtension().toPropertyName(true)} { \n " +
//                    idToTypes.map { idToType ->
//                        "val ${idToType.key}: ${idToType.value} \n get() = scene.lookup(\"#${idToType.key}\")"
//                    }.joinToString("\n") +
//                    "\nval root: $root = ????" + "\n" + "}"
//            println("crude kotlin binding class = \n$crudeKotlinBinding")

        }
    }

    private fun computeRootType(xmlDoc: Document): String {
        return xmlDoc.documentElement.nodeName ?: ""
    }

    /**
     * Computes a map of id name and type.
     *
     * @param xmlDoc Document
     * @return Map<String, String>
     */
    private fun computeBinding(xmlDoc: Document): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val nodeList = xmlDoc.getElementsByTagName("*")
        Generic.filter(nodeList.length, nodeList::item, 0) { node -> node.nodeType == Node.ELEMENT_NODE }.forEach { node ->
            Generic.forEach(node.attributes.length, node.attributes::item, 0) { attr ->
                if (attr.nodeName?.equals("fx:id", true) == true) {
                    map[attr.nodeValue] = node.nodeName
                }
            }
        }
        return map
    }

    //TODO improve this.
    private fun computeBlocks(item: List<BaseValidationResultItem>): List<KotlinBlock> = item.computeBlocks {
        KotlinBlock(it.name, listOf(
                it.createPathProperty(),
                it.createResourceUrlProperty("FXML")
        ), listOf(
                it.createLoadAsStringFunction()
        ), listOf(), "")
    }

}

