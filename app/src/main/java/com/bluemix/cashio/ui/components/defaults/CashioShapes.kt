package com.bluemix.cashio.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized corner radius values for Cashio.
 *
 * Usage:
 * - shape = RoundedCornerShape(CashioRadius.medium)
 * - Surface(shape = RoundedCornerShape(CashioRadius.large))
 *
 * Benefits:
 * - Consistent roundness across all UI elements
 * - Easy to adjust brand identity
 * - Clear semantic naming
 */
object CashioRadius {
    /** 6dp - Extra small radius (subtle rounding) */
    val xs: Dp = 6.dp

    /** 12dp - Small radius (buttons, chips, small cards) */
    val small: Dp = 12.dp

    /** 14dp - Medium-small radius (text fields, search bars) */
    val mediumSmall: Dp = 14.dp

    /** 16dp - Medium radius (standard cards, sections) ⭐ Most common */
    val medium: Dp = 16.dp

    /** 20dp - Large radius (prominent cards, hero sections) */
    val large: Dp = 20.dp

    /** 999dp - Pill shape (fully rounded ends for buttons/pills) */
    val pill: Dp = 999.dp
}

/**
 * Semantic radius aliases for common use cases.
 */
object CashioShapes {
    /** Default card radius: 16dp */
    val card: Dp = CashioRadius.medium

    /** Button radius: 14dp */
    val button: Dp = CashioRadius.mediumSmall

    /** TextField radius: 12dp */
    val textField: Dp = CashioRadius.small

    /** Chip/tag radius: 999dp (pill) */
    val chip: Dp = CashioRadius.pill

    /** Dialog radius: 20dp */
    val dialog: Dp = CashioRadius.large
}
