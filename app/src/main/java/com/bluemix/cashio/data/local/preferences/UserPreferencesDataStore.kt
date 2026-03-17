package com.bluemix.cashio.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bluemix.cashio.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "cashio_user_preferences"
)

/**
 * DataStore-backed implementation of [UserPreferencesRepository].
 *
 * ## Context safety
 * Always pass [applicationContext] to this class. Storing an Activity or Fragment
 * context here would leak the context for the lifetime of the DataStore file.
 *
 * ## What is NOT stored here
 * - Permission grant status — always read from the system via [com.bluemix.cashio.core.util.PermissionHelper].
 *   Persisting permissions creates a stale cache that diverges when users revoke
 *   permissions from Android Settings without reopening the app.
 * - Database seed status — Realm's record count is the single source of truth.
 *   A persisted flag diverges when the database is migrated or cleared.
 *
 * ## Currency default
 * The default currency is **INR**, consistent with the app's India-targeted
 * default categories and keyword mappings.
 */
class UserPreferencesDataStore(context: Context) : UserPreferencesRepository {


    // Always use applicationContext to avoid leaking Activity/Fragment context.
    private val dataStore = context.applicationContext.dataStore

    // ── Keys ───────────────────────────────────────────────────────────────

    private companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val KEY_SELECTED_CURRENCY = stringPreferencesKey("selected_currency")
    }

    // ── Onboarding ─────────────────────────────────────────────────────────

    override val isOnboardingCompleted: Flow<Boolean> =
        dataStore.data.map { it[KEY_ONBOARDING_COMPLETED] ?: false }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    // ── Appearance ─────────────────────────────────────────────────────────

    override val darkModeEnabled: Flow<Boolean> =
        dataStore.data.map { it[KEY_DARK_MODE_ENABLED] ?: false }

    override suspend fun setDarkModeEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_DARK_MODE_ENABLED] = enabled }
    }

    // ── Currency ───────────────────────────────────────────────────────────

    /** Emits "INR" if no currency has been saved yet. */
    override val selectedCurrencyCode: Flow<String> =
        dataStore.data.map { it[KEY_SELECTED_CURRENCY] ?: "INR" }

    override suspend fun setSelectedCurrencyCode(code: String) {
        dataStore.edit { it[KEY_SELECTED_CURRENCY] = code.uppercase() }
    }
}