package csense.javafx.resource.compiler.resource.handlers

import csense.javafx.resource.compiler.kotlin.writer.*
import csense.javafx.resource.compiler.resource.handlers.base.*
import csense.kotlin.extensions.coroutines.*
import kotlinx.coroutines.*
import java.nio.file.*

class GenericHandler(val scope: CoroutineScope) : IResourceHandler<BaseValidationResultItem> {


    private val files: MutableList<Path> = mutableListOf()

    override fun acceptsFile(file: Path, fileName: String): Boolean {
        files.add(file)
        return true
    }

    override fun onValidateAndCreateResult(resourceRoot: Path): Deferred<ValidationSuccess<BaseValidationResultItem>> = scope.asyncDefault {
        ValidationSuccess(files.toBaseValidationResultItems(resourceRoot))
    }

    override fun onCreateClassesFromValidation(items: ValidationSuccess<BaseValidationResultItem>): Deferred<List<KotlinResourceFile>> = scope.asyncDefault {
        listOf(KotlinResourceFile("Resources",
                computeBlocks(items.item),
                listOf("java.net.*"),
                createSurroundingObject = true
        ))
    }


    private fun computeBlocks(item: List<BaseValidationResultItem>): List<KotlinBlock> = item.computeBlocks {
        KotlinBlock(it.name, listOf(
                it.createPathProperty(),
                it.createResourceUrlProperty("Resources")
        ), listOf(
                it.createLoadAsStringFunction()
        ), listOf(), "")
    }
}

