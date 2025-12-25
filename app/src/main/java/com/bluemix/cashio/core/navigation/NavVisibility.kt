package com.bluemix.cashio.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry

@Composable
fun NavBackStackEntry?.shouldShowBottomBar(): Boolean {
    return remember(this) {
        derivedStateOf {
            when (this@shouldShowBottomBar?.destination?.route) {
                Route.Dashboard::class.qualifiedName,
                Route.Expenses::class.qualifiedName,
                Route.Analytics::class.qualifiedName,
                Route.Settings::class.qualifiedName -> true

                else -> false
            }
        }
    }.value
}

/**
 * Decide FAB visibility. I’m setting it to show on bottom-bar destinations
 * EXCEPT Settings (common pattern), and obviously not on AddExpense / Onboarding.
 */
@Composable
fun NavBackStackEntry?.shouldShowFab(): Boolean {
    return remember(this) {
        derivedStateOf {
            when (this@shouldShowFab?.destination?.route) {
                Route.Dashboard::class.qualifiedName,
                Route.Expenses::class.qualifiedName,
                Route.Analytics::class.qualifiedName,
                Route.Settings::class.qualifiedName -> true

                else -> false
            }
        }
    }.value
}
