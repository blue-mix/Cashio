package com.bluemix.cashio.ui.components.defaults

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource

/**
 * A unified model for icons in the application.
 *
 * This wrapper allows UI components to accept icons from either
 * Jetpack Compose [ImageVector]s or legacy Android Drawable resources
 * interchangeably, decoupling the UI from the resource type.
 */
sealed class CashioIcon {
    data class Vector(val imageVector: ImageVector) : CashioIcon()
    data class Drawable(val resId: Int) : CashioIcon()
}

/**
 * A composable utility to render a [CashioIcon].
 *
 * Handles the logic of switching between [Icon] with `imageVector`
 * and [Icon] with `painter` automatically.
 *
 * @param icon The [CashioIcon] data model to render.
 * @param modifier Modifier to be applied to the icon.
 * @param tint Color to tint the icon. Defaults to [Color.Unspecified] (original color) if null.
 * @param contentDescription Accessibility description for the icon.
 */
@Composable
fun CashioIcon(
    icon: CashioIcon,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    contentDescription: String? = null
) {
    when (icon) {
        is CashioIcon.Vector -> {
            Icon(
                imageVector = icon.imageVector,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint ?: Color.Unspecified
            )
        }

        is CashioIcon.Drawable -> {
            Icon(
                painter = painterResource(icon.resId),
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint ?: Color.Unspecified
            )
        }
    }
}