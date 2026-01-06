package com.bluemix.cashio.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Defines the Type-Safe Navigation Graph for the application.
 *
 * Each object or class represents a distinct screen (destination).
 * Arguments are defined as properties in the data classes and handled automatically
 * by Kotlinx Serialization.
 */
sealed interface Route {

    // --- Startup Flow ---

    /**
     * The initial onboarding screen for new users.
     */
    @Serializable
    data object Onboarding : Route

    /**
     * The splash screen that handles database seeding and initialization.
     */
    @Serializable
    data object Splash : Route

    // --- Main Bottom Navigation Tabs ---

    @Serializable
    data object Dashboard : Route

    @Serializable
    data object Transactions : Route

    @Serializable
    data object History : Route

    @Serializable
    data object Analytics : Route

    @Serializable
    data object Settings : Route

    // --- Feature Screens ---

    /**
     * Screen for adding or editing an expense.
     * @param expenseId If null, creates a new expense. If provided, edits the existing one.
     */
    @Serializable
    data class AddExpense(val expenseId: String? = null) : Route

    /**
     * Detailed view of a specific transaction.
     * @param transactionId The unique ID of the transaction to display.
     */
    @Serializable
    data class TransactionDetails(val transactionId: String) : Route

    // --- Settings Sub-screens ---

    @Serializable
    data object Categories : Route

    @Serializable
    data object KeywordMapping : Route
}