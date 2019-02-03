//package csense.javafx.resource.compiler.writer
//
//import csense.javafx.resource.compiler.*
//import csense.javafx.resource.compiler.bll.fileNameWithoutExtension
//import csense.javafx.resource.compiler.bll.propertyNamesStrings
//import csense.javafx.resource.compiler.bll.toPropertyName
//import csense.javafx.resource.compiler.kotlin.writer.KotlinAccessLevel
//import csense.javafx.resource.compiler.reader.ResourceFileRecorder
//import csense.kotlin.extensions.map
//import csense.kotlin.extensions.primitives.removeFileExtension
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.async
//import kotlinx.coroutines.runBlocking
//import org.intellij.lang.annotations.Language
//import java.io.IOException
//import java.nio.file.Files
//import java.nio.file.Path
//import java.nio.file.Paths
//import java.util.*
//
//class ProjectWriter(
//    val validation: ValidationResult.ValidationSucceded,
//    val project: ResourceFileRecorder,
//    val parsed: ParsedArgs
//) {
//    fun writeToProjectFile(): WriteResult = runBlocking() {
//        val fileToWriteAsync = async() {
//            val fileLocation =
//                Paths.get(
//                    parsed.sourceRoot.toAbsolutePath().toString(),
//                    *parsed.packageName.split(".").toTypedArray()
//                )
//            if (!Files.exists(fileLocation)) {
//                Files.createDirectories(fileLocation)
//            }
//            fileLocation.resolve(parsed.resourceName).toFile()
//        }
//
//        val mapNameValue = mutableMapOf<PathWithLang, MutableSet<String>>()
//
//
//
//        validation.categorizedProperties.categorized.forEach { resource: CategorizedResource ->
//            //read properties file
//            val prop = project.loadedProperties[resource.default.realPath.toAbsolutePath()]?.await()
//                ?: throw Exception("Could not find previously discovered properties file")
////            val prop = resource.default.realPath.loadProperties()
//            //and compute the map
//            prop.propertyNamesStrings().forEach { propertyName ->
//                mapNameValue.getOrPut(resource.default, ::mutableSetOf)
//                    .add(propertyName)
//            }
//        }
//
//        //todo further validate missing translations and other kind of wierd missings / overlaps
//        //this would also allow us to write staticstics and inline comments like "translated to ..."
//        // and or missing translations for languages da
//
//
//        //create file
//        val fileContentAsync = async() {
//            createFileContent(mapNameValue) + "\n\n" + SmartResourceBundleEncoded.encodedClass
//        }
//
//        fileToWriteAsync.await().writeText(fileContentAsync.await())
//        return@runBlocking WriteResult.Succeded()
//    }
//
//    fun createFileContent(mapNameValue: MutableMap<PathWithLang, MutableSet<String>>): String {
//        return createObjectHeader("Strings", parsed.packageName, parsed.resourceAccessLevel) + "    " +
//                mapNameValue.map {
//                    createInnerContent(it)
//                }.joinToString("\n    ") + "\n" + createObjectEnding() + "\n"
//    }
//
//    fun createInnerContent(propertyMapping: Map.Entry<PathWithLang, MutableSet<String>>): String {
//        return createInnerObjectHeader(propertyMapping.key.normalizedRootPath.fileNameWithoutExtension()) + "        " +
//                createBundlePropertyInInnerObject(propertyMapping.key.realPath) +
//                propertyMapping.value.joinToString("\n        ") { resourceRawName: String ->
//                    createStringResourceProperty(
//                        resourceRawName.toPropertyName(parsed.useCamelCase),
//                        resourceRawName
//                    )
//                } +
//                changeLocalFunction +
//                supportLanguagesFunctions +
//                "\n    " + createObjectEnding()
//    }
//
//    fun createBundlePropertyInInnerObject(bundlePath: Path): String {
//
//        val bundlePathString = bundlePath.toAbsolutePath().toString()
//        val removed = bundlePathString.removePrefix(parsed.resourceRoot.toAbsolutePath().toString())
//        val normalizedStart = removed.removePrefix("\\").replace('\\', '/')
//        val realNameWithPackageName = normalizedStart.removeFileExtension()
//
//        return "private val bundle: SmartResourceBundle by lazy {\n            SmartResourceBundle(\n" +
//                "                \"$realNameWithPackageName\",\n                " +
//                "setOf(\n" + "                    " +
//                validation.categorizedProperties.languages.joinToString(",\n                    ") { "\"$it\"" } +
//                "\n                )\n            )\n        }\n        "
//    }
//
//    fun createInnerObjectHeader(name: String): String = "object $name {\n"
//
//    fun createObjectHeader(name: String, packageName: String, accessLevel: KotlinAccessLevel): String {
//        val ignores = "@file:Suppress(\"unused\", \"ClassName\", \"KDocMissingDocumentation\")\n\n"
//        val accessLevelToInsert = (accessLevel == KotlinAccessLevel.public).map("", "internal ")
//        val imports = "\nimport java.util.*\n\n"
//        return ignores +
//                "package $packageName\n" +
//                imports +
//                "${accessLevelToInsert}object $name {\n"
//    }
//
//
//    fun createStringResourceProperty(name: String, rawKey: String): String {
//        return "val $name: String \n            get () = bundle.getString(\"$rawKey\")\n"
//    }
//
//    fun createObjectEnding(): String = "}"
//
//    @Language("kotlin")
//    private val changeLocalFunction: String =
//        "\n        /**\n         * the local of the bundle (the resource loader)\n         */\n        var locale: Locale\n            get() = bundle.currentLocale\n            set(value) = bundle.changeLocale(value)\n"
//
//    @Language("kotlin")
//    private val supportLanguagesFunctions: String =
//        "\n        val supportedLanguages: Set<String>\n            get() = bundle.localLanguages\n"
//}
//
//
//
