package org.wordpress.android.util.coroutines

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit.MILLISECONDS
import kotlin.coroutines.Continuation

suspend inline fun <T> suspendCoroutineWithTimeout(
    timeout: Long,
    crossinline block: (Continuation<T>) -> Unit
) = withTimeoutOrNull(timeout, MILLISECONDS) {
    suspendCancellableCoroutine(block = block)
}
