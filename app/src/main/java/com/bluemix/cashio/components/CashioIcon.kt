package com.bluemix.cashio.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier

sealed class CashioIcon {
    data class Vector(val imageVector: ImageVector) : CashioIcon()
    data class Drawable(val resId: Int) : CashioIcon()
}

@Composable
fun CashioIcon(
    icon: CashioIcon,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color? = null,
    contentDescription: String? = null
) {
    when (icon) {
        is CashioIcon.Vector -> {
            Icon(
                imageVector = icon.imageVector,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint ?: androidx.compose.ui.graphics.Color.Unspecified
            )
        }
        is CashioIcon.Drawable -> {
            Icon(
                painter = painterResource(icon.resId),
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint ?: androidx.compose.ui.graphics.Color.Unspecified
            )
        }
    }
}
