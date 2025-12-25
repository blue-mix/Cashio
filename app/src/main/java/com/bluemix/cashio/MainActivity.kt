
package com.bluemix.cashio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bluemix.cashio.core.navigation.CashioNavHost
import com.bluemix.cashio.data.local.preferences.UserPreferencesDataStore
import com.bluemix.cashio.ui.theme.CashioTheme
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Opt-in to edge-to-edge so our UI can draw behind system bars.
        // We handle insets per-screen / per-Scaffold instead of forcing global padding.
        enableEdgeToEdge()

        setContent {
            MainContent()
        }
    }
}

/**
 * Root composition for the app.
 *
 * Kept separate from onCreate() for readability + easier testing/previews later.
 */
@Composable
private fun MainContent() {
    // Koin inject at the composition root so preferences are available immediately.
    val userPrefs: UserPreferencesDataStore = koinInject()

    // Dark mode is driven by stored user preference.
    // Note: `initial = false` avoids null state before DataStore emits.
    val isDarkThemeEnabled by userPrefs.darkModeEnabled.collectAsState(initial = false)

    CashioTheme(darkTheme = isDarkThemeEnabled) {
        // Surface gives us a consistent background for the whole app.
        // Use background (not surface) to match Material guidance for app canvas.
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CashioNavHost()
        }
    }
}
