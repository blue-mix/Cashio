package com.bluemix.cashio.core.navigation

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface NavIcon {
    data class Vector(val imageVector: ImageVector) : NavIcon
    data class Drawable(@DrawableRes val resId: Int) : NavIcon
}

data class BottomNavItem(
    val title: String,
    val selectedIcon: NavIcon,
    val unselectedIcon: NavIcon,
    val route: Route
)
