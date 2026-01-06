package com.bluemix.cashio.ui.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluemix.cashio.ui.theme.CashioPadding
import com.bluemix.cashio.ui.theme.CashioRadius
import com.bluemix.cashio.ui.theme.CashioSpacing

/**
 * Visual styles for the state card.
 */
enum class StateCardVariant {
    /** Shows a centered circular progress indicator. */
    LOADING,

    /** Standard layout for empty states or information. */
    EMPTY,

    /** Styled with error container colors to indicate failure. */
    ERROR
}

/**
 * Configuration for an action button displayed at the bottom of the card.
 *
 * @property text The button label.
 * @property onClick The callback when the button is clicked.
 * @property outlined If true, renders an [OutlinedButton]; otherwise, a filled [Button].
 */
data class StateCardAction(
    val text: String,
    val onClick: () -> Unit,
    val outlined: Boolean = false
)

/**
 * A versatile card component used to display UI states (Loading, Empty, Error).
 *
 * It supports optional animation on entry, custom emojis, and action buttons.
 *
 * @param variant The visual mode of the card ([StateCardVariant.LOADING], [StateCardVariant.EMPTY], etc.).
 * @param title Optional headline text.
 * @param message Optional body text explaining the state.
 * @param emoji Optional large emoji displayed at the top (e.g., "😕" for errors).
 * @param height Optional fixed height. If null, the card wraps its content.
 * @param action Optional CTA button configuration.
 * @param animated If true, the card enters the screen with a fade+scale animation.
 * @param contentPadding Padding applied inside the card container.
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
    val visible = remember { mutableStateOf(!animated) }

    // Trigger animation if enabled
    LaunchedEffect(animated) {
        if (animated) visible.value = true
    }

    // Determine colors based on variant
    val (bg, fg) = when (variant) {
        StateCardVariant.ERROR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
    }

    // Content composable definition
    val cardContent = @Composable {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .then(if (height != null) Modifier.height(height) else Modifier),
            shape = RoundedCornerShape(CashioRadius.medium),
            color = bg,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp
        ) {
            when (variant) {
                StateCardVariant.LOADING -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (height != null) Modifier.height(height)
                                else Modifier.padding(CashioSpacing.huge)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.padding(contentPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(CashioSpacing.compact)
                    ) {
                        if (!emoji.isNullOrBlank()) {
                            Text(text = emoji, fontSize = 44.sp)
                        }
                        if (!title.isNullOrBlank()) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = fg,
                                textAlign = TextAlign.Center
                            )
                        }
                        if (!message.isNullOrBlank()) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (variant == StateCardVariant.ERROR) fg else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        action?.let { act ->
                            Spacer(Modifier.height(CashioSpacing.xs))
                            if (act.outlined) {
                                OutlinedButton(
                                    onClick = act.onClick,
                                    shape = RoundedCornerShape(CashioRadius.mediumSmall)
                                ) { Text(act.text) }
                            } else {
                                Button(
                                    onClick = act.onClick,
                                    shape = RoundedCornerShape(CashioRadius.mediumSmall)
                                ) { Text(act.text) }
                            }
                        }
                    }
                }
            }
        }
    }

    // Render with or without animation wrapper
    if (animated) {
        AnimatedVisibility(
            visible = visible.value,
            enter = fadeIn() + scaleIn(),
            label = "StateCardAnimatedVisibility"
        ) {
            cardContent()
        }
    } else {
        cardContent()
    }
}