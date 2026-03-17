package com.bluemix.cashio.presentation.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role

/* ══════════════════════════════════════════════════════════════════════════════
 *  Modern-Flat Interaction — Press-Scale
 *
 *  Replaces the default Material ripple with a subtle 0.98× scale-down
 *  on press. Creates a tactile, iOS/Linear-style feel without the
 *  expanding ink splat that screams "Google reference app."
 *
 *  Usage:
 *    Modifier.pressScale(onClick = { ... })
 *    Modifier.pressScale(onClick = { ... }, scaleFactor = 0.96f) // stronger
 * ══════════════════════════════════════════════════════════════════════════════ */

private const val DEFAULT_SCALE = 0.98f
private const val PRESS_ANIM_MS = 80

/**
 * Clickable modifier that scales the content to [scaleFactor] on press
 * instead of showing a ripple.
 *
 * @param onClick      Action on click.
 * @param enabled      Whether the element is interactive.
 * @param scaleFactor  Scale-down ratio on press (1.0 = no effect).
 * @param role         Semantic role for accessibility.
 */
@Composable
fun Modifier.pressScale(
    onClick: () -> Unit,
    enabled: Boolean = true,
    scaleFactor: Float = DEFAULT_SCALE,
    role: Role? = Role.Button
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleFactor else 1f,
        animationSpec = tween(durationMillis = PRESS_ANIM_MS),
        label = "pressScale"
    )

    return this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,           // no ripple
            enabled = enabled,
            role = role,
            onClick = onClick
        )
}