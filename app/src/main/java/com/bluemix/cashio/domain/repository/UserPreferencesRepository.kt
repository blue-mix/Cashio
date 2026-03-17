package com.bluemix.cashio.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Contract for user preference persistence.
 *
 * Abstracts the underlying storage mechanism (DataStore, SharedPreferences, etc.)
 * from the presentation layer. ViewModels depend on this interface — never on
 * the concrete [com.bluemix.cashio.data.local.preferences.UserPreferencesDataStore].
 *
 * ## Currency default
 * The canonical default currency is **INR** — consistent with the app's default
 * categories and keyword mappings which are India-targeted.
 */
interface UserPreferencesRepository {

    // ── Onboarding ─────────────────────────────────────────────────────────

    val isOnboardingCompleted: Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)

    // ── Appearance ─────────────────────────────────────────────────────────

    val darkModeEnabled: Flow<Boolean>
    suspend fun setDarkModeEnabled(enabled: Boolean)

    // ── Currency ───────────────────────────────────────────────────────────

    /**
     * ISO 4217 currency code (e.g. "INR", "USD").
     * Emits "INR" as the default if no value has been stored.
     */
    val selectedCurrencyCode: Flow<String>
    suspend fun setSelectedCurrencyCode(code: String)
}