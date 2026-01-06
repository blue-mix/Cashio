package com.bluemix.cashio.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluemix.cashio.ui.system.SystemBarsMatchTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    viewModel: SplashViewModel = koinViewModel()
) {
    val bg = MaterialTheme.colorScheme.background
    val darkIcons = bg.luminance() > 0.5f

    // ✅ Make status + nav bars match splash background
    SystemBarsMatchTheme(
        backgroundColorArgb = bg.toArgb(),
        darkIcons = darkIcons
    )

    // Motion profile tuned for "soft + premium"
    val splashSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 380f
    )

    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.97f) }

    LaunchedEffect(Unit) {
        // Start app init (VM already seeds + delays 1s)
        viewModel.initApp(onFinished)

        // Visual intro
        launch { alpha.animateTo(1f, animationSpec = splashSpring) }
        launch { scale.animateTo(1f, animationSpec = splashSpring) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            TypewriterText(
                fullText = "CASHIO",
                startDelayMs = 220,  // slight beat feels intentional
                charDelayMs = 110
            )
        }
    }
}

@Composable
private fun TypewriterText(
    fullText: String,
    startDelayMs: Long = 0,
    charDelayMs: Long = 90,
    modifier: Modifier = Modifier
) {
    var visibleCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(fullText) {
        visibleCount = 0
        if (startDelayMs > 0) delay(startDelayMs)

        for (i in 1..fullText.length) {
            visibleCount = i

            // micro-timing makes it feel less robotic
            val d = when {
                i == 1 -> charDelayMs + 70
                i == fullText.length -> charDelayMs + 90
                else -> charDelayMs
            }
            delay(d)
        }
    }

    Text(
        text = fullText.take(visibleCount),
        modifier = modifier,
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.5.sp
    )
}
