package csense.javafx.resource.compiler.reader

import csense.javafx.resource.compiler.resource.handlers.IResourceHandler
import java.nio.file.Files
import java.nio.file.Path

class ProjectReader(
    private val resourceRoot: Path,
    val handlers: List<IResourceHandler<*>>
) {
    fun findFiles() = FileResourceReader(handlers).apply {
        Files.walkFileTree(resourceRoot, this)
    }
}