//package com.bluemix.cashio.presentation.settings.vm
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.bluemix.cashio.core.common.Result
//import com.bluemix.cashio.data.local.preferences.UserPreferencesDataStore
//import com.bluemix.cashio.domain.usecase.keywordmapping.GetKeywordMappingsUseCase
//import com.bluemix.cashio.domain.usecase.preferences.ObserveDarkModeUseCase
//import com.bluemix.cashio.domain.usecase.preferences.SetDarkModeUseCase
//import com.bluemix.cashio.presentation.common.UiState
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.flow.distinctUntilChanged
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//
//class SettingsViewModel(
//    private val observeDarkModeUseCase: ObserveDarkModeUseCase,
//    private val setDarkModeUseCase: SetDarkModeUseCase,
//    private val getKeywordMappings: GetKeywordMappingsUseCase,
//) : ViewModel() {
//
//    private val _state = MutableStateFlow(SettingsState())
//    val state: StateFlow<SettingsState> = _state.asStateFlow()
//
//    init {
//        observePreferences()
//        loadKeywordMappings()
//    }
//
//    private fun observePreferences() {
//        viewModelScope.launch {
////            // Observe Currency changes
////            launch {
////                userPreferences.selectedCurrency
////                    .distinctUntilChanged()
////                    .collect { code ->
////                        // Fallback to USD if parsing fails
////                        val currency = Currency.fromCode(code) ?: Currency.USD
////                        updateState { it.copy(selectedCurrency = currency) }
////                    }
////            }
//
//            // Observe Dark Mode changes
//            launch {
//                observeDarkModeUseCase()
//                    .distinctUntilChanged()
//                    .collectLatest { enabled ->
//                        updateState { it.copy(darkModeEnabled = enabled) }
//                    }
//            }
//        }
//    }
//
//    /* ------------------------- Actions ------------------------- */
//
//    // ✅ CORRECT - Permissions never persisted
//    fun refreshPermissionStatus(smsGranted: Boolean, notificationGranted: Boolean) {
//        updateState {
//            it.copy(
//                smsPermissionGranted = smsGranted,
//                notificationAccessGranted = notificationGranted
//            )
//        }
//    }
//
//    fun setDarkMode(enabled: Boolean) {
//        viewModelScope.launch {
//            when (val result = setDarkModeUseCase(enabled)) {
//                is Result.Success -> {
//                    showMessage(SettingsMessage.Success("Dark mode enabled"))
//                }
//                is Result.Error -> {
//                    showMessage(SettingsMessage.Error("Light mode enabled"))
//                }
//                else -> Unit
//            }
//        }
//    }
//
////    fun changeCurrency(currency: Currency) {
////        viewModelScope.launch {
////            userPreferences.setSelectedCurrency(currency.code)
////            showMessage(SettingsMessage.Success("Currency updated to ${currency.name}"))
////        }
////    }
//
//    fun loadKeywordMappings() {
//        viewModelScope.launch {
//            updateState { it.copy(keywordMappings = UiState.Loading) }
//
//            when (val result = getKeywordMappings()) {
//                is Result.Success -> updateState {
//                    it.copy(keywordMappings = UiState.Success(result.data))
//                }
//
//                is Result.Error -> updateState {
//                    it.copy(
//                        keywordMappings = UiState.Error(
//                            result.message ?: "Failed to load keyword mappings"
//                        )
//                    )
//                }
//
//                else -> Unit
//            }
//        }
//    }
//
//    fun dismissMessage() {
//        updateState { it.copy(message = null) }
//    }
//
//    /* ------------------------- Helpers ------------------------- */
//
//    private fun showMessage(msg: SettingsMessage) {
//        updateState { it.copy(message = msg) }
//    }
//
//    private fun updateState(transform: (SettingsState) -> SettingsState) {
//        _state.update(transform)
//    }
//}


package com.bluemix.cashio.presentation.settings.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.usecase.keywordmapping.GetKeywordMappingsUseCase
import com.bluemix.cashio.domain.usecase.preferences.ClearAllDataUseCase
import com.bluemix.cashio.domain.usecase.preferences.ObserveDarkModeUseCase
import com.bluemix.cashio.domain.usecase.preferences.SetDarkModeUseCase
import com.bluemix.cashio.presentation.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * ViewModel for the Settings screen.
 *
 * Uses use cases for preferences management. Permissions are NEVER persisted -
 * they are always read from the system via PermissionHelper.
 */
class SettingsViewModel(
    private val observeDarkModeUseCase: ObserveDarkModeUseCase,
    private val setDarkModeUseCase: SetDarkModeUseCase,
    private val getKeywordMappingsUseCase: GetKeywordMappingsUseCase,
    private val clearAllDataUseCase: ClearAllDataUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        observeDarkMode()
        loadKeywordMappings()
    }

    private fun observeDarkMode() {
        viewModelScope.launch {
            observeDarkModeUseCase()
                .distinctUntilChanged()
                .collectLatest { enabled ->
                    updateState { it.copy(darkModeEnabled = enabled) }
                }
        }
    }

    /* ------------------------- Actions ------------------------- */

    /**
     * Updates permission status in UI state.
     *
     * Permissions are NEVER persisted to DataStore - the system is the single
     * source of truth. This method only updates the UI state for display purposes.
     */
    fun refreshPermissionStatus(smsGranted: Boolean, notificationGranted: Boolean) {
        updateState {
            it.copy(
                smsPermissionGranted = smsGranted,
                notificationAccessGranted = notificationGranted
            )
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            when (val result = setDarkModeUseCase(enabled)) {
                is Result.Success -> {
                    showMessage(
                        SettingsMessage.Success(
                            if (enabled) "Dark mode enabled" else "Light mode enabled"
                        )
                    )
                }

                is Result.Error -> {
                    showMessage(
                        SettingsMessage.Error(
                            result.message ?: "Failed to update dark mode"
                        )
                    )
                }

                else -> Unit
            }
        }
    }

    fun loadKeywordMappings() {
        viewModelScope.launch {
            updateState { it.copy(keywordMappings = UiState.Loading) }

            when (val result = getKeywordMappingsUseCase()) {
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

    fun showClearDataDialog() {
        updateState { it.copy(showClearDataConfirmation = true) }
    }

    fun dismissClearDataDialog() {
        updateState { it.copy(showClearDataConfirmation = false) }
    }

    fun confirmClearData() {
        if (_state.value.isClearingData) return

        viewModelScope.launch {
            updateState {
                it.copy(
                    isClearingData = true,
                    showClearDataConfirmation = false
                )
            }

            when (val result = clearAllDataUseCase()) {
                is Result.Success -> {
                    updateState { it.copy(isClearingData = false) }
                    showMessage(
                        SettingsMessage.Success("All data cleared successfully")
                    )
                    // Reload keyword mappings to reflect empty state
                    loadKeywordMappings()
                }

                is Result.Error -> {
                    updateState { it.copy(isClearingData = false) }
                    showMessage(
                        SettingsMessage.Error(
                            result.message ?: "Failed to clear data"
                        )
                    )
                }

                else -> {
                    updateState { it.copy(isClearingData = false) }
                }
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