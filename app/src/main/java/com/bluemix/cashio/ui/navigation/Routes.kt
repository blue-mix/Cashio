package com.bluemix.cashio.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Onboarding : Route

    @Serializable
    data object Dashboard : Route

    // Main list screen for all transactions (new)
    @Serializable
    data object Transactions : Route

    // Existing (you used "Expenses" for HistoryScreen earlier)
    @Serializable
    data object History : Route

    // Add transaction. Optional expenseId enables edit mode.
    @Serializable
    data class AddExpense(val expenseId: String?=null) : Route

    // Details screen for a transaction/expense
    @Serializable
    data class TransactionDetails(val transactionId: String) : Route

    @Serializable
    data object Analytics : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Categories : Route

    @Serializable
    data object KeywordMapping : Route

    @Serializable
    data object Splash : Route

    // TODO(nav): enable when Spending screen ships
//    @Serializable
//    data object Spending : Route


}
