//package com.bluemix.cashio.ui.defaults
//
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//
//object CashioCardDefaults {
//    val CornerRadius: Dp = CashioShapes.card
//    val ContentPadding: Dp = CashioPadding.card
//    val BorderWidth: Dp = 1.dp
//    const val BorderAlpha: Float = 0.12f
//    val TonalElevation: Dp = 1.dp
//    val ShadowElevation: Dp = 2.dp
//}
//
///**
// * Standard card surface for Cashio.
// *
// * When [onClick] is provided the card is rendered as a [Surface] with clickable
// * semantics and a ripple. When null it renders as a static surface.
// *
// * [containerColor] defaults to [MaterialTheme.colorScheme.surface] so cards
// * automatically adapt to light/dark theme without explicit color overrides.
// */
//@Composable
//fun CashioCard(
//    modifier: Modifier = Modifier,
//    onClick: (() -> Unit)? = null,
//    padding: PaddingValues = PaddingValues(all = CashioCardDefaults.ContentPadding),
//    cornerRadius: Dp = CashioCardDefaults.CornerRadius,
//    showBorder: Boolean = true,
//    containerColor: Color = MaterialTheme.colorScheme.surface,
//    content: @Composable () -> Unit
//) {
//    val shape = RoundedCornerShape(cornerRadius)
//
//    val border: BorderStroke? = if (showBorder) {
//        BorderStroke(
//            width = CashioCardDefaults.BorderWidth,
//            color = MaterialTheme.colorScheme.outline.copy(alpha = CashioCardDefaults.BorderAlpha)
//        )
//    } else null
//
//    val body: @Composable () -> Unit = {
//        Box(modifier = Modifier.padding(padding)) { content() }
//    }
//
//    if (onClick != null) {
//        Surface(
//            modifier = modifier,
//            shape = shape,
//            color = containerColor,
//            border = border,
//            tonalElevation = CashioCardDefaults.TonalElevation,
//            onClick = onClick,
//            content = body
//        )
//    } else {
//        Surface(
//            modifier = modifier,
//            shape = shape,
//            color = containerColor,
//            border = border,
//            tonalElevation = CashioCardDefaults.TonalElevation,
//            content = body
//        )
//    }
//}