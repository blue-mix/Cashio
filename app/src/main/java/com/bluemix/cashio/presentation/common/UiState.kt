package com.bluemix.cashio.presentation.common

/**
 * Generic UI state wrapper
 */
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

/**
 * Extension to check state
 */
val UiState<*>.isLoading: Boolean get() = this is UiState.Loading
val UiState<*>.isSuccess: Boolean get() = this is UiState.Success
val UiState<*>.isError: Boolean get() = this is UiState.Error
val UiState<*>.isIdle: Boolean get() = this is UiState.Idle

/**
 * Get data or null
 */
fun <T> UiState<T>.getOrNull(): T? = when (this) {
    is UiState.Success -> data
    else -> null
}
