package com.bluemix.cashio.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Standardized top bar for Cashio screens.
 *
 * Updated:
 * - TopBarAction now supports both ImageVector + drawable via TopBarIcon.
 * - Date icon is also customizable (drawable or vector).
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
                TopBarIconPill(
                    icon = action.icon,
                    onClick = action.onClick,
                    enabled = action.enabled
                )
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
                TopBarIconPill(
                    icon = action.icon,
                    onClick = action.onClick,
                    enabled = action.enabled
                )
            }
        }
    )
}

/* -------------------------------------------------------------------------- */
/* Models                                                                      */
/* -------------------------------------------------------------------------- */

sealed interface CashioTopBarTitle {
    data class Text(val text: String) : CashioTopBarTitle

    /**
     * Provide icon here so you can swap calendar icon to your drawable.
     */
    data class Date(
        val date: LocalDate = LocalDate.now(),
        val icon: TopBarIcon = TopBarIcon.Drawable(
            // ✅ Replace with your calendar drawable
            resId = com.bluemix.cashio.R.drawable.calendar
        )
    ) : CashioTopBarTitle
}

/**
 * Allow both vector + drawable.
 */
sealed interface TopBarIcon {
    data class Vector(val imageVector: ImageVector) : TopBarIcon
    data class Drawable(@DrawableRes val resId: Int) : TopBarIcon
}

data class TopBarAction(
    val icon: TopBarIcon,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)

/* -------------------------------------------------------------------------- */
/* UI building blocks                                                          */
/* -------------------------------------------------------------------------- */

private object CashioTopBarDefaults {
    val IconSize = 40.dp
    val IconPadding = 8.dp
    val IconTonalElevation = 2.dp
    val IconShadowElevation = 2.dp
    const val IconSurfaceAlpha = 0.96f

    val DatePillRadius = 999.dp
    val DatePillPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    val DateIconSpacing = 8.dp
}

@Composable
private fun TopBarIconPill(
    icon: TopBarIcon,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.size(CashioTopBarDefaults.IconSize),
        shape = CircleShape,
        tonalElevation = CashioTopBarDefaults.IconTonalElevation,
        shadowElevation = CashioTopBarDefaults.IconShadowElevation,
        color = MaterialTheme.colorScheme.surface.copy(alpha = CashioTopBarDefaults.IconSurfaceAlpha),
        onClick = onClick,
        enabled = enabled
    ) {
        when (icon) {
            is TopBarIcon.Vector -> {
                Icon(
                    imageVector = icon.imageVector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(CashioTopBarDefaults.IconSize)
                        .padding(CashioTopBarDefaults.IconPadding)
                )
            }

            is TopBarIcon.Drawable -> {
                Icon(
                    painter = painterResource(id = icon.resId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(CashioTopBarDefaults.IconSize)
                        .padding(CashioTopBarDefaults.IconPadding)
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

    Surface(
        shape = RoundedCornerShape(CashioTopBarDefaults.DatePillRadius),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(CashioTopBarDefaults.DatePillPadding),
            horizontalArrangement = Arrangement.spacedBy(CashioTopBarDefaults.DateIconSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (icon) {
                is TopBarIcon.Vector -> {
                    Icon(
                        imageVector = icon.imageVector,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                is TopBarIcon.Drawable -> {
                    Icon(
                        painter = painterResource(icon.resId),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier =Modifier.size(24.dp)
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
}

/* -------------------------------------------------------------------------- */
/* Helpers                                                                     */
/* -------------------------------------------------------------------------- */

private fun LocalDate.toTopBarLabel(): String {
    val formatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH)
    return format(formatter)
}
