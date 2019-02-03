package csense.javafx.resource.compiler

import java.util.*

object Languages {
    @JvmStatic
    val allIsoCodes: Set<String> = Locale.getISOLanguages().toHashSet()
}

