package csense.javafx.resource.compiler.reader

import csense.javafx.resource.compiler.bll.SafeAlwaysFileVistor
import csense.javafx.resource.compiler.resource.handlers.IResourceHandler
import csense.kotlin.logger.L
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

class FileResourceReader(
        val handlers: List<IResourceHandler<*>>
) : SafeAlwaysFileVistor<Path>() {


    override fun onFile(file: Path, attrs: BasicFileAttributes) {
        val fileName = file.fileName.toString()
        val gotHandled = handlers.any { it.acceptsFile(file, fileName) }
        if (!gotHandled) {
            L.warning(FileResourceReader::class.java.simpleName, "failed to handle file $fileName")
            //warn about this.
        }
    }


}

