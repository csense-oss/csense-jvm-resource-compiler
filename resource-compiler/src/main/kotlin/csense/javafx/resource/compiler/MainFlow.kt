package csense.javafx.resource.compiler

import csense.javafx.resource.compiler.kotlin.writer.*
import csense.javafx.resource.compiler.resource.handlers.*
import csense.kotlin.*
import csense.kotlin.extensions.*
import csense.kotlin.extensions.collections.*
import csense.kotlin.extensions.coroutines.*
import csense.kotlin.logger.*
import kotlinx.coroutines.*
import java.nio.file.*


class MainFlow(scope: CoroutineScope) {
    val properties: PropertiesHandler = PropertiesHandler(scope)
    val fxml: FXMLViewBindingHandler = FXMLViewBindingHandler(scope)
    val css: CSSHandler = CSSHandler(scope)
    val font: FontHandler = FontHandler(scope)
    val images: ImageHandler = ImageHandler(scope)
    val generic: GenericHandler = GenericHandler(scope)
    val allInAList: List<IResourceHandler<*>> = listOf(properties, fxml, css, font, images, generic)


    suspend fun computeAll(parsed: ParsedArgs, scope: CoroutineScope) {
        val propertyResults = properties.onValidateAndCreateResult(parsed.resourceRoot)
        val fxmlResults = fxml.onValidateAndCreateResult(parsed.resourceRoot)
        val cssResults = css.onValidateAndCreateResult(parsed.resourceRoot)
        val fontResults = font.onValidateAndCreateResult(parsed.resourceRoot)
        val imagesResult = images.onValidateAndCreateResult(parsed.resourceRoot)
        val genericResults = generic.onValidateAndCreateResult(parsed.resourceRoot)

        val propertyClassesAsync = logTimeInMillis("Property results") { properties.onCreateClassesFromValidation(propertyResults.await()) }
        val fxmlClassesAsync = logTimeInMillis("FXML results") { fxml.onCreateClassesFromValidation(fxmlResults.await()) }
        val cssClassesAsync = logTimeInMillis("CSS results") { css.onCreateClassesFromValidation(cssResults.await()) }
        val fontClassesAsync = logTimeInMillis("Font results") { font.onCreateClassesFromValidation(fontResults.await()) }
        val imageClassesAsync = logTimeInMillis("Image results") { images.onCreateClassesFromValidation(imagesResult.await()) }
        val genericClassesAsync = logTimeInMillis("Generic results") { generic.onCreateClassesFromValidation(genericResults.await()) }


        KotlinWriter.createFileFrom(parsed, scope, "",
                propertyClassesAsync.await(),
                fxmlClassesAsync.await(),
                cssClassesAsync.await(),
                fontClassesAsync.await(),
                imageClassesAsync.await(),
                genericClassesAsync.await()
        ).awaitAll()
    }
}
