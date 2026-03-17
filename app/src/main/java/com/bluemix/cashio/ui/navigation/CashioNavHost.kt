package com.bluemix.cashio.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bluemix.cashio.R
import com.bluemix.cashio.presentation.add.AddExpenseScreen
import com.bluemix.cashio.presentation.analytics.ui.AnalyticsScreen
import com.bluemix.cashio.presentation.categories.CategoriesScreen
import com.bluemix.cashio.presentation.history.HistoryScreen
import com.bluemix.cashio.presentation.home.DashboardScreen
import com.bluemix.cashio.presentation.keyword.KeywordMappingScreen
import com.bluemix.cashio.presentation.onboarding.OnboardingScreen
import com.bluemix.cashio.presentation.settings.ui.SettingsScreen
import com.bluemix.cashio.presentation.transaction.TransactionDetailsScreen
import com.bluemix.cashio.presentation.transaction.TransactionsScreen

/**
 * Navigation UI constants - centralized for consistency
 */
object NavUi {
    val FabSize = 56.dp
    val FabOverlapOffset = 45.dp
    val FabGapWidth = FabSize
    val BottomBarShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
    val BottomBarElevation = 12.dp

    const val BottomBarAnimMs = 300
    const val FabAnimMs = 200
    const val TabTransitionMs = 300
    const val DrillTransitionMs = 300
    const val DrillFadeMs = 200

    val MainRouteClasses = setOf(
        Route.Dashboard::class,
        Route.History::class,
        Route.Analytics::class,
        Route.Settings::class
    )
}

/**
 * Main navigation destinations that show bottom bar and FAB
 */
private val MAIN_DESTINATIONS = setOf(
    Route.Dashboard::class.qualifiedName,
    Route.History::class.qualifiedName,
    Route.Analytics::class.qualifiedName,
    Route.Settings::class.qualifiedName
)

/**
 * The Root Navigation Graph container.
 *
 * Manages:
 * 1. Global Scaffold (Bottom Bar, FAB)
 * 2. Visibility animations for system UI elements
 * 3. Navigation graph with type-safe routes
 * 4. Transition animations (tab switching vs drill-down)
 *
 * @param startDestination Initial screen (Onboarding for new users, Dashboard for returning)
 */
