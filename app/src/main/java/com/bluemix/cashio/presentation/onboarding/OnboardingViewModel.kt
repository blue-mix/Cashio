package com.bluemix.cashio.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.data.local.preferences.UserPreferencesDataStore
import com.bluemix.cashio.domain.model.Currency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingState(
    val selectedCurrency: Currency = Currency.USD,
    val smsPermissionGranted: Boolean = false,
    val currentStep: Int = 0,
    val isComplete: Boolean = false
)

class OnboardingViewModel(
    private val userPreferences: UserPreferencesDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferences.selectedCurrency.collect { currencyCode ->
                val currency = Currency.fromCode(currencyCode) ?: Currency.USD
                _state.update { it.copy(selectedCurrency = currency) }
            }
        }
    }

    fun selectCurrency(currency: Currency) {
        _state.update { it.copy(selectedCurrency = currency) }
        viewModelScope.launch {
            userPreferences.setSelectedCurrency(currency.code)
        }
    }

    fun setSmsPermission(granted: Boolean) {
        _state.update { it.copy(smsPermissionGranted = granted) }
        viewModelScope.launch {
            userPreferences.setSmsPermission(granted)
        }
    }

    fun nextStep() {
        _state.update { it.copy(currentStep = it.currentStep + 1) }
    }

    fun previousStep() {
        if (_state.value.currentStep > 0) {
            _state.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun skipSmsPermission() {
        // User can skip SMS permission and enable later in Settings
        _state.update { it.copy(smsPermissionGranted = false) }
        nextStep()
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setFirstLaunchComplete()
            _state.update { it.copy(isComplete = true) }
        }
    }
}
