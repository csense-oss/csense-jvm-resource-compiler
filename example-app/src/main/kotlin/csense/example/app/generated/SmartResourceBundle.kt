package csense.example.app.generated


import java.util.*

/**
 * A smarter resource bundle reader, that knows exactly what files are where, and does not need to do any guessing.
 * smart and fast and quite safer. :)
 * @property mainPath String
 * @property localLanguages List<(kotlin.String..kotlin.String?)>
 * @property mainProperties Properties
 * @property currentProperties Properties
 * @constructor
 */
class SmartResourceBundle(
        private val mainPath: String,
        val localLanguages: Set<String>
) {
    var currentLocale: Locale = Locale.getDefault()

    private var mainProperties: Properties = loadPropertyFile(null)

    private var currentProperties: Properties = updateLocalProperty(currentLocale)
    /**
     *
     * @param key String
     * @return String
     */
    fun getString(key: String): String {
        return currentProperties.getProperty(key)
                ?: mainProperties.getProperty(key)
                ?: key //default to key iff not found.
    }

    /**
     *
     * @param newLocale Locale
     */
    fun changeLocale(newLocale: Locale = Locale.getDefault()) {
        currentLocale = newLocale
        currentProperties = updateLocalProperty(newLocale)
    }

    private fun updateLocalProperty(newLocale: Locale): Properties {
        return if (localLanguages.contains(newLocale.language)) {
            loadPropertyFile(newLocale.language)
        } else {
            //does not have language, so will default to main bundle.
            Properties()
        }
    }

    /**
     *
     * @param locale String?
     * @return Properties
     */
    private fun loadPropertyFile(locale: String?): Properties {
        val suffix = if (locale != null) {
            "_$locale"
        } else {
            ""
        }
        val filename = "$mainPath$suffix.properties"
        return Properties().apply {
            val resource = SmartResourceBundle::class.java.classLoader.getResourceAsStream(filename)
                    ?: throw Exception("Could not load resouce with name: $filename")
            resource.use(::load)
        }
    }
}