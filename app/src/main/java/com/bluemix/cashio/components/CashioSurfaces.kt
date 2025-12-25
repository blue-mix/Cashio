package com.bluemix.cashio.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
 * Why:
 * - guarantees consistent radius, elevation, surface color, and optional border
 * - keeps screen code cleaner (no repeated Surface boilerplate)
 *
 * Notes:
 * - If clickable, prefer Surface(onClick) so ripple + semantics work properly.
 * - Content padding is handled internally to keep screens consistent.
 */
@Composable
fun CashioCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(CashioCardDefaults.ContentPadding),
    cornerRadius: Dp = CashioCardDefaults.CornerRadius,
    showBorder: Boolean = true,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val border: BorderStroke? = if (showBorder) {
        BorderStroke(
            width = CashioCardDefaults.BorderWidth,
            color = MaterialTheme.colorScheme.outline.copy(alpha = CashioCardDefaults.BorderAlpha)
        )
    } else {
        null
    }

    if (onClick != null) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = CashioCardDefaults.TonalElevation,
            shadowElevation = CashioCardDefaults.ShadowElevation,
            border = border,
            onClick = onClick
        ) {
            Box(modifier = Modifier.padding(padding)) { content() }
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = CashioCardDefaults.TonalElevation,
            shadowElevation = CashioCardDefaults.ShadowElevation,
            border = border
        ) {
            Box(modifier = Modifier.padding(padding)) { content() }
        }
    }
}

/**
 * Convenience wrapper: default card WITH border.
 */
@Composable
fun CashioOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(CashioCardDefaults.ContentPadding),
    content: @Composable () -> Unit
) {
    CashioCard(
        modifier = modifier,
        onClick = onClick,
        padding = padding,
        showBorder = true,
        content = content
    )
}

/**
 * Convenience wrapper: default card WITHOUT border.
 */
@Composable
fun CashioPlainCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(CashioCardDefaults.ContentPadding),
    content: @Composable () -> Unit
) {
    CashioCard(
        modifier = modifier,
        onClick = onClick,
        padding = padding,
        showBorder = false,
        content = content
    )
}
