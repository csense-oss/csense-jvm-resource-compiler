package csense.javafx.resource.compiler

import csense.kotlin.AsyncFunction1
import csense.kotlin.EmptyFunctionResult
import csense.kotlin.Function0
import csense.kotlin.extensions.measureTimeMillisResult
import csense.kotlin.logger.L
import csense.kotlin.logger.LoggingFunctionType
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

///**
// *
// * Be cautious when using this, it requires some hefty timing in the mapper to make sense.. as it might take some time to start all the coroutines
// * and the overhead of coroutines can easily add up
// * @receiver Iterable<T>
// * @param coroutineScope CoroutineScope
// * @param context CoroutineContext
// * @param mapper Function1<T, U>
// * @return List<Deferred<U>>
// */
//fun <T, U> Iterable<T>.mapAsync(
//        coroutineScope: CoroutineScope,
//        context: CoroutineContext = Dispatchers.Default,
//        mapper: AsyncFunction1<T, U>
//): List<Deferred<U>> = map {
//    coroutineScope.async(context) {
//        mapper(it)
//    }
//}
//
///**
// *
// * @receiver Iterable<T>
// * @param coroutineScope CoroutineScope
// * @param context CoroutineContext
// * @param mapper Function1<T, U>
// * @return List<U>
// */
//suspend fun <T, U> Iterable<T>.mapAsyncAwait(
//        coroutineScope: CoroutineScope,
//        context: CoroutineContext = Dispatchers.Default,
//        mapper: AsyncFunction1<T, U>
//): List<U> = this.mapAsync(coroutineScope, context, mapper).awaitAll()

/**
 * Conveinces for setting subLists iff they are not empty.
 * @receiver MutableMap<K, V>
 * @param key K
 * @param value V
 */
fun <K, V : Iterable<*>> MutableMap<K, V>.setIfNotEmpty(key: K, value: V) {
    if (value.any()) {
        this[key] = value
    }
}

//TODO potential csense ?

inline fun <T> logTimeInMillis(
        loggingTitle: String = "Timing",
        loggingMethod: LoggingFunctionType<Unit> = L::debug,
        action: EmptyFunctionResult<T>
): T {
    val (time, result) = measureTimeMillisResult { action() }
    loggingMethod(loggingTitle, "${time}ms", null)
    return result
}