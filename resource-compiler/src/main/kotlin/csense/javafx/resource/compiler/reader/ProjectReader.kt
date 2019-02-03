package csense.javafx.resource.compiler.reader

import csense.javafx.resource.compiler.FileResourceReader
import csense.javafx.resource.compiler.bll.SafeAlwaysFileVistor
import csense.javafx.resource.compiler.bll.loadProperties
import csense.javafx.resource.compiler.resource.handlers.IResourceHandler
import csense.kotlin.extensions.coroutines.asyncIO
import csense.kotlin.extensions.coroutines.launchIO
import csense.kotlin.logger.L
import kotlinx.coroutines.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque

class ProjectReader(
    private val resourceRoot: Path,
    val handlers: List<IResourceHandler<*>>
) {
    fun findFiles() = FileResourceReader(handlers).apply {
        Files.walkFileTree(resourceRoot, this)
    }
}