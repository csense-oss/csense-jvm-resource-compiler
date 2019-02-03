package csense.javafx.resource.compiler.resource.handlers

import kotlinx.coroutines.Deferred
import java.nio.file.Path

interface IResourceHandler<T : BaseValidationResultItem> {

    /**
     * By returning true, you also accepts having receieved the file
     * @param file Path the file we are inspecting
     * @return Boolean true means we consumed it, false means we do not want it
     */
    fun acceptsFile(file: Path, fileName: String): Boolean

    fun onValidateAndCreateResult(): Deferred<ValidationSuccess<T>>

}

class ValidationSuccess<T : BaseValidationResultItem>(
    val item: List<T>
)

interface BaseValidationResultItem {
    val name: String
    val relativeLocationToRoot: String
}

interface BaseLocalizableValidationResultItem : BaseValidationResultItem {
    val namesToLookup: Set<String>
    val localizedLanguages: Set<String>
}