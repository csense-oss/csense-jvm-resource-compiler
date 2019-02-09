package csense.javafx.resource.compiler.resource.handlers.base

import csense.javafx.resource.compiler.resource.handlers.*
import csense.kotlin.extensions.primitives.*
import java.nio.file.*

abstract class BaseSingleFileHandler<T : BaseValidationResultItem> : IResourceHandler<T> {

    protected val files: MutableList<Path> = mutableListOf()

    protected abstract val supportedFileExtension: String

    override fun acceptsFile(
            file: Path,
            fileName: String
    ): Boolean = fileName.endsWith(supportedFileExtension, true).ifTrue {
        files.add(file)
    }

}