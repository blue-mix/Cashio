package com.bluemix.cashio.ui.system

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun SystemBarsMatchTheme(
    backgroundColorArgb: Int = MaterialTheme.colorScheme.background.toArgb(),
    darkIcons: Boolean,
) {
    val activity = LocalActivity.current ?: return
    val window = activity.window

    DisposableEffect(backgroundColorArgb, darkIcons) {
        // draw behind bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // set bar colors to match your splash background
        window.statusBarColor = backgroundColorArgb
        window.navigationBarColor = backgroundColorArgb

        // set icon brightness
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = darkIcons
        controller.isAppearanceLightNavigationBars = darkIcons

        onDispose { /* keep it, next screen will set its own */ }
    }
}
