package com.bluemix.cashio.core.navigation

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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bluemix.cashio.R
import com.bluemix.cashio.presentation.analytics.AnalyticsScreen
import com.bluemix.cashio.presentation.categories.CategoriesScreen
import com.bluemix.cashio.presentation.common.PlaceholderScreen
import com.bluemix.cashio.presentation.history.HistoryScreen
import com.bluemix.cashio.presentation.home.DashboardScreen
import com.bluemix.cashio.presentation.keywordmapping.KeywordMappingScreen
import com.bluemix.cashio.presentation.settings.SettingsScreen
import com.bluemix.cashio.presentation.transaction.AddExpenseScreen
import com.bluemix.cashio.presentation.transactiondetails.TransactionDetailsScreen
import com.bluemix.cashio.presentation.transactions.TransactionsScreen

@Composable
fun CashioNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    val showBottomBar = backStackEntry.shouldShowBottomBar()
    val showFab = backStackEntry.shouldShowFab()

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
            route = Route.Expenses
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
        // Important: don’t force navigationBarsPadding() globally.
        // Scaffold will give proper content padding to avoid overlaps when needed.
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300))
            ) {
                BottomBarWithFabCutSpace(
                    navController = navController,
                    items = bottomNavItems
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
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
                        .size(56.dp)
                        .offset(y = (45).dp) // adjust for how much overlap you want
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
            }
        },
        // Make FAB overlap the bottom bar like your design
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // This padding is KEY: it prevents content going under bottom bar
                // and handles system insets correctly.
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = Route.Dashboard
            ) {
                composable<Route.Onboarding> {
                    PlaceholderScreen(
                        title = "Onboarding",
                        onNavigate = {
                            navController.navigate(Route.Dashboard) {
                                popUpTo(Route.Onboarding) {
                                    inclusive = true
                                } // ✅ remove onboarding
                                launchSingleTop = true
                            }
                        }
                    )
                }


                composable<Route.Dashboard> {
                    DashboardScreen(
                        onNavigateToWallet = { navController.navigate(Route.Expenses) },
                        onNavigateToAllTransactions = { navController.navigate(Route.Transactions) },
                        onNavigateToTransactionDetails = { id ->
                            navController.navigate(Route.TransactionDetails(id))
                        }
                    )
                }
                // New full transactions screen (explicit)
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
                composable<Route.Expenses> {
                    HistoryScreen(onTransactionClick = {id ->
                        navController.navigate(Route.TransactionDetails(id)) })
                }

                composable<Route.AddExpense> { entry ->
                    val route = entry.toRoute<Route.AddExpense>()

                    AddExpenseScreen(
                        expenseId = route.expenseId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateCategory = { navController.navigate(Route.Categories) }
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
                        onNavigateToKeywordMapping = { navController.navigate(Route.KeywordMapping) }
                    )
                }

                composable<Route.KeywordMapping> {
                    KeywordMappingScreen(onNavigateBack = { navController.popBackStack() })
                }


            }
        }
    }
}


@Composable
private fun NavIconSlot(
    icon: NavIcon,
    contentDescription: String?
) {
    when (icon) {
        is NavIcon.Vector -> {
            Icon(
                imageVector = icon.imageVector,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
        }
        is NavIcon.Drawable -> {
            Icon(
                painter = painterResource(id = icon.resId),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Bottom bar that leaves a center gap under the FAB (no cutout, just spacing).
 */
@Composable
private fun BottomBarWithFabCutSpace(
    navController: androidx.navigation.NavHostController,
    items: List<BottomNavItem>
) {
    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 12.dp,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            // left 2
            items.take(2).forEach { item ->
                val isSelected = navController.currentDestinationMatches(item.route)
                val icon = if (isSelected) item.selectedIcon else item.unselectedIcon

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(Route.Dashboard) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
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

            Spacer(modifier = Modifier.width(56.dp))

            // right 2
            items.takeLast(2).forEach { item ->
                val isSelected = navController.currentDestinationMatches(item.route)
                val icon = if (isSelected) item.selectedIcon else item.unselectedIcon

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(Route.Dashboard) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
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


@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
)

private fun androidx.navigation.NavHostController.currentDestinationMatches(route: Route): Boolean {
    val target = route::class.qualifiedName
    return currentBackStackEntry?.destination
        ?.hierarchy
        ?.any { it.route == target } == true
}
