package com.bluemix.cashio

import android.animation.ObjectAnimator
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
import androidx.lifecycle.lifecycleScope
import com.bluemix.cashio.data.local.preferences.UserPreferencesDataStore
import com.bluemix.cashio.ui.navigation.CashioNavHost
import com.bluemix.cashio.ui.navigation.Route
import com.bluemix.cashio.ui.theme.CashioTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val userPrefs: UserPreferencesDataStore by inject()

    // null = not ready (keep system splash visible)
    private val startRoute = MutableStateFlow<Route?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splash.setKeepOnScreenCondition { startRoute.value == null }

        splash.setOnExitAnimationListener { splashScreenView ->
            ObjectAnimator.ofFloat(splashScreenView.view, "alpha", 1f, 0f).apply {
                duration = 220L
                start()
                doOnEndCompat { splashScreenView.remove() }
            }
        }

        lifecycleScope.launch {
            val completed = userPrefs.isOnboardingCompleted.first()
            startRoute.value = if (completed) Route.Splash else Route.Onboarding
        }

        setContent {
            CashioAppRoot(
                userPrefs = userPrefs,
                startDestinationFlow = startRoute
            )
        }
    }
}

/** Small compat helper so you don’t need API 26+ doOnEnd() imports. */
private inline fun ObjectAnimator.doOnEndCompat(crossinline block: () -> Unit) {
    addListener(object : android.animation.Animator.AnimatorListener {
        override fun onAnimationStart(animation: android.animation.Animator) = Unit
        override fun onAnimationEnd(animation: android.animation.Animator) = block()
        override fun onAnimationCancel(animation: android.animation.Animator) = block()
        override fun onAnimationRepeat(animation: android.animation.Animator) = Unit
    })
}

@Composable
fun CashioAppRoot(
    userPrefs: UserPreferencesDataStore,
    startDestinationFlow: StateFlow<Route?>
) {
    val isDarkThemeEnabled by userPrefs.darkModeEnabled.collectAsStateWithLifecycle(initialValue = false)
    val startDestination by startDestinationFlow.collectAsStateWithLifecycle()
    val start = startDestination ?: return

    CashioTheme(darkTheme = isDarkThemeEnabled) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CashioNavHost(startDestination = start)
        }
    }
}
