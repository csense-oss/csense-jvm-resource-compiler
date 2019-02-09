package csense.javafx.resource.compiler.resource.handlers

import csense.javafx.resource.compiler.kotlin.writer.*
import kotlinx.coroutines.Deferred
import java.nio.file.Path

interface IResourceHandler<T : BaseValidationResultItem> {

    /**
     * By returning true, you also accepts having receieved the file
     * @param file Path the file we are inspecting
     * @return Boolean true means we consumed it, false means we do not want it
     */
    fun acceptsFile(file: Path, fileName: String): Boolean

    fun onValidateAndCreateResult(resourceRoot: Path): Deferred<ValidationSuccess<T>>

    fun onCreateClassesFromValidation(items: ValidationSuccess<T>): Deferred<List<KotlinResourceFile>>


}

class ValidationSuccess<T : BaseValidationResultItem>(
        val item: List<T>
)

open class BaseValidationResultItem(
        val name: String,
        val relativeLocationToRoot: String,
        val extension: String
)

open class BaseLocalizableValidationResultItem(
        name: String,
        relativeLocationToRoot: String,
        extension: String,
        val namesToLookup: Set<String>,
        val localizedLanguages: Set<String>
) : BaseValidationResultItem(name, relativeLocationToRoot, extension)