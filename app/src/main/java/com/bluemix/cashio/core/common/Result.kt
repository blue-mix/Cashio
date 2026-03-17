package com.bluemix.cashio.core.common

import kotlinx.coroutines.CancellationException

/**
 * A discriminated union representing the outcome of an operation.
 *
 * ## No Loading state
 * [Loading] has been intentionally removed. Loading is UI state — it belongs as a
 * `Boolean` field inside the screen's `UiState` data class, not as a [Result] variant
 * that every `when` expression must handle. Mixing UI state and result state in one
 * type forces every non-UI layer (use cases, repositories) to be aware of it.
 *
 * ## Usage
 * ```kotlin
 * when (val result = getExpensesUseCase()) {
 *     is Result.Success -> render(result.data)
 *     is Result.Error   -> showError(result.message)
 * }
 * ```
 */
sealed class Result<out T> {

    data class Success<out T>(val data: T) : Result<T>()

    data class Error(
        val exception: Exception,
        val message: String? = exception.message
    ) : Result<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error

    /** Returns data if [Success], or null otherwise. */
    fun getOrNull(): T? = (this as? Success)?.data

    /**
     * Transforms a [Success] value using [transform].
     *
     * Any exception thrown by [transform] is caught and returned as [Error],
     * so callers never receive an uncaught exception from a map operation.
     *
     * [CancellationException] is always re-thrown — never swallowed — to
     * preserve structured concurrency semantics.
     */
    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> try {
            Success(transform(data))
        } catch (e: CancellationException) {
            throw e     // Must propagate — never catch CancellationException
        } catch (e: Exception) {
            Error(e, e.message)
        }

        is Error -> this
    }

    /**
     * Chains a suspending operation that itself returns [Result].
     * Short-circuits on [Error].
     */
    suspend fun <R> flatMap(transform: suspend (T) -> Result<R>): Result<R> = when (this) {
        is Success -> try {
            transform(data)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Error(e, e.message)
        }

        is Error -> this
    }

    /** Executes [action] if this is [Success], then returns this unchanged. */
    fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    /** Executes [action] if this is [Error], then returns this unchanged. */
    fun onError(action: (Error) -> Unit): Result<T> {
        if (this is Error) action(this)
        return this
    }
}

/**
 * Wraps a suspending [block] in a [Result].
 *
 * [CancellationException] is **always re-thrown** so coroutine cancellation
 * propagates correctly through structured concurrency. Swallowing it would
 * cause coroutines to hang indefinitely after cancellation.
 */
suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: CancellationException) {
    throw e     // Never swallow — propagate cancellation
} catch (e: Exception) {
    Result.Error(e, e.message)
}