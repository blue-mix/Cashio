package com.bluemix.cashio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.data.local.preferences.UserPreferencesDataStore
import com.bluemix.cashio.ui.navigation.CashioNavHost
import com.bluemix.cashio.ui.theme.CashioTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val userPrefs: UserPreferencesDataStore by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        setContent {

            MainContent(userPrefs = userPrefs)
        }
    }
}

@Composable
private fun MainContent(
    userPrefs: UserPreferencesDataStore,
) {
    val isDarkThemeEnabled by userPrefs.darkModeEnabled.collectAsStateWithLifecycle(
        initialValue = false
    )

    CashioTheme(darkTheme = isDarkThemeEnabled) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CashioNavHost()
        }
    }
}
