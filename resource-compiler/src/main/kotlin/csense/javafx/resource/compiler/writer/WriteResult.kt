package csense.javafx.resource.compiler.writer

sealed class WriteResult {
    class Succeded() : WriteResult()
    class Failed() : WriteResult()
}
