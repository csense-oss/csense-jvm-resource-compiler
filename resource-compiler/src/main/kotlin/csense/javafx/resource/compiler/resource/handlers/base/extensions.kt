package csense.javafx.resource.compiler.resource.handlers.base

import csense.javafx.resource.compiler.bll.*
import csense.javafx.resource.compiler.datastructures.*
import csense.javafx.resource.compiler.kotlin.writer.*
import csense.javafx.resource.compiler.resource.handlers.*
import java.nio.file.*

fun BaseValidationResultItem.createPathProperty(): KotlinProperty =
        KotlinProperty("path", "String", KotlinAccessLevel.Public, false, "\"$relativeLocationToRoot$name.$extension\"")


fun BaseValidationResultItem.createResourceUrlProperty(className: String): KotlinProperty =
        KotlinProperty("resourceUrl", "URL", KotlinAccessLevel.Public, false, "$className::class.java.classLoader.getResource(path)")

fun BaseValidationResultItem.createLoadAsStringFunction(): KotlinFunction =
        KotlinFunction("loadAsString", "String", "", "return resourceUrl.readText()", KotlinAccessLevel.Public)


fun List<Path>.toBaseValidationResultItems(resourceRoot: Path): List<BaseValidationResultItem> = map {
    BaseValidationResultItem(it.fileName.fileNameWithoutExtension(), resourceRoot.computeRelativeWithNoFileName(it), it.toFile().extension)
}


fun List<BaseValidationResultItem>.computeBlocks(onEachItem: Function1<BaseValidationResultItem, KotlinBlock>): List<KotlinBlock> {
    val pathToBlock = PathCombiner<KotlinBlock>()

    forEach {
        val block = onEachItem(it)
        val path = it.relativeLocationToRoot.split("/")
        pathToBlock.add(path, block)
    }

    return pathToBlock.createStrucuturedResult { it: PathCombinerItem<KotlinBlock>, subList: List<KotlinBlock> ->
        KotlinBlock(it.pathPart, listOf(), listOf(), subList, "")
    }
}