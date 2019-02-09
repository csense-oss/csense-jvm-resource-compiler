package csense.javafx.resource.compiler.resource.handlers

import csense.javafx.resource.compiler.kotlin.writer.*
import csense.javafx.resource.compiler.resource.handlers.base.*
import csense.kotlin.extensions.coroutines.*
import kotlinx.coroutines.*
import java.nio.file.*

class CSSHandler(val scope: CoroutineScope) : BaseSingleFileHandler<BaseValidationResultItem>() {

    override val supportedFileExtension: String = ".css"
    //TODO consider databinding the classes and selectors ? hmm for another handler ofc.

    override fun onValidateAndCreateResult(resourceRoot: Path): Deferred<ValidationSuccess<BaseValidationResultItem>> = scope.asyncDefault {
        ValidationSuccess(files.toBaseValidationResultItems(resourceRoot))
    }

    override fun onCreateClassesFromValidation(items: ValidationSuccess<BaseValidationResultItem>): Deferred<List<KotlinResourceFile>> = scope.asyncDefault {
        listOf(KotlinResourceFile("CSS",
                computeBlocks(items.item),
                listOf("java.net.*"),
                createSurroundingObject = true
        ))
    }

    private fun computeBlocks(item: List<BaseValidationResultItem>): List<KotlinBlock> = item.computeBlocks {
        KotlinBlock(it.name, listOf(
                it.createPathProperty(),
                it.createResourceUrlProperty("CSS")
        ), listOf(
                it.createLoadAsStringFunction()
        ), listOf(), "")
    }

}

class CssHandlerFailed : Exception()
