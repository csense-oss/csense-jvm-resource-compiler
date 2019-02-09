package csense.javafx.resource.compiler.resource.handlers

import csense.javafx.resource.compiler.kotlin.writer.*
import csense.javafx.resource.compiler.resource.handlers.base.*
import csense.kotlin.extensions.coroutines.*
import kotlinx.coroutines.*
import java.nio.file.*

class FontHandler(val scope: CoroutineScope) : BaseMultiFileHandler<BaseValidationResultItem>() {
    /*TODO support font types (-light, -bold)*/
    override val supportedFileTypes = listOf(".otf", ".ttf")

    override fun onValidateAndCreateResult(resourceRoot: Path): Deferred<ValidationSuccess<BaseValidationResultItem>> = scope.asyncDefault {
        ValidationSuccess(files.toBaseValidationResultItems(resourceRoot))
    }

    override fun onCreateClassesFromValidation(items: ValidationSuccess<BaseValidationResultItem>): Deferred<List<KotlinResourceFile>> = scope.asyncDefault {
        listOf(KotlinResourceFile("FONTS",
                computeBlocks(items.item),
                listOf("java.net.*"),
                createSurroundingObject = true
        ))
    }

    private fun computeBlocks(item: List<BaseValidationResultItem>): List<KotlinBlock> = item.computeBlocks {
        KotlinBlock(it.name, listOf(
                it.createPathProperty(),
                it.createResourceUrlProperty("FONTS")
        ), listOf(
        ), listOf(), "")
    }
}

class FontHandlerFailure : Exception()

