package csense.javafx.resource.compiler.resource.handlers.base

import csense.javafx.resource.compiler.resource.handlers.*
import csense.kotlin.extensions.primitives.*
import java.nio.file.*

abstract class BaseMultiFileHandler<T : BaseValidationResultItem> : IResourceHandler<T> {

    protected abstract val supportedFileTypes: List<String>

    protected val files: MutableList<Path> = mutableListOf()

    override fun acceptsFile(
            file: Path,
            fileName: String
    ): Boolean = fileName.endsWithAny(supportedFileTypes, false).ifTrue {
        files.add(file)
    }


}