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

enum class StateCardVariant { LOADING, EMPTY, ERROR, INFO }

data class StateCardAction(
    val text: String,
    val onClick: () -> Unit,
    val outlined: Boolean = false
)

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
    contentPadding: PaddingValues = PaddingValues(20.dp)
) {
    val visible = remember { mutableStateOf(!animated) }
    LaunchedEffect(animated) { if (animated) visible.value = true }

    val (bg, fg) = when (variant) {
        StateCardVariant.ERROR -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
    }

    val card = @Composable {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .then(if (height != null) Modifier.height(height) else Modifier),
            shape = RoundedCornerShape(16.dp),
            color = bg,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp
        ) {
            when (variant) {
                StateCardVariant.LOADING -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (height != null) Modifier.height(height) else Modifier.padding(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.padding(contentPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Spacer(Modifier.height(4.dp))
                            if (act.outlined) {
                                OutlinedButton(onClick = act.onClick) { Text(act.text) }
                            } else {
                                Button(onClick = act.onClick) { Text(act.text) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (animated) {
        AnimatedVisibility(
            visible = visible.value,
            enter = fadeIn() + scaleIn(),
            label = "StateCardAnimatedVisibility"
        ) { card() }
    } else {
        card()
    }
}
