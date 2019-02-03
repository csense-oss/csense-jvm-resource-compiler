//package csense.javafx.resource.compiler.resource.handlers
//
//import java.nio.file.Path
//
//class CSSHandler : IResourceHandler {
//
//    //We could potentially parse the css files and actually calculate som more usable things, but for now just keep it simple
//    private val cssFiles: MutableList<Path> = mutableListOf()
//
//    override fun acceptsFile(file: Path): Boolean {
//        if (!file.endsWith(".css")) {
//            return false
//        }
//        cssFiles.add(file.toAbsolutePath())
//        return true
//    }
//
//    override fun onValidateAndCreateResult(): BaseValidationResult {
//        return CssHandlerFailed()
//    }
//
//}
//
//class CssHandlerSuccess(items: List<CssHandlerSuccessItem>) : BaseValidationSuccess<CssHandlerSuccessItem>(items)
//
//class CssHandlerFailed : BaseValidationFailed()
//
//class CssHandlerSuccessItem : BaseValidationResultItem {
//    override val name: String
//        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
//    override val relativeLocationToRoot: String
//        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
//
//}
