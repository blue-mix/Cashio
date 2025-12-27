package com.bluemix.cashio.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bluemix.cashio.R
import com.bluemix.cashio.presentation.add.AddExpenseScreen
import com.bluemix.cashio.presentation.analytics.ui.AnalyticsScreen
import com.bluemix.cashio.presentation.categories.CategoriesScreen
import com.bluemix.cashio.presentation.common.PlaceholderScreen
import com.bluemix.cashio.presentation.history.HistoryScreen
import com.bluemix.cashio.presentation.home.DashboardScreen
import com.bluemix.cashio.presentation.keyword.KeywordMappingScreen
import com.bluemix.cashio.presentation.settings.ui.SettingsScreen
import com.bluemix.cashio.presentation.splash.SplashScreen
import com.bluemix.cashio.presentation.transaction.TransactionDetailsScreen
import com.bluemix.cashio.presentation.transaction.TransactionsScreen

private object NavUi {
    val FabSize = 56.dp
    val FabOverlapOffset = 45.dp
    val FabGapWidth = FabSize
    val BottomBarShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
    const val BottomBarAnimMs = 300
    const val FabAnimMs = 200
}

@Composable
fun CashioNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val showBottomBar = currentDestination.shouldShowBottomBar()
    val showFab = currentDestination.shouldShowFab()

    val bottomNavItems = listOf(
        BottomNavItem(
            title = "Home",
            selectedIcon = NavIcon.Drawable(R.drawable.homeselected),
            unselectedIcon = NavIcon.Drawable(R.drawable.homeunselected),
            route = Route.Dashboard
        ),
        BottomNavItem(
            title = "History",
            selectedIcon = NavIcon.Drawable(R.drawable.historyselected),
            unselectedIcon = NavIcon.Drawable(R.drawable.historyunselected),
            route = Route.History
        ),
        BottomNavItem(
            title = "Analytics",
            selectedIcon = NavIcon.Drawable(R.drawable.graphselected),
            unselectedIcon = NavIcon.Drawable(R.drawable.graphunselected),
            route = Route.Analytics
        ),
        BottomNavItem(
            title = "Settings",
            selectedIcon = NavIcon.Drawable(R.drawable.settingsselected),
            unselectedIcon = NavIcon.Drawable(R.drawable.settingsunselected),
            route = Route.Settings
        )
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(NavUi.BottomBarAnimMs)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(NavUi.BottomBarAnimMs)
                )
            ) {
                BottomBarWithFabGap(
                    navController = navController,
                    items = bottomNavItems
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = fadeIn(tween(NavUi.FabAnimMs)) +
                        slideInVertically { it / 2 },
                exit = fadeOut(tween(NavUi.FabAnimMs)) +
                        slideOutVertically { it / 2 }

            ) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Route.AddExpense(expenseId = null)) {
                            launchSingleTop = true
                        }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(NavUi.FabSize)
                        .offset(y = NavUi.FabOverlapOffset)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            NavHost(
                navController = navController,
                startDestination = Route.Onboarding
            ) {
                composable<Route.Onboarding> {
                    PlaceholderScreen(
                        title = "Onboarding",
                        onNavigate = {
                            navController.navigate(Route.Dashboard) {
                                popUpTo(Route.Onboarding) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<Route.Dashboard> {
                    DashboardScreen(
                        onNavigateToWallet = { navController.navigate(Route.History) },
                        onNavigateToAllTransactions = { navController.navigate(Route.Transactions) },
                        onNavigateToTransactionDetails = { id ->
                            navController.navigate(Route.TransactionDetails(id))
                        }
                    )
                }

                composable<Route.Transactions> {
                    TransactionsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onTransactionClick = { id ->
                            navController.navigate(Route.TransactionDetails(id))
                        }
                    )
                }

                composable<Route.TransactionDetails> { entry ->
                    val route = entry.toRoute<Route.TransactionDetails>()
                    TransactionDetailsScreen(
                        transactionId = route.transactionId,
                        onNavigateBack = { navController.popBackStack() },
                        onEditClick = { id ->
                            navController.navigate(Route.AddExpense(expenseId = id))
                        }
                    )
                }

                composable<Route.History> {
                    HistoryScreen(
                        onTransactionClick = { id ->
                            navController.navigate(Route.TransactionDetails(id))
                        }
                    )
                }

                composable<Route.AddExpense> { entry ->
                    val route = entry.toRoute<Route.AddExpense>()
                    AddExpenseScreen(
                        expenseId = route.expenseId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToCategories = { navController.navigate(Route.Categories) }
                    )
                }

                composable<Route.Categories> {
                    CategoriesScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable<Route.Analytics> {
                    AnalyticsScreen()
                }

                composable<Route.Settings> {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToKeywordMapping = { navController.navigate(Route.KeywordMapping) },
                        onNavigateToCategories = { navController.navigate(Route.Categories) }
                    )
                }

                composable<Route.KeywordMapping> {
                    KeywordMappingScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable<Route.Splash> {
                    SplashScreen(
                        onFinished = {
                            navController.navigate(Route.Dashboard) {
                                popUpTo(Route.Splash) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

            }
        }
    }
}

@Composable
private fun NavIconSlot(
    icon: NavIcon,
    contentDescription: String?,
) {
    when (icon) {
        is NavIcon.Vector -> Icon(
            imageVector = icon.imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )

        is NavIcon.Drawable -> Icon(
            painter = painterResource(id = icon.resId),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Bottom bar that leaves a center gap under the FAB.
 */
@Composable
private fun BottomBarWithFabGap(
    navController: NavHostController,
    items: List<BottomNavItem>,
) {
    Surface(
        shape = NavUi.BottomBarShape,
        // tonalElevation = 12.dp,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            items.take(2).forEach { item ->
                val isSelected = navController.currentDestinationMatches(item.route)
                val icon = if (isSelected) item.selectedIcon else item.unselectedIcon
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { navController.navigateBottom(item.route) }
,
                            icon = {
                        NavIconSlot(
                            icon = icon,
                            contentDescription = item.title
                        )
                    },
                    label = { Text(item.title) },
                    colors = navItemColors()
                )
            }

            Spacer(modifier = Modifier.width(NavUi.FabGapWidth))

            items.takeLast(2).forEach { item ->
                val isSelected = navController.currentDestinationMatches(item.route)
                val icon = if (isSelected) item.selectedIcon else item.unselectedIcon
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { navController.navigateBottom(item.route) }
,
                            icon = {
                        NavIconSlot(
                            icon = icon,
                            contentDescription = item.title
                        )
                    },
                    label = { Text(item.title) },
                    colors = navItemColors()
                )
            }
        }
    }
}
private fun NavHostController.navigateBottom(route: Route) {
    navigate(route) {
        popUpTo(Route.Dashboard) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
)

/**
 * Centralized route matching.
 *
 * For typed routes, the destination.route is typically the qualified name of the route class.
 * This keeps logic in one place, so if you change navigation libraries you only update this.
 */
private fun NavHostController.currentDestinationMatches(route: Route): Boolean {
    val target = route::class.qualifiedName ?: return false
    val destination = currentBackStackEntry?.destination ?: return false
    return destination.hierarchy.any { it.route == target }
}

/**
 * Visibility policy for bottom bar / FAB.
 * Keep this close to navigation, not scattered across screens.
 */
private val MAIN_DESTINATIONS = setOf(
    Route.Dashboard::class.qualifiedName,
    Route.History::class.qualifiedName,
    Route.Analytics::class.qualifiedName,
    Route.Settings::class.qualifiedName
)

private fun NavDestination?.isMainDestination(): Boolean =
    this?.route in MAIN_DESTINATIONS

private fun NavDestination?.shouldShowBottomBar() = isMainDestination()
private fun NavDestination?.shouldShowFab() = isMainDestination()
