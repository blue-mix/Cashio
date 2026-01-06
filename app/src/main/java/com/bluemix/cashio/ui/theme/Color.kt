package com.bluemix.cashio.ui.theme

import androidx.compose.ui.graphics.Color

/* -------------------------------------------------------------------------- */
/* Brand Palette                                                             */
/* -------------------------------------------------------------------------- */
object CashioBrand {
    // Primary brand purple (indigo)
    val Primary = Color(0xFF6356E5)
    val PrimaryDark = Color(0xFF4336C4)
    val PrimarySoft = Color(0xFFE2E0FF)

    // Secondary / supporting purple-gray
    val Secondary = Color(0xFF7B7EA8)
    val SecondarySoft = Color(0xFFE2E3F5)

    // Accent / CTA highlight
    val Tertiary = Color(0xFFF59E0B)        // warm amber
    val TertiarySoft = Color(0xFFFFE8B4)
}

/* -------------------------------------------------------------------------- */
/* Semantic Colors                                                           */
/* -------------------------------------------------------------------------- */
object CashioSemantic {
    // Light Mode Semantics (High Contrast)
    val IncomeGreen = Color(0xFF16A34A)
    val ExpenseRed = Color(0xFFEF4444)

    // Dark Mode Semantics (Pastel/Soft for visibility on dark backgrounds)
    val IncomeGreenDark = Color(0xFF4ADE80) // Lighter green
    val ExpenseRedDark = Color(0xFFF87171)  // Lighter red

    val Warning = Color(0xFFF59E0B)
    val Info = Color(0xFF0EA5E9)
}

/* -------------------------------------------------------------------------- */
/* Neutral Utility Colors                                                     */
/* -------------------------------------------------------------------------- */
object CashioNeutral {
    val GrayLight = Color(0xFFE5E7EB)  // Updated to be lighter for dividers
    val GrayMedium = Color(0xFF9CA3AF)
    val GrayDark = Color(0xFF4B5563)
    val Scrim = Color.Black.copy(alpha = 0.32f) // Standard scrim
}

/* -------------------------------------------------------------------------- */
/* Material 3 – Light Theme Tokens                                           */
/* -------------------------------------------------------------------------- */
val md_theme_light_primary = CashioBrand.Primary
val md_theme_light_onPrimary = Color.White
val md_theme_light_primaryContainer = CashioBrand.PrimarySoft
val md_theme_light_onPrimaryContainer = Color(0xFF171356)

val md_theme_light_secondary = CashioBrand.Secondary
val md_theme_light_onSecondary = Color.White
val md_theme_light_secondaryContainer = CashioBrand.SecondarySoft
val md_theme_light_onSecondaryContainer = Color(0xFF191A2C)

val md_theme_light_tertiary = CashioBrand.Tertiary
val md_theme_light_onTertiary = Color(0xFF231600)
val md_theme_light_tertiaryContainer = CashioBrand.TertiarySoft
val md_theme_light_onTertiaryContainer = Color(0xFF281800)

val md_theme_light_error = CashioSemantic.ExpenseRed
val md_theme_light_errorContainer = Color(0xFFFFE5E5)
val md_theme_light_onError = Color.White
val md_theme_light_onErrorContainer = Color(0xFF7F1D1D)

val md_theme_light_background = Color(0xFFF5F5FA)
val md_theme_light_onBackground = Color(0xFF111827)

// Surfaces
val md_theme_light_surface = Color.White
val md_theme_light_onSurface = Color(0xFF111827)
val md_theme_light_surfaceVariant = Color(0xFFE3E2F2)
val md_theme_light_onSurfaceVariant = Color(0xFF4B4A65)

// NEW: Surface Containers (Crucial for M3 Cards)
val md_theme_light_surfaceContainerLowest = Color(0xFFFFFFFF)
val md_theme_light_surfaceContainerLow = Color(0xFFF3F3F8)
val md_theme_light_surfaceContainer = Color(0xFFEDEDF4)
val md_theme_light_surfaceContainerHigh = Color(0xFFE7E7EE)
val md_theme_light_surfaceContainerHighest = Color(0xFFE1E1E8)

// Outlines
val md_theme_light_outline = CashioNeutral.GrayMedium
val md_theme_light_outlineVariant = CashioNeutral.GrayLight // For dividers

val md_theme_light_inverseSurface = Color(0xFF18181F)
val md_theme_light_inverseOnSurface = Color(0xFFF3F4FF)
val md_theme_light_inversePrimary = Color(0xFFC7C5FF)
val md_theme_light_scrim = CashioNeutral.Scrim

/* -------------------------------------------------------------------------- */
/* Material 3 – Dark Theme Tokens                                            */
/* -------------------------------------------------------------------------- */
val md_theme_dark_primary = Color(0xFFC7C5FF)
val md_theme_dark_onPrimary = Color(0xFF23206B)
val md_theme_dark_primaryContainer = Color(0xFF3A378C)
val md_theme_dark_onPrimaryContainer = CashioBrand.PrimarySoft

val md_theme_dark_secondary = Color(0xFFC6C6F2)
val md_theme_dark_onSecondary = Color(0xFF25274B)
val md_theme_dark_secondaryContainer = Color(0xFF383A61)
val md_theme_dark_onSecondaryContainer = CashioBrand.SecondarySoft

val md_theme_dark_tertiary = Color(0xFFFACC6B)
val md_theme_dark_onTertiary = Color(0xFF2C1F00)
val md_theme_dark_tertiaryContainer = Color(0xFF4A3700)
val md_theme_dark_onTertiaryContainer = CashioBrand.TertiarySoft

// Using pastel versions for dark mode readability
val md_theme_dark_error = CashioSemantic.ExpenseRedDark
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

val md_theme_dark_background = Color(0xFF050610)
val md_theme_dark_onBackground = Color(0xFFE5E7F5)

// Surfaces
val md_theme_dark_surface = Color(0xFF0B0C18)
val md_theme_dark_onSurface = Color(0xFFE5E7F5)
val md_theme_dark_surfaceVariant = Color(0xFF2D3045)
val md_theme_dark_onSurfaceVariant = Color(0xFFC5C6DD)

// NEW: Surface Containers (Dark mode hierarchy)
val md_theme_dark_surfaceContainerLowest = Color(0xFF000000)
val md_theme_dark_surfaceContainerLow = Color(0xFF0F101E)
val md_theme_dark_surfaceContainer = Color(0xFF161726)
val md_theme_dark_surfaceContainerHigh = Color(0xFF212232)
val md_theme_dark_surfaceContainerHighest = Color(0xFF2C2D3E)

// Outlines
val md_theme_dark_outline = Color(0xFF8D90AA)
val md_theme_dark_outlineVariant = Color(0xFF44475F)

val md_theme_dark_inverseSurface = Color(0xFFE5E7F5)
val md_theme_dark_inverseOnSurface = Color(0xFF111827)
val md_theme_dark_inversePrimary = CashioBrand.Primary
val md_theme_dark_scrim = CashioNeutral.Scrim