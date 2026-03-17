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
import com.bluemix.cashio.domain.usecase.preferences.ObserveDarkModeUseCase
import com.bluemix.cashio.domain.usecase.preferences.ObserveOnboardingCompletedUseCase
import com.bluemix.cashio.ui.navigation.CashioNavHost
import com.bluemix.cashio.ui.navigation.Route
import com.bluemix.cashio.ui.theme.CashioTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/** Small compat helper so you don't need API 26+ doOnEnd() imports. */
private inline fun ObjectAnimator.doOnEndCompat(crossinline block: () -> Unit) {
    addListener(object : android.animation.Animator.AnimatorListener {
        override fun onAnimationStart(animation: android.animation.Animator) = Unit
        override fun onAnimationEnd(animation: android.animation.Animator) = block()
        override fun onAnimationCancel(animation: android.animation.Animator) = block()
        override fun onAnimationRepeat(animation: android.animation.Animator) = Unit
    })
}

/**
 * Main entry point for the Cashio app.
 *
 * ## Architecture decisions
 * 1. **No database seeding here** — seeding is handled exclusively by
 *    [OnboardingViewModel] when the user completes onboarding. This ensures
 *    seeding errors are surfaced to the user with a retry option.
 * 2. **Use cases instead of DataStore** — accesses preferences through use cases
 *    to maintain proper separation of concerns.
 * 3. **Splash screen** — kept on screen until onboarding status is determined.
 */
class MainActivity : ComponentActivity() {

    private val observeOnboardingCompletedUseCase: ObserveOnboardingCompletedUseCase by inject()
    private val observeDarkModeUseCase: ObserveDarkModeUseCase by inject()

    private val appReady = MutableStateFlow(false)
    private val startRoute = MutableStateFlow<Route?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep splash screen visible until we determine the start route
        splash.setKeepOnScreenCondition { !appReady.value }

        splash.setOnExitAnimationListener { splashScreenView ->
            ObjectAnimator.ofFloat(splashScreenView.view, "alpha", 1f, 0f).apply {
                duration = 220L
                start()
                doOnEndCompat { splashScreenView.remove() }
            }
        }

        // Determine start destination based on onboarding status
        lifecycleScope.launch {
            runCatching {
                val completed = observeOnboardingCompletedUseCase().first()
                startRoute.value = if (completed) Route.Dashboard else Route.Onboarding
            }.onFailure {
                // Log error if needed
                // Safe fallback: send to onboarding if we can't read preferences
                startRoute.value = Route.Onboarding
            }
            appReady.value = true
        }

        setContent {
            CashioAppRoot(
                observeDarkModeUseCase = observeDarkModeUseCase,
                startDestinationFlow = startRoute
            )
        }
    }
}

@Composable
fun CashioAppRoot(
    observeDarkModeUseCase: ObserveDarkModeUseCase,
    startDestinationFlow: StateFlow<Route?>
) {
    val isDarkThemeEnabled by observeDarkModeUseCase()
        .collectAsStateWithLifecycle(initialValue = false)

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