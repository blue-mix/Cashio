package com.bluemix.cashio.ui.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluemix.cashio.ui.defaults.CashioPadding
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioSpacing

enum class StateCardVariant {
    LOADING,
    EMPTY,
    ERROR
}

data class StateCardAction(
    val text: String,
    val onClick: () -> Unit,
    val outlined: Boolean = false
)

// Constants for consistency
private object StateCardDefaults {
    val TonalElevation = 2.dp
    val ShadowElevation = 2.dp
    val EmojiSize = 44.sp
    val LoadingMinHeight = 120.dp
}

/**
 * Versatile state card for loading, empty, and error states.
 *
 * Uses [rememberSaveable] to prevent animation replay when navigating
 * back to a screen (e.g., after popping from the back stack).
 *
 * @param variant The type of state to display
 * @param title Optional headline text
 * @param message Optional body text with more context
 * @param emoji Optional decorative emoji
 * @param height Fixed height (null for wrap content)
 * @param action Optional action button
 * @param animated Whether to animate the card entrance
 * @param contentPadding Internal padding (defaults to card padding)
 */
@Composable
fun StateCard(
    variant: StateCardVariant,
    title: String? = null,
    message: String? = null,
    emoji: String? = null,
    height: Dp? = null,
    action: StateCardAction? = null,
    animated: Boolean = false,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(CashioPadding.card)
) {
    // rememberSaveable: survives recomposition and back-stack restoration
    var visible by rememberSaveable(key = "state_card_visibility") {
        mutableStateOf(!animated)
    }

    LaunchedEffect(Unit) {
        if (animated && !visible) {
            visible = true
        }
    }
    val colors = StateCardColors.current(variant)

    val cardContent: @Composable () -> Unit = {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .then(if (height != null) Modifier.height(height) else Modifier),
            shape = RoundedCornerShape(CashioRadius.medium),
            color = colors.background,
            tonalElevation = StateCardDefaults.TonalElevation,
            shadowElevation = StateCardDefaults.ShadowElevation
        ) {
            when (variant) {
                StateCardVariant.LOADING -> LoadingContent(height)
                else -> MessageContent(
                    emoji = emoji,
                    title = title,
                    message = message,
                    action = action,
                    colors = colors,
                    contentPadding = contentPadding
                )
            }
        }
    }

    if (animated) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + scaleIn(),
            label = "StateCardAnimatedVisibility"
        ) {
            cardContent()
        }
    } else {
        cardContent()
    }
}

@Composable
private fun LoadingContent(height: Dp?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (height != null) Modifier.height(height)
                else Modifier.height(StateCardDefaults.LoadingMinHeight)
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun MessageContent(
    emoji: String?,
    title: String?,
    message: String?,
    action: StateCardAction?,
    colors: StateCardColors,
    contentPadding: PaddingValues
) {
    Column(
        modifier = Modifier.padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CashioSpacing.sm)
    ) {
        if (!emoji.isNullOrBlank()) {
            Text(
                text = emoji,
                fontSize = StateCardDefaults.EmojiSize
            )
        }

        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.title,
                textAlign = TextAlign.Center
            )
        }

        if (!message.isNullOrBlank()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.message,
                textAlign = TextAlign.Center
            )
        }

        action?.let { act ->
            Spacer(Modifier.height(CashioSpacing.xs))

            if (act.outlined) {
                OutlinedButton(
                    onClick = act.onClick,
                    shape = RoundedCornerShape(CashioRadius.small)
                ) {
                    Text(act.text)
                }
            } else {
                Button(
                    onClick = act.onClick,
                    shape = RoundedCornerShape(CashioRadius.small)
                ) {
                    Text(act.text)
                }
            }
        }
    }
}

@Immutable
private data class StateCardColors(
    val background: Color,
    val title: Color,
    val message: Color
) {
    companion object {
        // Remove the @Composable here if you want to use it in remember,
        // OR just call it directly in the UI body.
        @Composable
        fun current(variant: StateCardVariant): StateCardColors {
            val cs = MaterialTheme.colorScheme
            return when (variant) {
                StateCardVariant.ERROR -> StateCardColors(
                    background = cs.errorContainer,
                    title = cs.onErrorContainer,
                    message = cs.onErrorContainer
                )
                else -> StateCardColors(
                    background = cs.surface,
                    title = cs.onSurface,
                    message = cs.onSurfaceVariant
                )
            }
        }
    }
}