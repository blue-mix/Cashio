package com.bluemix.cashio.ui.components.defaults

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource

sealed class CashioIcon {
    data class Vector(val imageVector: ImageVector) : CashioIcon()
    data class Drawable(val resId: Int) : CashioIcon()
}

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
