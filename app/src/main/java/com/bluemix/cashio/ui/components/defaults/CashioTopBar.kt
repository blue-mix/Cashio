package com.bluemix.cashio.ui.components.defaults

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.core.format.CashioFormat.toTopBarLabel
import java.time.LocalDate

/**
 * The standard Top Application Bar for Cashio screens.
 *
 * This composable wraps Material 3's [CenterAlignedTopAppBar] to provide
 * consistent styling across the app, including:
 * - "Pill" shaped action buttons with tonal backgrounds.
 * - Support for standard text titles or specialized Date selector titles.
 * - Automatic integration with the app's theme colors.
 *
 * @param title The content to display in the center (Text or Date).
 * @param modifier Modifier for the top bar layout.
 * @param leadingAction Optional configuration for the start-aligned button (e.g., Back, Menu).
 * @param trailingAction Optional configuration for the end-aligned button (e.g., Edit, Settings).
 * @param containerColor Background color of the bar. Defaults to transparent for edge-to-edge designs.
 * @param contentColor Color for icons and text.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashioTopBar(
    title: CashioTopBarTitle,
    modifier: Modifier = Modifier,
    leadingAction: TopBarAction? = null,
    trailingAction: TopBarAction? = null,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = containerColor,
            navigationIconContentColor = contentColor,
            titleContentColor = contentColor,
            actionIconContentColor = contentColor
        ),
        navigationIcon = {
            leadingAction?.let { action ->
                Box(modifier = Modifier.padding(start = CashioSpacing.small)) {
                    TopBarIconPill(
                        icon = action.icon,
                        onClick = action.onClick,
                        enabled = action.enabled
                    )
                }
            }
        },
        title = {
            when (title) {
                is CashioTopBarTitle.Text -> {
                    Text(
                        text = title.text,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                }

                is CashioTopBarTitle.Date -> {
                    DateTitle(
                        date = title.date,
                        contentColor = contentColor,
                        icon = title.icon
                    )
                }
            }
        },
        actions = {
            trailingAction?.let { action ->
                Box(modifier = Modifier.padding(end = CashioSpacing.small)) {
                    TopBarIconPill(
                        icon = action.icon,
                        onClick = action.onClick,
                        enabled = action.enabled
                    )
                }
            }
        }
    )
}

/* -------------------------------------------------------------------------- */
/* Models                                                                     */
/* -------------------------------------------------------------------------- */

/**
 * Defines the content style for the Top Bar's title area.
 */
sealed interface CashioTopBarTitle {
    /** A simple static text title. */
    data class Text(val text: String) : CashioTopBarTitle

    /** A dynamic title displaying a date, often used for dashboards or reports. */
    data class Date(
        val date: LocalDate = LocalDate.now(),
        val icon: TopBarIcon = TopBarIcon.Drawable(
            resId = com.bluemix.cashio.R.drawable.calendar
        )
    ) : CashioTopBarTitle
}

/**
 * Icon wrapper specific to Top Bar actions.
 * Abstraction to support both Vector and Drawable resources.
 */
sealed interface TopBarIcon {
    data class Vector(val imageVector: ImageVector) : TopBarIcon
    data class Drawable(@DrawableRes val resId: Int) : TopBarIcon
}

/**
 * Configuration for an interactive button within the Top Bar.
 *
 * @param icon The visual icon to display.
 * @param onClick Lambda invoked when the button is tapped.
 * @param enabled Whether the button accepts user input.
 */
data class TopBarAction(
    val icon: TopBarIcon,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)

/* -------------------------------------------------------------------------- */
/* Internal UI Building Blocks                                                */
/* -------------------------------------------------------------------------- */

private object CashioTopBarDefaults {
    val IconSize = 24.dp
    val IconPadding = CashioSpacing.small
    val IconTonalElevation = 2.dp
    val IconShadowElevation = 2.dp
    const val IconSurfaceAlpha = 0.96f
    val ButtonSize = 40.dp
    val DatePillPadding = PaddingValues(
        horizontal = CashioSpacing.medium,
        vertical = CashioSpacing.tiny
    )
    val DateIconSpacing = CashioSpacing.small
}

@Composable
private fun TopBarIconPill(
    icon: TopBarIcon,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Surface(
        modifier = Modifier.size(CashioTopBarDefaults.ButtonSize),
        shape = CircleShape,
        tonalElevation = CashioTopBarDefaults.IconTonalElevation,
        shadowElevation = CashioTopBarDefaults.IconShadowElevation,
        color = MaterialTheme.colorScheme.surface.copy(alpha = CashioTopBarDefaults.IconSurfaceAlpha),
        onClick = onClick,
        enabled = enabled
    ) {
        val iconModifier = Modifier
            .size(CashioTopBarDefaults.IconSize)
            .padding(CashioTopBarDefaults.IconPadding)

        when (icon) {
            is TopBarIcon.Vector -> {
                Icon(
                    imageVector = icon.imageVector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = iconModifier
                )
            }

            is TopBarIcon.Drawable -> {
                Icon(
                    painter = painterResource(id = icon.resId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = iconModifier
                )
            }
        }
    }
}

@Composable
private fun DateTitle(
    date: LocalDate,
    contentColor: Color,
    icon: TopBarIcon
) {
    val dateText = remember(date) { date.toTopBarLabel() }

    Row(
        modifier = Modifier.padding(CashioTopBarDefaults.DatePillPadding),
        horizontalArrangement = Arrangement.spacedBy(CashioTopBarDefaults.DateIconSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconModifier = Modifier.size(CashioTopBarDefaults.IconSize)
        val iconTint = MaterialTheme.colorScheme.primary

        when (icon) {
            is TopBarIcon.Vector -> {
                Icon(
                    imageVector = icon.imageVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = iconModifier
                )
            }

            is TopBarIcon.Drawable -> {
                Icon(
                    painter = painterResource(icon.resId),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = iconModifier
                )
            }
        }

        Text(
            text = dateText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}