package csense.javafx.resource.compiler.bll

import java.nio.file.FileVisitResult
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

abstract class SafeAlwaysFileVistor<T> : SafeSimpleFileVisitor<T>() {
    override fun visitFileSafe(file: T, attrs: BasicFileAttributes): FileVisitResult {
        onFile(file, attrs)
        return FileVisitResult.CONTINUE
    }

    abstract fun onFile(file: T, attrs: BasicFileAttributes)
}

abstract class SafeSimpleFileVisitor<T> : SimpleFileVisitor<T>() {
    override fun visitFile(file: T?, attrs: BasicFileAttributes?): FileVisitResult {
        if (file == null || attrs == null) {
            return FileVisitResult.CONTINUE
        }
        return visitFileSafe(file, attrs)
    }

    abstract fun visitFileSafe(file: T, attrs: BasicFileAttributes): FileVisitResult
}