//package com.bluemix.cashio.ui.theme
//
//import android.os.Build
//import androidx.compose.material3.Typography
//import androidx.compose.ui.text.ExperimentalTextApi
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontVariation
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.sp
//import com.bluemix.cashio.R
//
///**
// * Manrope variable font — single file, multiple weights via FontVariation.
// * Gracefully falls back to system SansSerif on API < 26 (Oreo).
// */
//@OptIn(ExperimentalTextApi::class)
//val ManropeFontFamily = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//    FontFamily(
//        Font(
//            R.font.manrope,
//            variationSettings = FontVariation.Settings(FontVariation.weight(400))
//        )
//    )
//} else {
//    FontFamily.SansSerif
//}
//
//val CashioTypography = Typography(
//    displayLarge = TextStyle(
//        fontFamily = ManropeFontFamily,
//        fontWeight = FontWeight.Bold,
//        fontSize = 57.sp,
//        lineHeight = 64.sp,
//        letterSpacing = (-0.25).sp
//    ),
//    displayMedium = TextStyle(
//        fontFamily = ManropeFontFamily,
//        fontWeight = FontWeight.Bold,
//        fontSize = 45.sp,
//        lineHeight = 52.sp
//    ),
//    displaySmall = TextStyle(
//        fontFamily = ManropeFontFamily,
//        fontWeight = FontWeight.Bold,
//        fontSize = 36.sp,
//        lineHeight = 44.sp
//    ),
//    headlineLarge = TextStyle(
//        fontFamily = ManropeFontFamily,
//        fontWeight = FontWeight.SemiBold,
//        fontSize = 32.sp,
//        lineHeight = 40.sp
//    ),
//    headlineMedium = TextStyle(
//        fontFamily = ManropeFontFamily,
//        fontWeight = FontWeight.SemiBold,
//        fontSize = 28.sp,
//        lineHeight = 36.sp
//    ),
//    headlineSmall = TextStyle(
//        fontFamily = ManropeFontFamily,
//        fontWeight = FontWeight.SemiBold,
//        fontSize = 24.sp,
//        lineHeight = 32.sp
//    ),
//    titleLarge = TextStyle(
//        fontFamily = ManropeFontFamily,
//        fontWeight = FontWeight.SemiBold,
//        fontSize = 22.sp,
//        lineHeight = 28.sp
//    ),
//    titleMedium = TextStyle(
//        fontFamily = ManropeFontFamily,
//        fontWeight = FontWeight.SemiBold,
//        fontSize = 16.sp,
//        lineHeight = 24.sp,
//        letterSpacing = 0.15.sp
//    ),
//    titleSmall = TextStyle(
//        fontFamily = ManropeFontFamily,
//        fontWeight = FontWeight.Medium,
//        fontSize = 14.sp,
//        lineHeight = 20.sp,
//        letterSpacing = 0.1.sp
//    ),
//    bodyLarge = TextStyle(
//        fontFamily = ManropeFontFamily,
//        fontWeight = FontWeight.Normal,
//        fontSize = 16.sp,
//        lineHeight = 24.sp,
//        letterSpacing = 0.5.sp
//    ),
//    bodyMedium = TextStyle(
//        fontFamily = ManropeFontFamily,
//        fontWeight = FontWeight.Normal,
//        fontSize = 14.sp,
//        lineHeight = 20.sp,
//        letterSpacing = 0.25.sp
//    ),
//    bodySmall = TextStyle(
//        fontFamily = ManropeFontFamily,
//        fontWeight = FontWeight.Normal,
//        fontSize = 12.sp,
//        lineHeight = 16.sp,
//        letterSpacing = 0.4.sp
//    ),
//    labelLarge = TextStyle(
//        fontFamily = ManropeFontFamily,
//        fontWeight = FontWeight.Medium,
//        fontSize = 14.sp,
//        lineHeight = 20.sp,
//        letterSpacing = 0.1.sp
//    ),
//    labelMedium = TextStyle(
//        fontFamily = ManropeFontFamily,
//        fontWeight = FontWeight.Medium,
//        fontSize = 12.sp,
//        lineHeight = 16.sp,
//        letterSpacing = 0.5.sp
//    ),
//    labelSmall = TextStyle(
//        fontFamily = ManropeFontFamily,
//        fontWeight = FontWeight.Medium,
//        fontSize = 11.sp,
//        lineHeight = 16.sp,
//        letterSpacing = 0.5.sp
//    )
//)

