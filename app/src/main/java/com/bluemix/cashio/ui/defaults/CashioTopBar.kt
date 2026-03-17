//package com.bluemix.cashio.ui.defaults
//
//import androidx.annotation.DrawableRes
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.CenterAlignedTopAppBar
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.bluemix.cashio.R
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//import java.util.Locale
//
///* -------------------------------------------------------------------------- */
///* Models                                                                      */
///* -------------------------------------------------------------------------- */
//
//sealed interface CashioTopBarTitle {
//    data class Text(val text: String) : CashioTopBarTitle
//    data class Date(
//        val date: LocalDate = LocalDate.now(),
//        val icon: TopBarIcon = TopBarIcon.Drawable(R.drawable.calendar)
//    ) : CashioTopBarTitle
//}
//
//sealed interface TopBarIcon {
//    data class Vector(val imageVector: ImageVector) : TopBarIcon
//    data class Drawable(@DrawableRes val resId: Int) : TopBarIcon
//}
//
//data class TopBarAction(
//    val icon: TopBarIcon,
//    val onClick: () -> Unit,
//    val enabled: Boolean = true
//)
//
///* -------------------------------------------------------------------------- */
///* Top Bar                                                                     */
///* -------------------------------------------------------------------------- */
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CashioTopBar(
//    title: CashioTopBarTitle,
//    modifier: Modifier = Modifier,
//    leadingAction: TopBarAction? = null,
//    trailingAction: TopBarAction? = null,
//    containerColor: Color = Color.Transparent,
//    contentColor: Color = MaterialTheme.colorScheme.onBackground,
//) {
//    CenterAlignedTopAppBar(
//        modifier = modifier,
//        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//            containerColor = containerColor,
//            navigationIconContentColor = contentColor,
//            titleContentColor = contentColor,
//            actionIconContentColor = contentColor
//        ),
//        navigationIcon = {
//            leadingAction?.let { action ->
//                Box(modifier = Modifier.padding(start = CashioSpacing.small)) {
//                    TopBarIconPill(
//                        icon = action.icon,
//                        onClick = action.onClick,
//                        enabled = action.enabled
//                    )
//                }
//            }
//        },
//        title = {
//            when (title) {
//                is CashioTopBarTitle.Text -> Text(
//                    text = title.text,
//                    style = MaterialTheme.typography.titleLarge,
//                    fontWeight = FontWeight.SemiBold,
//                    color = contentColor
//                )
//
//                is CashioTopBarTitle.Date -> DateTitle(
//                    date = title.date,
//                    contentColor = contentColor,
//                    icon = title.icon
//                )
//            }
//        },
//        actions = {
//            trailingAction?.let { action ->
//                Box(modifier = Modifier.padding(end = CashioSpacing.small)) {
//                    TopBarIconPill(
//                        icon = action.icon,
//                        onClick = action.onClick,
//                        enabled = action.enabled
//                    )
//                }
//            }
//        }
//    )
//}
//
///* -------------------------------------------------------------------------- */
///* Private building blocks                                                     */
///* -------------------------------------------------------------------------- */
//
//private val ICON_SIZE = 24.dp
//private val BUTTON_SIZE = 40.dp
//private val TONAL_ELEV = 2.dp
//private val SHADOW_ELEV = 2.dp
//private const val SURFACE_ALPHA = 0.96f
//
//@Composable
//private fun TopBarIconPill(icon: TopBarIcon, onClick: () -> Unit, enabled: Boolean) {
//    Surface(
//        modifier = Modifier.size(BUTTON_SIZE),
//        shape = CircleShape,
//        tonalElevation = TONAL_ELEV,
//        shadowElevation = SHADOW_ELEV,
//        color = MaterialTheme.colorScheme.surface.copy(alpha = SURFACE_ALPHA),
//        onClick = onClick,
//        enabled = enabled
//    ) {
//        TopBarIconContent(
//            icon = icon, modifier = Modifier
//                .size(ICON_SIZE)
//                .padding(CashioSpacing.small)
//        )
//    }
//}
//
//@Composable
//private fun DateTitle(date: LocalDate, contentColor: Color, icon: TopBarIcon) {
//    // Recompute only when date changes — not on every recomposition.
//    val dateText = remember(date) {
//        val formatter = DateTimeFormatter.ofPattern("dd MMMM, yyyy", Locale.getDefault())
//        date.format(formatter)
//    }
//
//    Row(
//        modifier = Modifier.padding(
//            PaddingValues(
//                horizontal = CashioSpacing.medium,
//                vertical = CashioSpacing.tiny
//            )
//        ),
//        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.small),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        TopBarIconContent(
//            icon = icon,
//            modifier = Modifier.size(ICON_SIZE),
//            tint = MaterialTheme.colorScheme.primary
//        )
//        Text(
//            text = dateText,
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.SemiBold,
//            color = contentColor
//        )
//    }
//}
//
///**
// * Renders the icon content for both pill buttons and date titles,
// * eliminating the duplicated when-block that existed in every call site.
// */
//@Composable
//private fun TopBarIconContent(
//    icon: TopBarIcon,
//    modifier: Modifier = Modifier,
//    tint: Color = MaterialTheme.colorScheme.onSurface
//) {
//    when (icon) {
//        is TopBarIcon.Vector -> Icon(
//            imageVector = icon.imageVector,
//            contentDescription = null,
//            tint = tint,
//            modifier = modifier
//        )
//
//        is TopBarIcon.Drawable -> Icon(
//            painter = painterResource(id = icon.resId),
//            contentDescription = null,
//            tint = tint,
//            modifier = modifier
//        )
//    }
//}

package com.bluemix.cashio.ui.defaults

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
import com.bluemix.cashio.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/* ══════════════════════════════════════════════════════════════════════════════
 *  Cashio Design Tokens — Top Bar
 *
 *  Modern-Flat top bar: zero shadow on icon pills, border-stroke instead.
 *  Consumes the loose-grid spacing tokens.
 * ══════════════════════════════════════════════════════════════════════════════ */

/* -------------------------------------------------------------------------- */
/*  Models                                                                     */
/* -------------------------------------------------------------------------- */

sealed interface CashioTopBarTitle {
    data class Text(val text: String) : CashioTopBarTitle
    data class Date(
        val date: LocalDate = LocalDate.now(),
        val icon: TopBarIcon = TopBarIcon.Drawable(R.drawable.calendar)
    ) : CashioTopBarTitle
}

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
/*  Top Bar                                                                    */
/* -------------------------------------------------------------------------- */

private val ICON_SIZE = 20.dp         // slightly smaller for a refined feel
private val BUTTON_SIZE = 36.dp       // compact pill

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
                Box(modifier = Modifier.padding(start = CashioSpacing.xs)) {
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
                is CashioTopBarTitle.Text -> Text(
                    text = title.text,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                is CashioTopBarTitle.Date -> DateTitle(
                    date = title.date,
                    contentColor = contentColor,
                    icon = title.icon
                )
            }
        },
        actions = {
            trailingAction?.let { action ->
                Box(modifier = Modifier.padding(end = CashioSpacing.xs)) {
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
/*  Private building blocks                                                    */
/* -------------------------------------------------------------------------- */

/**
 * Flat circular icon button — no shadow, just a faint fill + border stroke.
 */
@Composable
private fun TopBarIconPill(icon: TopBarIcon, onClick: () -> Unit, enabled: Boolean) {
    Surface(
        modifier = Modifier.size(BUTTON_SIZE),
        shape = CircleShape,
        tonalElevation = 0.dp,                                   // flat
        shadowElevation = 0.dp,                                   // flat
        color = MaterialTheme.colorScheme.surfaceContainerLow,   // faint fill
        border = CashioBorder.stroke(),                          // signature border
        onClick = onClick,
        enabled = enabled
    ) {
        Box(contentAlignment = Alignment.Center) {
            TopBarIconContent(icon = icon, modifier = Modifier.size(ICON_SIZE))
        }
    }
}

@Composable
private fun DateTitle(date: LocalDate, contentColor: Color, icon: TopBarIcon) {
    val dateText = remember(date) {
        val formatter = DateTimeFormatter.ofPattern("dd MMMM, yyyy", Locale.getDefault())
        date.format(formatter)
    }

    Row(
        modifier = Modifier.padding(
            PaddingValues(
                horizontal = CashioSpacing.sm,    // 12 dp
                vertical = CashioSpacing.xxs      // 2 dp
            )
        ),
        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.xs),  // 6 dp
        verticalAlignment = Alignment.CenterVertically
    ) {
        TopBarIconContent(
            icon = icon,
            modifier = Modifier.size(ICON_SIZE),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
private fun TopBarIconContent(
    icon: TopBarIcon,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    when (icon) {
        is TopBarIcon.Vector -> Icon(
            imageVector = icon.imageVector,
            contentDescription = null,
            tint = tint,
            modifier = modifier
        )
        is TopBarIcon.Drawable -> Icon(
            painter = painterResource(id = icon.resId),
            contentDescription = null,
            tint = tint,
            modifier = modifier
        )
    }
}