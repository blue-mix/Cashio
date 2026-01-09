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
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.data.local.preferences.UserPreferencesDataStore
import com.bluemix.cashio.domain.usecase.base.SeedDatabaseUseCase
import com.bluemix.cashio.ui.navigation.CashioNavHost
import com.bluemix.cashio.ui.navigation.Route
import com.bluemix.cashio.ui.theme.CashioTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/** Small compat helper so you don’t need API 26+ doOnEnd() imports. */
private inline fun ObjectAnimator.doOnEndCompat(crossinline block: () -> Unit) {
    addListener(object : android.animation.Animator.AnimatorListener {
        override fun onAnimationStart(animation: android.animation.Animator) = Unit
        override fun onAnimationEnd(animation: android.animation.Animator) = block()
        override fun onAnimationCancel(animation: android.animation.Animator) = block()
        override fun onAnimationRepeat(animation: android.animation.Animator) = Unit
    })
}

class MainActivity : ComponentActivity() {

    private val userPrefs: UserPreferencesDataStore by inject()
    private val seedDatabaseUseCase: SeedDatabaseUseCase by inject()

    private val appReady = MutableStateFlow(false)
    private val startRoute = MutableStateFlow<Route>(Route.Onboarding)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splash.setKeepOnScreenCondition { !appReady.value }

        splash.setOnExitAnimationListener { splashScreenView ->
            ObjectAnimator.ofFloat(splashScreenView.view, "alpha", 1f, 0f).apply {
                duration = 220L
                start()
                doOnEndCompat { splashScreenView.remove() }
            }
        }

        lifecycleScope.launch {
            // 1) Read onboarding state (fast)
            val completed = userPrefs.isOnboardingCompleted.first()
            startRoute.value = if (completed) Route.Dashboard else Route.Onboarding


            val seeded = userPrefs.isDbSeeded.first()
            if (!seeded) {
                val result = seedDatabaseUseCase()
                if (result is Result.Success) userPrefs.setDbSeeded(true)
                // If seeding fails, don’t block app forever. Log + proceed.
            }
            // 3) Release system splash
            appReady.value = true
        }

        setContent {
            CashioAppRoot(
                userPrefs = userPrefs,
                startDestinationFlow = startRoute
            )
        }
    }
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
