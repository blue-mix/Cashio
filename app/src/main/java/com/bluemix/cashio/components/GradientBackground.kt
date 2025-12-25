package com.bluemix.cashio.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

//
//@Composable
//fun GradientHeader(
//    modifier: Modifier = Modifier,
//    height: Dp = 260.dp,
//    isDarkMode: Boolean = isSystemInDarkTheme()
//) {
//    val colors = remember(isDarkMode) {
//        if (isDarkMode) {
//            listOf(
//                CashioPrimary.copy(alpha = 0.85f),        // bright glow
//                CashioPrimaryDark,                         // deep brand purple
//                Color(0xFF18192A),                         // soft indigo-black
//                Color(0xFF050610)                          // background dark
//            )
//        } else {
//            listOf(
//                CashioPrimary,                             // top glow
//                CashioPrimarySoft,                         // soft blush
//                Color(0xFFF5F5FA)                          // page background
//            )
//        }
//    }
//
//    Box(
//        modifier = modifier
//            .fillMaxWidth()
//            .height(height)
//            .drawBehind {
//                val center = Offset(size.width / 2f, 0f)
//                val radius = size.maxDimension * 1.3f
//
//                val brush = Brush.radialGradient(
//                    colors = colors,
//                    center = center,
//                    radius = radius
//                )
//
//                drawRect(brush = brush)
//            }
//    )
//}
@Composable
fun GradientHeader(
    modifier: Modifier = Modifier,
    height: Dp = 260.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .drawBehind {

                drawRect(Color.Black)

                val brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2f, 0f),
                    radius = size.width
                )

                // Scale Y to simulate ellipse 80% x 60%
                withTransform({
                    scale(scaleX = 1f, scaleY = 0.6f, pivot = Offset(size.width / 2f, 0f))
                }) {
                    drawRect(brush = brush)
                }
            }
    )
}

@Composable
fun DarkHorizonGlowBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.drawBehind {
            val center = Offset(size.width * 0.5f, size.height * 0.90f)
            val radius = size.maxDimension * 1.25f

            val brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.00f to Color(0xFF000000),
                    0.40f to Color(0xFF000000),
                    1.00f to Color(0xFF0D1A36)
                ),
                center = center,
                radius = radius
            )

            // Scale both axes a bit to mimic "125% 125%"
            withTransform({
                scale(
                    scaleX = 1.25f,
                    scaleY = 1.25f,
                    pivot = center
                )
            }) {
                drawRect(brush = brush)
            }
        }
    )
}

@Composable
fun DreamySunsetBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.drawBehind {
            // Base beige (matches bg-[#f5f5dc])
            drawRect(Color(0xFFF5F5DC))

            // 1) Linear gradient (top -> bottom)
            drawRect(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0xFFF5F5DC),                 // rgba(245,245,220,1)
                        0.25f to Color(0xFFFFDFBA).copy(alpha = 0.8f), // rgba(255,223,186,0.8)
                        0.50f to Color(0xFFFFB6C1).copy(alpha = 0.6f), // rgba(255,182,193,0.6)
                        0.75f to Color(0xFF9370DB).copy(alpha = 0.7f), // rgba(147,112,219,0.7)
                        1.00f to Color(0xFF483D8B).copy(alpha = 0.9f)  // rgba(72,61,139,0.9)
                    )
                )
            )

            // Helper to draw a radial glow at percentage positions.
            fun glow(
                cx: Float,
                cy: Float,
                color: Color,
                radiusFrac: Float
            ) {
                val center = Offset(size.width * cx, size.height * cy)
                val radius = size.minDimension * radiusFrac
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(color, Color.Transparent),
                        center = center,
                        radius = radius
                    )
                )
            }

            // 2) radial-gradient(circle at 30% 20%, rgba(255,255,224,0.4) 0%, transparent 50%)
            glow(
                cx = 0.30f,
                cy = 0.20f,
                color = Color(0xFFFFFFE0).copy(alpha = 0.4f),
                radiusFrac = 0.55f
            )

            // 3) radial-gradient(circle at 70% 80%, rgba(72,61,139,0.6) 0%, transparent 70%)
            glow(
                cx = 0.70f,
                cy = 0.80f,
                color = Color(0xFF483D8B).copy(alpha = 0.6f),
                radiusFrac = 0.85f
            )

            // 4) radial-gradient(circle at 50% 60%, rgba(147,112,219,0.3) 0%, transparent 60%)
            glow(
                cx = 0.50f,
                cy = 0.60f,
                color = Color(0xFF9370DB).copy(alpha = 0.3f),
                radiusFrac = 0.75f
            )
        }
    )
}

@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()

    Box(modifier = modifier.fillMaxSize()) {
        if (dark) {
            DarkHorizonGlowBackground(Modifier.fillMaxSize())
        } else {
            DreamySunsetBackground(Modifier.fillMaxSize())
        }

        content()
    }
}
