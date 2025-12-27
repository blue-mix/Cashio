package com.bluemix.cashio.ui.components.defaults

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.bluemix.cashio.ui.theme.md_theme_dark_surface
import com.bluemix.cashio.ui.theme.md_theme_dark_surfaceVariant
import com.bluemix.cashio.ui.theme.md_theme_light_surface
import com.bluemix.cashio.ui.theme.md_theme_light_surfaceVariant

@Composable
fun AppBackground(
    dark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val brush = if (dark) {
        Brush.verticalGradient(
            listOf(
                md_theme_dark_surface,
                md_theme_dark_surfaceVariant
            )
        )

    } else {
        Brush.verticalGradient(
            listOf(
                md_theme_light_surface,
                md_theme_light_surfaceVariant
            )
        )

    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush)
    ) {
        content()
    }
}
