package csense.javafx.resource.compiler.resource.handlers

import csense.javafx.resource.compiler.kotlin.writer.*
import csense.javafx.resource.compiler.resource.handlers.base.*
import csense.kotlin.extensions.coroutines.*
import kotlinx.coroutines.*
import java.nio.file.*


class FXMLHandler(val scope: CoroutineScope) : BaseSingleAsyncFileHandler<BaseValidationResultItem, Pair<Path, Deferred<String>>>() {

    override fun onCreateClassesFromValidation(items: ValidationSuccess<BaseValidationResultItem>): Deferred<List<KotlinResourceFile>> = scope.asyncDefault {
        listOf(KotlinResourceFile("FXML",
                computeBlocks(items.item),
                listOf("java.net.*"),
                createSurroundingObject = true
        ))
    }

    override val supportedFileExtension: String = ".fxml"

    override fun asyncLoadFile(file: Path): Pair<Path, Deferred<String>> = Pair(file, scope.asyncIO {
        @Suppress("BlockingMethodInNonBlockingContext")
        Files.readAllBytes(file).toString(Charsets.UTF_8)
    })

    override fun onValidateAndCreateResult(resourceRoot: Path): Deferred<ValidationSuccess<BaseValidationResultItem>> = scope.asyncDefault {
        ValidationSuccess(files.map { it.first }.toBaseValidationResultItems(resourceRoot))
    }

    private fun computeBlocks(item: List<BaseValidationResultItem>): List<KotlinBlock> = item.computeBlocks {
        KotlinBlock(it.name, listOf(
                it.createPathProperty(),
                it.createResourceUrlProperty("FXML")
        ), listOf(
                it.createLoadAsStringFunction()
        ), listOf(), "")
    }

}

