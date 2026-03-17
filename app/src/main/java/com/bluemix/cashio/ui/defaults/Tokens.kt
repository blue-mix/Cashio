package com.bluemix.cashio.ui.defaults

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/* ══════════════════════════════════════════════════════════════════════════════
 *  Cashio Design Tokens — Spacing · Shapes · Borders · Card
 *
 *  Aesthetic: Modern-Flat with custom visual rhythm.
 *
 *  Key departures from standard Material 3:
 *  ┌──────────────────────────────────────────────────────────────────────────┐
 *  │  1. "Squircle" corner radii — 16 / 20 / 28 dp instead of M3's            │
 *  │     tiny 12 dp. Friendlier, more tactile, Apple-esque.                   │
 *  │                                                                          │
 *  │  2. "Loose Grid" spacing — 2 / 6 / 12 / 16 / 20 / 32 / 48 dp.            │
 *  │     Intentionally avoids the lockstep 8dp grid that makes every app      │
 *  │     look like a Google reference implementation. The scale creates       │
 *  │     varied visual rhythm: tight where data is dense, generous where      │
 *  │     sections breathe.                                                    │
 *  │                                                                          │
 *  │  3. Zero elevation — no shadows anywhere. Depth comes from 1 dp          │
 *  │     border strokes and subtle fill differentiation.                      │
 *  └──────────────────────────────────────────────────────────────────────────┘
 * ══════════════════════════════════════════════════════════════════════════════ */

/* -------------------------------------------------------------------------- */
/*  Corner Radii — The "Squircle" System                                       */
/*                                                                             */
/*  Larger radii than M3 defaults for a softer, more premium feel.             */
/*  The jump between tiers is deliberate: small details stay crisp,            */
/*  containers feel like pillowed surfaces.                                    */
/* -------------------------------------------------------------------------- */
object CashioRadius {
    /** Text fields, chips, compact inputs — crisp but not sharp */
    val small: Dp  = 12.dp

    /** Cards, buttons, dialogs — the signature squircle */
    val medium: Dp = 20.dp

    /** Bottom sheets, hero cards, modals — pillowed and tactile */
    val large: Dp  = 28.dp

    /** Fully rounded — trend pills, badges, avatar rings */
    val pill: Dp   = 999.dp
}

/**
 * Semantic shape aliases — map UI concepts to the radius scale.
 */
object CashioShapes {
    val card: Dp       = CashioRadius.medium   // 20 dp — signature squircle
    val button: Dp     = CashioRadius.medium   // 20 dp
    val textField: Dp  = CashioRadius.small    // 12 dp
    val chip: Dp       = CashioRadius.pill     // fully rounded
    val dialog: Dp     = CashioRadius.large    // 28 dp
    val sheet: Dp      = CashioRadius.large    // 28 dp
}

/* -------------------------------------------------------------------------- */
/*  Spacing — "Loose Grid"                                                    */
/*                                                                            */
/*  Scale: 2 · 6 · 12 · 16 · 20 · 32 · 48                                     */
/*                                                                            */
/*  The 6 dp and 20 dp steps are the secret sauce. 6 dp is tighter than a     */
/*  standard 8 dp half-grid — perfect for icon-to-label gaps and dense data.  */
/*  20 dp sits between the "default" 16 and "generous" 24, giving cards and   */
/*  sections a distinctive breathing room that doesn't feel like vanilla M3.  */
/* -------------------------------------------------------------------------- */
object CashioSpacing {
    /** 2 dp — hairline: inner chip padding, tight icon gaps */
    val xxs: Dp   = 2.dp

    /** 6 dp — compact: icon-to-label, label-to-value, dense list gaps */
    val xs: Dp    = 6.dp

    /** 12 dp — snug: intra-card element spacing, chip rows */
    val sm: Dp    = 12.dp

    /** 16 dp — base: screen insets, standard card padding, section gaps */
    val md: Dp    = 16.dp

    /** 20 dp — airy: generous card padding, between card sections */
    val lg: Dp    = 20.dp

    /** 32 dp — roomy: major section separators, form section breaks */
    val xl: Dp    = 32.dp

