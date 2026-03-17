//package com.bluemix.cashio.ui.defaults
//
//import androidx.compose.material3.Icon
//import androidx.compose.material3.LocalContentColor
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.res.painterResource
//
///**
// * A unified model for icons in the application.
// * Decouples UI components from whether the icon is a vector or drawable resource.
// */
//sealed class CashioIcon {
//    data class Vector(val imageVector: ImageVector) : CashioIcon()
//    data class Drawable(val resId: Int) : CashioIcon()
//}
//
///**
// * Renders a [CashioIcon], handling both [ImageVector] and drawable resource variants.
// *
// * [tint] defaults to [LocalContentColor] — the ambient content color set by the
// * parent composable (e.g. Surface, Button). Passing [Color.Unspecified] was
// * previously the default, which caused icons to render invisible in contexts
// * where the parent did not tint them explicitly.
// *
// * Pass an explicit [Color] to override (e.g. for semantic income/expense colors).
// */
//@Composable
//fun CashioIcon(
//    icon: CashioIcon,
//    modifier: Modifier = Modifier,
//    tint: Color = LocalContentColor.current,
//    contentDescription: String? = null
//) {
//    when (icon) {
//        is CashioIcon.Vector -> Icon(
//            imageVector = icon.imageVector,
//            contentDescription = contentDescription,
//            modifier = modifier,
//            tint = tint
//        )
//
//        is CashioIcon.Drawable -> Icon(
//            painter = painterResource(icon.resId),
//            contentDescription = contentDescription,
//            modifier = modifier,
//            tint = tint
//        )
//    }
//}
package com.bluemix.cashio.ui.defaults

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource

/* ══════════════════════════════════════════════════════════════════════════════
 *  Cashio Design Tokens — Icon
 *
 *  Unified icon model. Unchanged from prior revision.
 * ══════════════════════════════════════════════════════════════════════════════ */

sealed class CashioIcon {
    data class Vector(val imageVector: ImageVector) : CashioIcon()
    data class Drawable(val resId: Int) : CashioIcon()
}

@Composable
fun CashioIcon(
    icon: CashioIcon,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = null
) {
    when (icon) {
        is CashioIcon.Vector -> Icon(
            imageVector = icon.imageVector,
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
        is CashioIcon.Drawable -> Icon(
            painter = painterResource(icon.resId),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    }
}