package com.bluemix.cashio.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore(private val context: Context) {

    private object PreferencesKeys {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val SMS_PERMISSION_GRANTED = booleanPreferencesKey("sms_permission_granted")
        val SELECTED_CURRENCY = stringPreferencesKey("selected_currency")
        val NOTIFICATION_PERMISSION_GRANTED =
            booleanPreferencesKey("notification_permission_granted")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    }

    private val ds = context.dataStore

    private companion object {
        val DB_SEEDED = booleanPreferencesKey("db_seeded")
    }

    val isDbSeeded: Flow<Boolean> = ds.data.map { prefs ->
        prefs[DB_SEEDED] == true
    }

    suspend fun setDbSeeded(value: Boolean) {
        ds.edit { prefs ->
            prefs[DB_SEEDED] = value
        }
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_FIRST_LAUNCH] != false
    }

    val smsPermissionGranted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SMS_PERMISSION_GRANTED] == true
    }

    val selectedCurrency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SELECTED_CURRENCY] ?: "USD"
    }
    val darkModeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DARK_MODE_ENABLED] == true
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DARK_MODE_ENABLED] = enabled
        }
    }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun setSmsPermission(granted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SMS_PERMISSION_GRANTED] = granted
        }
    }

    suspend fun setSelectedCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_CURRENCY] = currency
        }
    }

    val notificationPermissionGranted: Flow<Boolean> =
        context.dataStore.data.map { preferences ->  // ✅ ADD
            preferences[PreferencesKeys.NOTIFICATION_PERMISSION_GRANTED] == true
        }

    suspend fun setNotificationPermission(granted: Boolean) {  // ✅ ADD
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_PERMISSION_GRANTED] = granted
        }
    }
}
