package csense.javafx.resource.compiler

import csense.javafx.resource.compiler.reader.*
import csense.kotlin.extensions.toPrettyString
import csense.kotlin.logger.L
import csense.kotlin.logger.LoggingFunctionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.system.measureTimeMillis


/**
 *
 */
suspend fun main(args: Array<String>): Unit = coroutineScope {
    //    readStdInLine()
//    L.usePrintAsLoggers { level: LoggingLevel, tag: String, message: String, error: Throwable? ->
//        "${level.wrapInAsciiColor()} - [$tag] : $message ${error?.toPrettyString("\n") ?: ""}"
//    }
    L.usePrintAsLoggers()
//    L.isLoggingAllowed(false)
//    L.isProductionLoggingAllowed = true
    launch(Dispatchers.IO) { Languages.allIsoCodes }//preloads the all iso codes, in a pretty faster manner. (-70ms in total execution time)
    val totalTime = measureTimeMillis {
        val parsed: ParsedArgs = args.parseArgs() ?: return@coroutineScope
        val mainFlow = MainFlow(this)
        val readAllFilesTime = measureTimeMillis {
            ProjectReader(parsed.resourceRoot, mainFlow.allInAList).findFiles()
        }
        L.debug("Main", "Discovering all file took ${readAllFilesTime}ms")

        mainFlow.computeAll(parsed, this)

    }
    L.logProd("Main", "Finished in ${totalTime}ms")
}


fun readStdInLine() {
    val br = BufferedReader(InputStreamReader(System.`in`))
    print("Enter String")
    br.readLine()
}


/**
 *
 */
typealias FunctionLoggerFormatter = (level: LoggingLevel, tag: String, message: String, error: Throwable?) -> String


//TOOD waiting for Csense
/**
 * This will add a logger to each category using the stdout (console).
 * @receiver L
 */
fun L.usePrintAsLoggers(
        formatter: FunctionLoggerFormatter = { level: LoggingLevel, tag: String, message: String, exception: Throwable? ->
            "$level - [$tag] $message ${exception?.toPrettyString()}"
        }
) {

    val debug: LoggingFunctionType<Any> = { tag: String, message: String, exception: Throwable? ->
        println(formatter(LoggingLevel.Debug, tag, message, exception))
    }
    val warning: LoggingFunctionType<Any> = { tag: String, message: String, exception: Throwable? ->
        println(formatter(LoggingLevel.Warning, tag, message, exception))
    }
    val error: LoggingFunctionType<Any> = { tag: String, message: String, exception: Throwable? ->
        println(formatter(LoggingLevel.Error, tag, message, exception))
    }
    val prod: LoggingFunctionType<Any> = { tag: String, message: String, exception: Throwable? ->
        println(formatter(LoggingLevel.Production, tag, message, exception))
    }
    debugLoggers.add(debug)
    warningLoggers.add(warning)
    errorLoggers.add(error)
    productionLoggers.add(prod)
}

/**
 * A simple enumeration over the types of logging that can happen.
 * @property stringValue String the textual representation (useful for tags)
 */
enum class LoggingLevel(val stringValue: String) {
    Debug("Debug"),
    Warning("Warning"),
    Error("Error"),
    Production("Production");

    /**
     * Gets the string representation.
     * @return String
     */
    override fun toString(): String {
        return stringValue
    }
}

/**
 * Wraps the level in some colors :)
 * @receiver LoggingLevel
 * @return String
 */
fun LoggingLevel.wrapInAsciiColor(): String {
    val color = when (this) {
        LoggingLevel.Debug -> "96m"
        LoggingLevel.Warning -> "93m"
        LoggingLevel.Error -> "91m"
        LoggingLevel.Production -> "92m"
    }
    return "\u001B[$color$stringValue\u001B[0m"
}
