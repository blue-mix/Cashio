package com.bluemix.cashio.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.data.local.preferences.UserPreferencesDataStore
import com.bluemix.cashio.domain.model.Currency
import com.bluemix.cashio.domain.model.KeywordMapping
import com.bluemix.cashio.domain.usecase.keyword.AddKeywordMappingUseCase
import com.bluemix.cashio.domain.usecase.keyword.DeleteKeywordMappingUseCase
import com.bluemix.cashio.domain.usecase.keyword.GetKeywordMappingsUseCase
import com.bluemix.cashio.presentation.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class SettingsState(
    val selectedCurrency: Currency = Currency.USD,

    /**
     * Actual OS permission status (source of truth for UI Switch checked state)
     * Do NOT “set” these directly without checking system status again.
     */
    val smsPermissionGranted: Boolean = false,
    val notificationAccessGranted: Boolean = false,

    val keywordMappings: UiState<List<KeywordMapping>> = UiState.Idle,

    // Keyword add flow
    val isAddingKeyword: Boolean = false,
    val newKeyword: String = "",
    val newKeywordCategoryId: String = "",

    // One banner message at a time
    val message: SettingsMessage? = null,
    val darkModeEnabled: Boolean = false,
)

sealed class SettingsMessage {
    data class Success(val text: String) : SettingsMessage()
    data class Error(val text: String) : SettingsMessage()
}

class SettingsViewModel(
    private val userPreferences: UserPreferencesDataStore,
    private val getKeywordMappingsUseCase: GetKeywordMappingsUseCase,
    private val addKeywordMappingUseCase: AddKeywordMappingUseCase,
    private val deleteKeywordMappingUseCase: DeleteKeywordMappingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        observePreferences()
        loadKeywordMappings()
    }

    /**
     * Preferences (currency etc.)
     * Permission status should come from PermissionHelper (UI calls refreshPermissionStatus()).
     */
    private fun observePreferences() {
        viewModelScope.launch {
            userPreferences.selectedCurrency
                .distinctUntilChanged()
                .collect { code ->
                    val currency = Currency.fromCode(code) ?: Currency.USD
                    _state.update { it.copy(selectedCurrency = currency) }
                }
        }
        viewModelScope.launch {
            userPreferences.darkModeEnabled.collect { enabled ->
                _state.update { it.copy(darkModeEnabled = enabled) }
            }
        }
    }

    /**
     * Call this from UI on:
     * - screen enter
     * - onResume (important)
     * - after returning from settings screens
     */
    fun refreshPermissionStatus(
        smsGranted: Boolean,
        notificationGranted: Boolean
    ) {
        _state.update {
            it.copy(
                smsPermissionGranted = smsGranted,
                notificationAccessGranted = notificationGranted
            )
        }

        // Optional: keep DataStore aligned with reality
        // (useful if other parts of app read these flags)
        viewModelScope.launch {
            userPreferences.setSmsPermission(smsGranted)
            userPreferences.setNotificationPermission(notificationGranted)
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

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkModeEnabled(enabled)
            _state.update { it.copy(message = SettingsMessage.Success("Theme updated")) }
        }
    }
    // -------------------- Keyword mappings --------------------

    fun loadKeywordMappings() {
        viewModelScope.launch {
            _state.update { it.copy(keywordMappings = UiState.Loading) }

            when (val result = getKeywordMappingsUseCase()) {
                is Result.Success -> _state.update {
                    it.copy(keywordMappings = UiState.Success(result.data))
                }

                is Result.Error -> _state.update {
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

    fun startAddKeyword(categoryId: String) {
        _state.update {
            it.copy(
                isAddingKeyword = true,
                newKeyword = "",
                newKeywordCategoryId = categoryId,
                message = null
            )
        }
    }

    fun updateNewKeyword(keyword: String) {
        _state.update { it.copy(newKeyword = keyword) }
    }

    fun saveKeyword() {
        val keyword = _state.value.newKeyword.trim()
        if (keyword.isBlank()) {
            _state.update { it.copy(message = SettingsMessage.Error("Please enter a keyword")) }
            return
        }
        if (_state.value.newKeywordCategoryId.isBlank()) {
            _state.update { it.copy(message = SettingsMessage.Error("Please select a category")) }
            return
        }

        viewModelScope.launch {
            val mapping = KeywordMapping(
                id = "kw_${UUID.randomUUID()}",
                keyword = keyword,
                categoryId = _state.value.newKeywordCategoryId,
                priority = 5
            )

            when (val result = addKeywordMappingUseCase(mapping)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isAddingKeyword = false,
                            newKeyword = "",
                            message = SettingsMessage.Success("Keyword added successfully")
                        )
                    }
                    loadKeywordMappings()
                }

                is Result.Error -> _state.update {
                    it.copy(
                        message = SettingsMessage.Error(
                            result.message ?: "Failed to add keyword"
                        )
                    )
                }

                else -> Unit
            }
        }
    }

    fun deleteKeyword(mappingId: String) {
        viewModelScope.launch {
            when (val result = deleteKeywordMappingUseCase(mappingId)) {
                is Result.Success -> {
                    _state.update { it.copy(message = SettingsMessage.Success("Keyword deleted")) }
                    loadKeywordMappings()
                }

                is Result.Error -> _state.update {
                    it.copy(
                        message = SettingsMessage.Error(
                            result.message ?: "Failed to delete keyword"
                        )
                    )
                }

                else -> Unit
            }
        }
    }

    fun cancelKeywordDialog() {
        _state.update {
            it.copy(
                isAddingKeyword = false,
                newKeyword = "",
                newKeywordCategoryId = "",
                message = null
            )
        }
    }

    fun dismissMessage() {
        _state.update { it.copy(message = null) }
    }
}
