package csense.javafx.resource.compiler.resource.handlers

import csense.javafx.resource.compiler.kotlin.writer.*
import csense.javafx.resource.compiler.resource.handlers.base.*
import csense.kotlin.extensions.coroutines.*
import kotlinx.coroutines.*
import java.nio.file.*

class ImageHandler(val scope: CoroutineScope) : BaseMultiFileHandler<BaseValidationResultItem>() {

    //default supported by javafx (8)
    override val supportedFileTypes: List<String> = listOf(".png", ".jpg", ".bmp", ".gif", ".jpeg", ".mpo", ".jps")

    override fun onValidateAndCreateResult(resourceRoot: Path): Deferred<ValidationSuccess<BaseValidationResultItem>> = scope.asyncDefault {
        ValidationSuccess(files.toBaseValidationResultItems(resourceRoot))
    }

    override fun onCreateClassesFromValidation(items: ValidationSuccess<BaseValidationResultItem>): Deferred<List<KotlinResourceFile>> = scope.asyncDefault {
        listOf(KotlinResourceFile("Images",
                computeBlocks(items.item),
                listOf("java.net.*"),
                createSurroundingObject = true
        ))
    }

    private fun computeBlocks(item: List<BaseValidationResultItem>): List<KotlinBlock> = item.computeBlocks {
        KotlinBlock(it.name, listOf(
                it.createPathProperty(),
                it.createResourceUrlProperty("Images")
        ), listOf(
                it.createLoadAsStringFunction()
        ), listOf(), "")
    }


}