@Composable
fun CashioNavHost(startDestination: Route) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // ✅ FIXED: Use pre-computed set instead of .map()
    val showBottomBar = remember(currentDestination) {
        currentDestination.isMainDestination()
    }
    val showFab = remember(currentDestination) {
        currentDestination.isMainDestination()
    }

    val bottomNavItems = remember {
        listOf(
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
    }

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
                        .semantics { contentDescription = "Add new transaction" }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = {
                    if (isTabTransition(
                            initialState.destination.route,
                            targetState.destination.route
                        )
                    ) {
                        fadeIn(animationSpec = tween(NavUi.TabTransitionMs))
                    } else {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(NavUi.DrillTransitionMs)
                        )
                    }
                },
                exitTransition = {
                    if (isTabTransition(
                            initialState.destination.route,
                            targetState.destination.route
                        )
                    ) {
                        fadeOut(animationSpec = tween(NavUi.TabTransitionMs))
                    } else {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(NavUi.DrillTransitionMs)
                        ) + fadeOut(tween(NavUi.DrillFadeMs))
                    }
                },
                popEnterTransition = {
                    if (isTabTransition(
                            initialState.destination.route,
                            targetState.destination.route
                        )
                    ) {
                        fadeIn(animationSpec = tween(NavUi.TabTransitionMs))
                    } else {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(NavUi.DrillTransitionMs)
                        )
                    }
                },
                popExitTransition = {
                    if (isTabTransition(
                            initialState.destination.route,
                            targetState.destination.route
                        )
                    ) {
                        fadeOut(animationSpec = tween(NavUi.TabTransitionMs))
                    } else {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(NavUi.DrillTransitionMs)
                        )
                    }
                }
            ) {
                // Startup Flow
                composable<Route.Onboarding> {
                    OnboardingScreen(
                        onNavigate = {
                            navController.navigate(Route.Dashboard) {
                                popUpTo(Route.Onboarding) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // Main Tabs
                composable<Route.Dashboard> {
                    DashboardScreen(
                        onNavigateToWallet = { navController.navigate(Route.History) },
                        onNavigateToAllTransactions = { navController.navigate(Route.Transactions) },
                        onNavigateToTransactionDetails = { id ->
                            navController.navigate(Route.TransactionDetails(id))
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

                composable<Route.Analytics> {
                    AnalyticsScreen()
                }

                composable<Route.Settings> {
                    SettingsScreen(
                        onNavigateToKeywordMapping = {
                            navController.navigate(Route.KeywordMapping)
                        },
                        onNavigateToCategories = {
                            navController.navigate(Route.Categories)
                        }
                    )
                }

                // Feature Screens
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

                composable<Route.AddExpense> { entry ->
                    val route = entry.toRoute<Route.AddExpense>()
                    AddExpenseScreen(
                        expenseId = route.expenseId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToCategories = {
                            navController.navigate(Route.Categories)
                        }
                    )
                }

                composable<Route.Categories> {
                    CategoriesScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<Route.KeywordMapping> {
                    KeywordMappingScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

/**
 * Determines if navigation is between tabs (for animation selection)
 */
private fun isTabTransition(fromRoute: String?, toRoute: String?): Boolean {
    return MAIN_DESTINATIONS.contains(fromRoute) && MAIN_DESTINATIONS.contains(toRoute)
}

/**
 * Checks if destination is a main tab destination
 */
private fun NavDestination?.isMainDestination(): Boolean =
    this?.route in MAIN_DESTINATIONS

//private object NavUi {
//    val FabSize = 56.dp
//    val FabOverlapOffset = 45.dp
//    val FabGapWidth = FabSize
//    val BottomBarShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
//    val BottomBarElevation = 12.dp
//
//    // Animation timings
//    const val BottomBarAnimMs = 300
//    const val FabAnimMs = 200
//    const val TabTransitionMs = 300
//    const val DrillTransitionMs = 300
//    const val DrillFadeMs = 200
//
//    val MainRouteClasses = setOf(
//        Route.Dashboard::class,
//        Route.History::class,
//        Route.Analytics::class,
//        Route.Settings::class
//    )
//}
//
//@Composable
//fun CashioNavHost(
//    startDestination: Route
//) {
//    val navController = rememberNavController()
//    val backStackEntry by navController.currentBackStackEntryAsState()
//    val currentDestination = backStackEntry?.destination
//
//    val showBottomBar = currentDestination.shouldShowBottomBar()
//    val showFab = currentDestination.shouldShowFab()
//
//    val bottomNavItems = remember {
//        listOf(
//            BottomNavItem(
//                title = "Home",
//                selectedIcon = NavIcon.Drawable(R.drawable.homeselected),
//                unselectedIcon = NavIcon.Drawable(R.drawable.homeunselected),
//                route = Route.Dashboard
//            ),
//            BottomNavItem(
//                title = "History",
//                selectedIcon = NavIcon.Drawable(R.drawable.historyselected),
//                unselectedIcon = NavIcon.Drawable(R.drawable.historyunselected),
//                route = Route.History
//            ),
//            BottomNavItem(
//                title = "Analytics",
//                selectedIcon = NavIcon.Drawable(R.drawable.graphselected),
//                unselectedIcon = NavIcon.Drawable(R.drawable.graphunselected),
//                route = Route.Analytics
//            ),
//            BottomNavItem(
//                title = "Settings",
//                selectedIcon = NavIcon.Drawable(R.drawable.settingsselected),
//                unselectedIcon = NavIcon.Drawable(R.drawable.settingsunselected),
//                route = Route.Settings
//            )
//        )
//    }
//
//    Scaffold(
//        contentWindowInsets = WindowInsets(0),
//        bottomBar = {
//            AnimatedVisibility(
//                visible = showBottomBar,
//                enter = slideInVertically(
//                    initialOffsetY = { it },
//                    animationSpec = tween(NavUi.BottomBarAnimMs)
//                ),
//                exit = slideOutVertically(
//                    targetOffsetY = { it },
//                    animationSpec = tween(NavUi.BottomBarAnimMs)
//                )
//            ) {
//                BottomBarWithFabGap(
//                    navController = navController,
//                    items = bottomNavItems
//                )
//            }
//        },
//        floatingActionButton = {
//            AnimatedVisibility(
//                visible = showFab,
//                enter = fadeIn(tween(NavUi.FabAnimMs)) +
//                        slideInVertically { it / 2 },
//                exit = fadeOut(tween(NavUi.FabAnimMs)) +
//                        slideOutVertically { it / 2 }
//            ) {
//                FloatingActionButton(
//                    onClick = {
//                        navController.navigate(Route.AddExpense(expenseId = null)) {
//                            launchSingleTop = true
//                        }
//                    },
//                    shape = CircleShape,
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    contentColor = MaterialTheme.colorScheme.onPrimary,
//                    modifier = Modifier
//                        .size(NavUi.FabSize)
//                        .offset(y = NavUi.FabOverlapOffset)
//                        .semantics { contentDescription = "Add new transaction" }
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Add,
//                        contentDescription = null
//                    )
//                }
//            }
//        },
//        floatingActionButtonPosition = FabPosition.Center
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            NavHost(
//                navController = navController,
//                startDestination = startDestination,
//                enterTransition = {
//                    if (isTabTransition(
//                            initialState.destination.route,
//                            targetState.destination.route
//                        )
//                    ) {
//                        fadeIn(animationSpec = tween(NavUi.TabTransitionMs))
//                    } else {
//                        slideIntoContainer(
//                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
//                            animationSpec = tween(NavUi.DrillTransitionMs)
//                        )
//                    }
//                },
//                exitTransition = {
//                    if (isTabTransition(
//                            initialState.destination.route,
//                            targetState.destination.route
//                        )
//                    ) {
//                        fadeOut(animationSpec = tween(NavUi.TabTransitionMs))
//                    } else {
//                        slideOutOfContainer(
//                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
//                            animationSpec = tween(NavUi.DrillTransitionMs)
//                        ) + fadeOut(tween(NavUi.DrillFadeMs))
//                    }
//                },
//                popEnterTransition = {
//                    if (isTabTransition(
//                            initialState.destination.route,
//                            targetState.destination.route
//                        )
//                    ) {
//                        fadeIn(animationSpec = tween(NavUi.TabTransitionMs))
//                    } else {
//                        slideIntoContainer(
//                            towards = AnimatedContentTransitionScope.SlideDirection.End,
//                            animationSpec = tween(NavUi.DrillTransitionMs)
//                        )
//                    }
//                },
//                popExitTransition = {
//                    if (isTabTransition(
//                            initialState.destination.route,
//                            targetState.destination.route
//                        )
//                    ) {
//                        fadeOut(animationSpec = tween(NavUi.TabTransitionMs))
//                    } else {
//                        slideOutOfContainer(
//                            towards = AnimatedContentTransitionScope.SlideDirection.End,
//                            animationSpec = tween(NavUi.DrillTransitionMs)
//                        )
//                    }
//                }
//            ) {
//                // Startup Flow
//                composable<Route.Onboarding> {
//                    OnboardingScreen(
//                        onNavigate = {
//                            navController.navigate(Route.Dashboard) {
//                                popUpTo(Route.Onboarding) { inclusive = true }
//                                launchSingleTop = true
//                            }
//                        }
//                    )
//                }
//
//                // Main Tabs
//                composable<Route.Dashboard> {
//                    DashboardScreen(
//                        onNavigateToWallet = { navController.navigate(Route.History) },
//                        onNavigateToAllTransactions = { navController.navigate(Route.Transactions) },
//                        onNavigateToTransactionDetails = { id ->
//                            navController.navigate(Route.TransactionDetails(id))
//                        }
//                    )
//                }
//
//                composable<Route.History> {
//                    HistoryScreen(
//                        onTransactionClick = { id ->
//                            navController.navigate(Route.TransactionDetails(id))
//                        }
//                    )
//                }
//
//                composable<Route.Analytics> {
//                    AnalyticsScreen()
//                }
//
//                composable<Route.Settings> {
//                    SettingsScreen(
//                        onNavigateToKeywordMapping = {
//                            navController.navigate(Route.KeywordMapping)
//                        },
//                        onNavigateToCategories = {
//                            navController.navigate(Route.Categories)
//                        }
//                    )
//                }
//
//                // Feature Screens
//                composable<Route.Transactions> {
//                    TransactionsScreen(
//                        onNavigateBack = { navController.popBackStack() },
//                        onTransactionClick = { id ->
//                            navController.navigate(Route.TransactionDetails(id))
//                        }
//                    )
//                }
//
//                composable<Route.TransactionDetails> { entry ->
//                    val route = entry.toRoute<Route.TransactionDetails>()
//                    TransactionDetailsScreen(
//                        transactionId = route.transactionId,
//                        onNavigateBack = { navController.popBackStack() },
//                        onEditClick = { id ->
//                            navController.navigate(Route.AddExpense(expenseId = id))
//                        }
//                    )
//                }
//
//                composable<Route.AddExpense> { entry ->
//                    val route = entry.toRoute<Route.AddExpense>()
//                    AddExpenseScreen(
//                        expenseId = route.expenseId,
//                        onNavigateBack = { navController.popBackStack() },
//                        onNavigateToCategories = {
//                            navController.navigate(Route.Categories)
//                        }
//                    )
//                }
//
//                composable<Route.Categories> {
//                    CategoriesScreen(
//                        onNavigateBack = { navController.popBackStack() }
//                    )
//                }
//
//                composable<Route.KeywordMapping> {
//                    KeywordMappingScreen(
//                        onNavigateBack = { navController.popBackStack() }
//                    )
//                }
//            }
//        }
//    }
//}
//
///**
// * Renders navigation icons - supports both Vector and Drawable resources
// */
//@Composable
//private fun NavIconSlot(
//    icon: NavIcon,
//    contentDescription: String?
//) {
//    when (icon) {
//        is NavIcon.Vector -> Icon(
//            imageVector = icon.imageVector,
//            contentDescription = contentDescription,
//            modifier = Modifier.size(24.dp)
//        )
//
//        is NavIcon.Drawable -> Icon(
//            painter = painterResource(id = icon.resId),
//            contentDescription = contentDescription,
//            modifier = Modifier.size(24.dp)
//        )
//    }
//}
//
//@Composable
//private fun BottomBarWithFabGap(
//    navController: NavHostController,
//    items: List<BottomNavItem>
//) {
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentDestination = navBackStackEntry?.destination
//
//    val currentRoute = remember(currentDestination) {
//        currentDestination.getCurrentMainRoute()
//    }
//
//    Surface(
//        shape = NavUi.BottomBarShape,
//        shadowElevation = NavUi.BottomBarElevation,
//        color = MaterialTheme.colorScheme.surface,
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        NavigationBar(
//            containerColor = Color.Transparent,
//            tonalElevation = 0.dp
//        ) {
//            items.take(2).forEach { item ->
//                BottomNavItemSlot(
//                    item = item,
//                    isSelected = currentRoute == item.route::class,
//                    onNavigate = { navController.navigateBottom(item.route) }
//                )
//            }
//
//            Spacer(modifier = Modifier.width(NavUi.FabGapWidth))
//
//            items.takeLast(2).forEach { item ->
//                BottomNavItemSlot(
//                    item = item,
//                    isSelected = currentRoute == item.route::class,
//                    onNavigate = { navController.navigateBottom(item.route) }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun RowScope.BottomNavItemSlot(
//    item: BottomNavItem,
//    isSelected: Boolean,
//    onNavigate: () -> Unit
//) {
//    val icon = if (isSelected) item.selectedIcon else item.unselectedIcon
//
//    NavigationBarItem(
//        selected = isSelected,
//        onClick = onNavigate,
//        icon = {
//            NavIconSlot(
//                icon = icon,
//                contentDescription = item.title
//            )
//        },
//        label = { Text(item.title) },
//        colors = NavigationBarItemDefaults.colors(
//            selectedIconColor = MaterialTheme.colorScheme.primary,
//            selectedTextColor = MaterialTheme.colorScheme.primary,
//            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
//            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
//            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
//        )
//    )
//}
//
//// Extension function for reusability
//private fun NavDestination?.getCurrentMainRoute(): KClass<out Route>? {
//    val routeName = this?.route ?: return null
//    return NavUi.MainRouteClasses.firstOrNull { it.qualifiedName == routeName }
//}
//
///**
// * Bottom navigation logic with state preservation
// */
//private fun NavHostController.navigateBottom(route: Route) {
//    navigate(route) {
//        popUpTo(Route.Dashboard) {
//            saveState = true
//        }
//        launchSingleTop = true
//        restoreState = true
//    }
//}
//
///**
// * Determines if navigation is between tabs (for animation selection)
// */
//private fun isTabTransition(fromRoute: String?, toRoute: String?): Boolean {
//    val tabs = setOf(
//        Route.Dashboard::class.qualifiedName,
//        Route.History::class.qualifiedName,
//        Route.Analytics::class.qualifiedName,
//        Route.Settings::class.qualifiedName
//    )
//    return tabs.contains(fromRoute) && tabs.contains(toRoute)
//}
//
//private fun NavDestination?.isMainDestination(): Boolean =
//    this?.route in MAIN_DESTINATIONS
//
//private fun NavDestination?.shouldShowBottomBar() = isMainDestination()
//private fun NavDestination?.shouldShowFab() = isMainDestination()