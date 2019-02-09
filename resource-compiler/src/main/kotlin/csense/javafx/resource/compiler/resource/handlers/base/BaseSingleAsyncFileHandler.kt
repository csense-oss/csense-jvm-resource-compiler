package csense.javafx.resource.compiler.resource.handlers.base

import csense.javafx.resource.compiler.resource.handlers.*
import csense.kotlin.extensions.primitives.*
import kotlinx.coroutines.*
import java.nio.file.*

abstract class BaseSingleAsyncFileHandler<T : BaseValidationResultItem, FileContent> : IResourceHandler<T> {

    protected val files: MutableList<FileContent> = mutableListOf()

    abstract val supportedFileExtension: String

    abstract fun asyncLoadFile(file: Path): FileContent

    override fun acceptsFile(file: Path, fileName: String): Boolean =
            fileName.endsWith(supportedFileExtension, true).ifTrue {
                files += asyncLoadFile(file)
            }
}