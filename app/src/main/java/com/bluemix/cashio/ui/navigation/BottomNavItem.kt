package com.bluemix.cashio.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlin.reflect.KClass

/**
 * UI Abstractions for Navigation Icons
 */
sealed interface NavIcon {
    data class Vector(val imageVector: ImageVector) : NavIcon
    data class Drawable(@DrawableRes val resId: Int) : NavIcon
}

/**
 * Represents an item in the Bottom Navigation Bar
 */
data class BottomNavItem(
    val title: String,
    val selectedIcon: NavIcon,
    val unselectedIcon: NavIcon,
    val route: Route
)

/**
 * Bottom navigation bar with center gap for FAB
 */
@Composable
fun BottomBarWithFabGap(
    navController: NavHostController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentRoute = remember(currentDestination) {
        currentDestination.getCurrentMainRoute()
    }

    Surface(
        shape = NavUi.BottomBarShape,
        shadowElevation = NavUi.BottomBarElevation,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            // ✅ FIXED: Explicit structure instead of forEachIndexed
            // First 2 items
            items.take(2).forEach { item ->
                BottomNavItemSlot(
                    item = item,
                    isSelected = currentRoute == item.route::class,
                    onNavigate = {
                        navController.navigate(item.route) {
                            popUpTo(Route.Dashboard) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // Gap for FAB
            Spacer(modifier = Modifier.width(NavUi.FabGapWidth))

            // Last 2 items
            items.drop(2).forEach { item ->
                BottomNavItemSlot(
                    item = item,
                    isSelected = currentRoute == item.route::class,
                    onNavigate = {
                        navController.navigate(item.route) {
                            popUpTo(Route.Dashboard) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

/**
 * Individual bottom navigation item
 */
@Composable
private fun RowScope.BottomNavItemSlot(
    item: BottomNavItem,
    isSelected: Boolean,
    onNavigate: () -> Unit
) {
    NavigationBarItem(
        selected = isSelected,
        onClick = onNavigate,
        icon = {
            NavIconSlot(
                icon = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.title
            )
        },
        label = { Text(item.title) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        )
    )
}

/**
 * Renders navigation icons - supports both Vector and Drawable
 */
@Composable
private fun NavIconSlot(
    icon: NavIcon,
    contentDescription: String?
) {
    when (icon) {
        is NavIcon.Vector -> Icon(
            imageVector = icon.imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
        is NavIcon.Drawable -> Icon(
            painter = painterResource(icon.resId),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Extension function to get current main route from destination
 */
private fun NavDestination?.getCurrentMainRoute(): KClass<out Route>? {
    val routeName = this?.route ?: return null
    return NavUi.MainRouteClasses.firstOrNull { it.qualifiedName == routeName }
}