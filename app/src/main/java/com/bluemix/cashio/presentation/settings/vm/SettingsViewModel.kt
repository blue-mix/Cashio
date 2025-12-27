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
            userPreferences.selectedCurrency
                .distinctUntilChanged()
                .collect { code ->
                    val currency = Currency.Companion.fromCode(code) ?: Currency.Companion.USD
                    _state.update { it.copy(selectedCurrency = currency) }
                }
        }

        viewModelScope.launch {
            userPreferences.darkModeEnabled
                .distinctUntilChanged()
                .collect { enabled ->
                    _state.update { it.copy(darkModeEnabled = enabled) }
                }
        }
    }

    fun refreshPermissionStatus(
        smsGranted: Boolean,
        notificationGranted: Boolean
    ) {
        val old = _state.value
        val changed = old.smsPermissionGranted != smsGranted ||
                old.notificationAccessGranted != notificationGranted

        _state.update {
            it.copy(
                smsPermissionGranted = smsGranted,
                notificationAccessGranted = notificationGranted
            )
        }

        if (changed) {
            viewModelScope.launch {
                userPreferences.setSmsPermission(smsGranted)
                userPreferences.setNotificationPermission(notificationGranted)
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkModeEnabled(enabled)
            _state.update { it.copy(message = SettingsMessage.Success("Theme updated")) }
        }
    }

    fun changeCurrency(currency: Currency) {
        viewModelScope.launch {
            userPreferences.setSelectedCurrency(currency.code)
            _state.update {
                it.copy(message = SettingsMessage.Success("Currency updated to ${currency.name}"))
            }
        }
    }

    fun loadKeywordMappings() {
        viewModelScope.launch {
            _state.update { it.copy(keywordMappings = UiState.Loading) }

            when (val result = getKeywordMappings()) {
                is Result.Success -> _state.update { it.copy(keywordMappings = UiState.Success(result.data)) }
                is Result.Error -> _state.update {
                    it.copy(keywordMappings = UiState.Error(result.message ?: "Failed to load keyword mappings"))
                }
                else -> Unit
            }
        }
    }

    fun dismissMessage() {
        _state.update { it.copy(message = null) }
    }
}