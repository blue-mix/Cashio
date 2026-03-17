package com.bluemix.cashio.presentation.onboarding

import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.usecase.base.SeedDatabaseUseCase
import com.bluemix.cashio.domain.usecase.preferences.SetOnboardingCompletedUseCase
import com.bluemix.cashio.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
)

class OnboardingViewModel(
    private val seedDatabaseUseCase: SeedDatabaseUseCase,
    private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    /**
     * Seeds the database then marks onboarding complete.
     *
     * Onboarding is **never** marked complete if seeding fails — an empty
     * database would send the user to a blank dashboard with no explanation.
     * The error is surfaced so the UI can display a retry option.
     */
    fun completeOnboarding() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val seedResult = seedDatabaseUseCase()) {
                is Result.Success -> {
                    // Seeding succeeded (or was already done) — mark onboarding complete.
                    when (val prefResult = setOnboardingCompletedUseCase(true)) {
                        is Result.Success -> {
                            _uiState.update { it.copy(isLoading = false, isComplete = true) }
                        }

                        is Result.Error -> {
                            _uiState.update {
                                it.copy(isLoading = false, error = prefResult.message)
                            }
                        }

                        else -> Unit
                    }
                }

                is Result.Error -> {
                    // Seeding failed — do NOT mark onboarding complete.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Setup failed: ${seedResult.message ?: "Unknown error"}. Please try again."
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}