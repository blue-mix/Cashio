package com.bluemix.cashio.ui.components.defaults

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Central defaults for all "Card-like" surfaces in the app.
 * Keep these consistent to avoid UI drift across screens.
 */
object CashioCardDefaults {
    val CornerRadius: Dp = 16.dp
    val ContentPadding: Dp = 16.dp

    val BorderWidth: Dp = 1.dp
    const val BorderAlpha: Float = 0.12f

    val TonalElevation: Dp = 1.dp
    val ShadowElevation: Dp = 2.dp
}

/**
 * App-wide Card wrapper.
 *
 * Notes:
 * - If clickable, prefer Surface(onClick) so ripple + semantics work properly.
 * - Content padding is handled internally to keep screens consistent.
 */
@Composable
fun CashioCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(all = CashioCardDefaults.ContentPadding),
    cornerRadius: Dp = CashioCardDefaults.CornerRadius,
    showBorder: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val border: BorderStroke? =
        if (showBorder) {
            BorderStroke(
                width = CashioCardDefaults.BorderWidth,
                color = MaterialTheme.colorScheme.outline.copy(alpha = CashioCardDefaults.BorderAlpha)
                // if you have it: outlineVariant
            )
        } else null

    val body: @Composable () -> Unit = {
        Box(modifier = Modifier.padding(padding)) { content() }
    }

    if (onClick != null) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = containerColor,
            tonalElevation = CashioCardDefaults.TonalElevation,
            shadowElevation = CashioCardDefaults.ShadowElevation,
            border = border,
            onClick = onClick,
            content = body
        )
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = containerColor,
            tonalElevation = CashioCardDefaults.TonalElevation,
            shadowElevation = CashioCardDefaults.ShadowElevation,
            border = border,
            content = body
        )
    }
}
