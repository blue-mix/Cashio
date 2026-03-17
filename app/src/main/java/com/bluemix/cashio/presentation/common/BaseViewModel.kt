package com.bluemix.cashio.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Common UI state wrapper used by every screen.
 *
 * [isLoading] is a boolean here — not a [Result.Loading] variant — so the
 * use case and repository layers never need to know about loading state.
 */
data class UiMessage(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isError: Boolean = false
)

/**
 * Base ViewModel that provides:
 * - [launch] with automatic error capture into a [UiMessage].
 * - [launchCancellable] that cancels any prior in-flight job before starting
 *   a new one — used in search, date-range switching, etc.
 *
 * Subclasses hold their own [MutableStateFlow] of screen-specific UiState.
 * Error messages and loading state live inside that per-screen state rather
 * than in this base class, keeping state cohesive and avoiding hidden coupling.
 */
abstract class BaseViewModel : ViewModel() {

    /**
     * Launches [block] in [viewModelScope].
     *
     * On [Result.Error], calls [onError] with the failure message.
     * [onError] defaults to a no-op — override per screen.
     */
    protected fun <T> launchResult(
        onError: (String?) -> Unit = {},
        block: suspend () -> Result<T>
    ): Job = viewModelScope.launch {
        when (val result = block()) {
            is Result.Error -> onError(result.message)
            is Result.Success -> Unit
        }
    }

    /**
     * Cancels [priorJob] before starting a new coroutine.
     * Use for operations where only the latest result matters
     * (date range changes, chart period switches, search queries).
     */
    protected fun launchCancelling(priorJob: Job?, block: suspend () -> Unit): Job {
        priorJob?.cancel()
        return viewModelScope.launch { block() }
    }
}