package com.bluemix.cashio.ui.theme

import android.os.Build
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bluemix.cashio.R

/* ══════════════════════════════════════════════════════════════════════════════
 *  Cashio Design Tokens — Typography
 *
 *  Aesthetic: Dense, professional, data-forward.
 *  Inspired by Linear's tight text and Revolut's bold monetary figures.
 *
 *  Key differences from standard Material 3 type scale:
 *  ┌──────────────────────────────────────────────────────────────────────────┐
 *  │  1. Display roles use ExtraBold (W800) — the hero spend card should    │
 *  │     feel like a financial instrument, not a greeting card.              │
 *  │                                                                         │
 *  │  2. lineHeight is tightened everywhere. M3 defaults are airy;          │
 *  │     we compress to ~1.2× for display, ~1.3× for body, making data     │
 *  │     feel dense and information-rich.                                    │
 *  │                                                                         │
 *  │  3. letterSpacing is negative for display/headline (optical tightening  │
 *  │     at large sizes) and zero for body/label (maximum numeric clarity).  │
 *  │                                                                         │
 *  │  4. Labels use FontWeight.Medium (W500) universally — enough heft to   │
 *  │     anchor small text without competing with titles.                    │
 *  │                                                                         │
 *  │  5. Manrope applied to every single role. No platform defaults.        │
 *  └──────────────────────────────────────────────────────────────────────────┘
 * ══════════════════════════════════════════════════════════════════════════════ */

@OptIn(ExperimentalTextApi::class)
val ManropeFontFamily: FontFamily = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    FontFamily(
        Font(
            R.font.manrope,
            variationSettings = FontVariation.Settings(FontVariation.weight(400))
        )
    )
} else {
    FontFamily.SansSerif
}

val CashioTypography = Typography(

    /* ── Display ──────────────────────────────────────────────────────────
     * Hero monetary amounts: "₹ 24,500" on the dashboard.
     *
     * ExtraBold + tight tracking = the number IS the interface.
     * lineHeight at ~1.12× so the figure sits flush in its container.
     * ──────────────────────────────────────────────────────────────────── */
    displayLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.ExtraBold,      // W800 — bold brand voice
        fontSize = 56.sp,
        lineHeight = 64.sp,                     // 1.14×
        letterSpacing = (-0.5).sp               // optical tightening
    ),
    displayMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 44.sp,
        lineHeight = 50.sp,                     // 1.14×
        letterSpacing = (-0.4).sp
    ),
    displaySmall = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,           // W700 — slightly lighter
        fontSize = 34.sp,
        lineHeight = 40.sp,                     // 1.18×
        letterSpacing = (-0.25).sp
    ),

    /* ── Headline ─────────────────────────────────────────────────────────
     * Analytics section headers, "This Month Spend" label.
     * SemiBold with moderate tightening.
     * ──────────────────────────────────────────────────────────────────── */
    headlineLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,                     // 1.2×
        letterSpacing = (-0.2).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,                     // 1.23×
        letterSpacing = (-0.15).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,                     // 1.27×
        letterSpacing = (-0.1).sp
    ),

    /* ── Title ────────────────────────────────────────────────────────────
     * Card headers ("Category", "More Details"), top-bar text.
     * SemiBold anchors the section; no tracking adjustment needed.
     * ──────────────────────────────────────────────────────────────────── */
    titleLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,                     // 1.3×
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,                       // intentionally between 14/16
        lineHeight = 20.sp,                     // 1.33×
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,                     // 1.38×
        letterSpacing = 0.sp
    ),

    /* ── Body ─────────────────────────────────────────────────────────────
     * Transaction notes, dialog text, multi-line descriptions.
     *
     * Tighter than M3 defaults but still comfortable for reading.
     * letterSpacing at 0 sp — Manrope's natural spacing is already
     * optimised for mixed alpha-numeric content.
     * ──────────────────────────────────────────────────────────────────── */
    bodyLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,                     // 1.47×
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,                     // 1.38×
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,                     // 1.33×
        letterSpacing = 0.sp
    ),

    /* ── Label ────────────────────────────────────────────────────────────
     * Chip text, amounts in lists, "See All" links, filter pills.
     *
     * Medium weight throughout — consistent anchoring.
     * Zero letterSpacing keeps "₹ 1,234" compact and crisp.
     * ──────────────────────────────────────────────────────────────────── */
    labelLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,                     // 1.38×
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,                     // 1.33×
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,                     // 1.27×
        letterSpacing = 0.sp
    )
)