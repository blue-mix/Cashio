package com.bluemix.cashio.presentation.settings.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.data.local.preferences.UserPreferencesDataStore
import com.bluemix.cashio.domain.model.Currency
import com.bluemix.cashio.domain.usecase.keyword.GetKeywordMappingsUseCase
import com.bluemix.cashio.presentation.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferences: UserPreferencesDataStore,
    private val getKeywordMappings: GetKeywordMappingsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        observePreferences()
        loadKeywordMappings()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            // Observe Currency changes
            launch {
                userPreferences.selectedCurrency
                    .distinctUntilChanged()
                    .collect { code ->
                        // Fallback to USD if parsing fails
                        val currency = Currency.fromCode(code) ?: Currency.USD
                        updateState { it.copy(selectedCurrency = currency) }
                    }
            }

            // Observe Dark Mode changes
            launch {
                userPreferences.darkModeEnabled
                    .distinctUntilChanged()
                    .collect { enabled ->
                        updateState { it.copy(darkModeEnabled = enabled) }
                    }
            }
        }
    }

    /* ------------------------- Actions ------------------------- */

    fun refreshPermissionStatus(smsGranted: Boolean, notificationGranted: Boolean) {
        val old = state.value
        val changed =
            old.smsPermissionGranted != smsGranted || old.notificationAccessGranted != notificationGranted

        if (changed) {
            updateState {
                it.copy(
                    smsPermissionGranted = smsGranted,
                    notificationAccessGranted = notificationGranted
                )
            }
            // Sync with local storage for persistence if needed elsewhere
            viewModelScope.launch {
                userPreferences.setSmsPermission(smsGranted)
                userPreferences.setNotificationPermission(notificationGranted)
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            // We set the preference, and rely on observePreferences() to update the state boolean
            userPreferences.setDarkModeEnabled(enabled)
            showMessage(SettingsMessage.Success(if (enabled) "Dark mode enabled" else "Light mode enabled"))
        }
    }

    fun changeCurrency(currency: Currency) {
        viewModelScope.launch {
            userPreferences.setSelectedCurrency(currency.code)
            showMessage(SettingsMessage.Success("Currency updated to ${currency.name}"))
        }
    }

    fun loadKeywordMappings() {
        viewModelScope.launch {
            updateState { it.copy(keywordMappings = UiState.Loading) }

            when (val result = getKeywordMappings()) {
                is Result.Success -> updateState {
                    it.copy(keywordMappings = UiState.Success(result.data))
                }

                is Result.Error -> updateState {
                    it.copy(
                        keywordMappings = UiState.Error(
                            result.message ?: "Failed to load keyword mappings"
                        )
                    )
                }

                else -> Unit
            }
        }
    }

    fun dismissMessage() {
        updateState { it.copy(message = null) }
    }

    /* ------------------------- Helpers ------------------------- */

    private fun showMessage(msg: SettingsMessage) {
        updateState { it.copy(message = msg) }
    }

    private fun updateState(transform: (SettingsState) -> SettingsState) {
        _state.update(transform)
    }
}