package com.bluemix.cashio.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A unified abstraction for navigation icons.
 *
 * This allows the UI to consume icons agnostically, supporting both
 * Compose [ImageVector] and legacy Android [DrawableRes] resources.
 */
sealed interface NavIcon {
    data class Vector(val imageVector: ImageVector) : NavIcon
    data class Drawable(@DrawableRes val resId: Int) : NavIcon
}

/**
 * Represents an item in the Bottom Navigation Bar.
 *
 * @property title The label displayed under the icon.
 * @property selectedIcon The filled/bold icon shown when the item is active.
 * @property unselectedIcon The outlined icon shown when the item is inactive.
 * @property route The Type-Safe [Route] destination associated with this item.
 */
data class BottomNavItem(
    val title: String,
    val selectedIcon: NavIcon,
    val unselectedIcon: NavIcon,
    val route: Route
)