    /** 48 dp — vast: scroll-end clearance, hero top margin */
    val xxl: Dp   = 48.dp
}

/**
 * Semantic padding aliases — recurring layout roles.
 */
object CashioPadding {
    /** Horizontal screen inset for every top-level layout */
    val screen: Dp         = CashioSpacing.lg    // 20 dp — wider than M3's 16

    /** Inner padding for standard cards */
    val card: Dp           = CashioSpacing.lg    // 20 dp — more breathing room

    /** Inner padding for compact / dense cards (keyword rows, list items) */
    val cardCompact: Dp    = CashioSpacing.md    // 16 dp

    /** Vertical padding between distinct sections in a form / screen */
    val sectionVertical: Dp = CashioSpacing.sm   // 12 dp

    /** Vertical padding inside a single list item */
    val listItem: Dp       = CashioSpacing.sm    // 12 dp
}

/* -------------------------------------------------------------------------- */
/*  Border Token                                                              */
/*                                                                            */
/*  THE defining element of the Modern-Flat aesthetic. Every card, input, and */
/*  container uses this border instead of shadows. The alpha is tuned per     */
/*  theme: light mode uses a soft stroke; dark mode uses a slightly brighter  */
/*  stroke to remain visible against Midnight Slate.                          */
/* -------------------------------------------------------------------------- */
object CashioBorder {
    val width: Dp = 1.dp

    /**
     * Returns a [BorderStroke] that adapts to the current theme.
     *
     * Light mode: outline at 16% alpha — visible but unobtrusive.
     * Dark mode:  outline at 16% alpha — the slightly brighter dark outline
     *             ensures visibility against the void.
     */
    @Composable
    fun stroke(): BorderStroke = BorderStroke(
        width = width,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
    )

    /** Variant for emphasized borders (selected states, focus rings). */
    @Composable
    fun emphasis(): BorderStroke = BorderStroke(
        width = width,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)
    )
}

/* -------------------------------------------------------------------------- */
/*  CashioCard — Flat, borderless-by-shadow, border-by-stroke                  */
/*                                                                             */
/*  Zero tonal elevation. Zero shadow elevation.                               */
/*  Depth hierarchy:                                                           */
/*    Background → surfaceContainerLow fill + stroke  (default card)           */
/*    Background → surfaceContainer fill + stroke     (elevated card)          */
/*  The fill colour is controlled by [containerColor] and defaults to          */
/*  surfaceContainerLow — one notch above the page background.                 */
/* -------------------------------------------------------------------------- */
object CashioCardDefaults {
    val CornerRadius: Dp    = CashioShapes.card        // 20 dp
    val ContentPadding: Dp  = CashioPadding.card       // 20 dp
    val TonalElevation: Dp  = 0.dp                     // flat — no M3 tinting
    val ShadowElevation: Dp = 0.dp                     // flat — no shadows
}

/**
 * Cashio's signature card surface.
 *
 * Completely flat: no shadow, no tonal elevation.
 * Separation from the background comes from:
 *  - A faint fill ([containerColor] defaults to `surfaceContainerLow`)
 *  - A 1 dp border stroke ([CashioBorder.stroke])
 *
 * When [onClick] is non-null, the card is rendered with clickable semantics.
 */
@Composable
fun CashioCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(all = CashioCardDefaults.ContentPadding),
    cornerRadius: Dp = CashioCardDefaults.CornerRadius,
    showBorder: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val border: BorderStroke? = if (showBorder) CashioBorder.stroke() else null

    val body: @Composable () -> Unit = {
        Box(modifier = Modifier.padding(padding)) { content() }
    }

    if (onClick != null) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = containerColor,
            border = border,
            tonalElevation = CashioCardDefaults.TonalElevation,
            shadowElevation = CashioCardDefaults.ShadowElevation,
            onClick = onClick,
            content = body
        )
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = containerColor,
            border = border,
            tonalElevation = CashioCardDefaults.TonalElevation,
            shadowElevation = CashioCardDefaults.ShadowElevation,
            content = body
        )
    }
}