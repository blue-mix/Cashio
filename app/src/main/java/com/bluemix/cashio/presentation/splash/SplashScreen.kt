package com.bluemix.cashio.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import com.bluemix.cashio.ui.system.SystemBarsMatchTheme

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    viewModel: SplashViewModel = koinViewModel()
) {
    val ready by viewModel.isReady.collectAsStateWithLifecycle()

    val bg = MaterialTheme.colorScheme.background
    val darkIcons = bg.luminance() > 0.5f

    // ✅ makes system bars match splash background
    SystemBarsMatchTheme(
        backgroundColorArgb = bg.toArgb(),
        darkIcons = darkIcons
    )

    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.96f) } // slightly less bouncey start

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = spring(dampingRatio = 0.85f))
        scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.85f))

        // wait for typewriter duration roughly
        val text = "CASHIO"
        val charDelay = 55L
        delay(text.length * charDelay + 150)

        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg), // ✅ fills behind bars too
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            TypewriterText(fullText = "CASHIO", charDelayMs = 55)
        }
    }
}

@Composable
private fun TypewriterText(
    fullText: String,
    charDelayMs: Long = 60,
    modifier: Modifier = Modifier
) {
    var visibleCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(fullText) {
        visibleCount = 0
        for (i in 1..fullText.length) {
            visibleCount = i
            delay(charDelayMs)
        }
    }

    Text(
        text = fullText.take(visibleCount),
        modifier = modifier,
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.primary
    )
}
