package com.bluemix.cashio.ui.components.defaults

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized spacing values for Cashio.
 *
 * Usage:
 * - Modifier.padding(CashioSpacing.medium)
 * - contentPadding = PaddingValues(CashioSpacing.default)
 * - Arrangement.spacedBy(CashioSpacing.small)
 *
 * Benefits:
 * - Single source of truth for spacing
 * - Consistent visual rhythm across app
 * - Easy to adjust globally
 */
object CashioSpacing {
    /** 2dp - Minimal spacing (e.g., small gaps in compact layouts) */
    val xxs: Dp = 2.dp

    /** 4dp - Extra small spacing (e.g., icon padding, tight elements) */
    val xs: Dp = 4.dp

    /** 6dp - Tiny spacing (e.g., label-to-value gaps) */
    val tiny: Dp = 6.dp

    /** 8dp - Small spacing (e.g., between related items, list spacing) */
    val small: Dp = 8.dp

    /** 10dp - Compact spacing (e.g., dropdown menu items) */
    val compact: Dp = 10.dp

    /** 12dp - Medium spacing (e.g., card content, section gaps) */
    val medium: Dp = 12.dp

    /** 14dp - Medium-large spacing (e.g., card padding, row padding) */
    val mediumLarge: Dp = 14.dp

    /** 16dp - Default spacing (e.g., screen padding, standard gaps) ⭐ Most common */
    val default: Dp = 16.dp

    /** 18dp - Large spacing (e.g., emphasized sections) */
    val large: Dp = 18.dp

    /** 20dp - Extra large spacing (e.g., card padding, major sections) */
    val xl: Dp = 20.dp

    /** 22dp - Extra extra large spacing (e.g., empty state padding) */
    val xxl: Dp = 22.dp

    /** 24dp - Huge spacing (e.g., screen-level margins, hero sections) */
    val huge: Dp = 24.dp

    /** 32dp - Massive spacing (e.g., onboarding screens, major dividers) */
    val massive: Dp = 32.dp
}

/**
 * Common padding presets for screens and cards.
 */
object CashioPadding {
    /** Standard screen horizontal padding: 16dp */
    val screen: Dp = CashioSpacing.default

    /** Card content padding: 16dp */
    val card: Dp = CashioSpacing.default

    /** Compact card padding: 14dp */
    val cardCompact: Dp = CashioSpacing.mediumLarge

    /** Section spacing in LazyColumn: 12dp to 16dp */
    val sectionVertical: Dp = CashioSpacing.medium

    /** Item spacing in lists: 12dp */
    val listItem: Dp = CashioSpacing.medium
}
