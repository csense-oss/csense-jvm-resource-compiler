@file:Suppress("unused")

package csense.javafx.resource.compiler.writer

import org.intellij.lang.annotations.Language


object SmartResourceBundleEncoded {

    /**
     *
     */
    @Language("kotlin")
    const val encodedClass: String =
        "/**\n * A smarter resource bundle reader, that knows exactly what files are where, and does not need to do any guessing.\n * smart and fast and quite safer. :)\n * @property mainPath String\n * @property localLanguages List<(kotlin.String..kotlin.String?)>\n * @property mainProperties Properties\n * @property currentProperties Properties\n * @constructor\n */\nclass SmartResourceBundle(\n    private val mainPath: String,\n    val localLanguages: Set<String>\n) {\n    var currentLocale: Locale = Locale.getDefault()\n\n    private var mainProperties: Properties = loadPropertyFile(null)\n\n    private var currentProperties: Properties = updateLocalProperty(currentLocale)\n    /**\n     *\n     * @param key String\n     * @return String\n     */\n    fun getString(key: String): String {\n        return currentProperties.getProperty(key)\n            ?: mainProperties.getProperty(key)\n            ?: key //default to key iff not found.\n    }\n\n    /**\n     *\n     * @param newLocale Locale\n     */\n    fun changeLocale(newLocale: Locale = Locale.getDefault()) {\n        currentLocale = newLocale\n        currentProperties = updateLocalProperty(newLocale)\n    }\n\n    private fun updateLocalProperty(newLocale: Locale): Properties {\n        return if (localLanguages.contains(newLocale.language)) {\n            loadPropertyFile(newLocale.language)\n        } else {\n            //does not have language, so will default to main bundle.\n            Properties()\n        }\n    }\n\n    /**\n     *\n     * @param locale String?\n     * @return Properties\n     */\n    private fun loadPropertyFile(locale: String?): Properties {\n        val suffix = if (locale != null) {\n            \"_\$locale\"\n        } else {\n            \"\"\n        }\n        val filename = \"\$mainPath\$suffix.properties\"\n        return Properties().apply {\n            val resource = SmartResourceBundle::class.java.classLoader.getResourceAsStream(filename)\n                ?: throw Exception(\"Could not load resouce with name: filename\")\n            resource.use(::load)\n        }\n    }\n}"
}