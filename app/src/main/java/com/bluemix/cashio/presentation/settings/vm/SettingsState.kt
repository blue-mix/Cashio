package com.bluemix.cashio.presentation.settings.vm

import com.bluemix.cashio.domain.model.Currency
import com.bluemix.cashio.domain.model.KeywordMapping
import com.bluemix.cashio.presentation.common.UiState

data class SettingsState(
    val selectedCurrency: Currency = Currency.USD,
    val smsPermissionGranted: Boolean = false,
    val notificationAccessGranted: Boolean = false,
    val keywordMappings: UiState<List<KeywordMapping>> = UiState.Idle,
    val message: SettingsMessage? = null,
    val darkModeEnabled: Boolean = false
)

sealed class SettingsMessage {
    data class Success(val text: String) : SettingsMessage()
    data class Error(val text: String) : SettingsMessage()
}
