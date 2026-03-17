package com.bluemix.cashio.domain.repository

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.DateRange
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.FinancialStats
import com.bluemix.cashio.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Contract for expense persistence and aggregation operations.
 *
 * ## Reactive vs one-shot
 * [observe*] functions return [Flow] and stay live — use them for screens that
 * must react to background changes (SMS import, notification capture).
 * [get*] functions are one-shot snapshots — use them for calculations or
 * detail screens where real-time updates are not needed.
 */
interface ExpenseRepository {

    // ── Reactive observations ──────────────────────────────────────────────

    /** Live stream of all expenses, newest first. */
    fun observeExpenses(): Flow<List<Expense>>

    /** Live stream of expenses within [startDate]..[endDate], newest first. */
    fun observeExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Expense>>

    // ── One-shot queries ───────────────────────────────────────────────────

    /** All expenses, newest first. */
    suspend fun getAllExpenses(): Result<List<Expense>>

    /** Single expense by [id], or `null` if not found. */
    suspend fun getExpenseById(id: String): Result<Expense?>

    /** Expenses within [startDate]..[endDate], newest first. */
    suspend fun getExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<List<Expense>>

    /** Expenses of a given [categoryId], newest first. */
    suspend fun getExpensesByCategory(categoryId: String): Result<List<Expense>>

    /** Expenses of a given [transactionType] within [startDate]..[endDate], newest first. */
    suspend fun getExpensesByType(
        transactionType: TransactionType,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<List<Expense>>

    // ── Mutations ──────────────────────────────────────────────────────────

    /**
     * Persist a new expense. Silently no-ops if an expense with the same [Expense.id]
     * already exists — guarantees idempotent SMS/notification imports.
     */
    suspend fun addExpense(expense: Expense): Result<Unit>

    /** Update all mutable fields of an existing expense. */
    suspend fun updateExpense(expense: Expense): Result<Unit>

    /** Delete a single expense by [expenseId]. */
    suspend fun deleteExpense(expenseId: String): Result<Unit>

    /** Delete multiple expenses in a single database transaction. */
    suspend fun deleteExpenses(expenseIds: List<String>): Result<Unit>

    // ── Aggregations ───────────────────────────────────────────────────────

    /**
     * Compute [FinancialStats] for the given [dateRange].
     *
     * [DateRange.CUSTOM] is not supported here — callers must use
     * [getExpensesByDateRange] with explicit dates and compute stats manually,
     * or use [getFinancialStatsByDates].
     */
    suspend fun getFinancialStats(dateRange: DateRange): Result<FinancialStats>

    /**
     * Compute [FinancialStats] for an explicit date window.
     * Used when [DateRange.CUSTOM] is selected by the user.
     */
    suspend fun getFinancialStatsByDates(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<FinancialStats>

    // ── SMS / Notification sync ────────────────────────────────────────────

    /**
     * Parse recent bank SMS messages and persist any new transactions.
     * Returns the count of newly added expenses.
     */
    suspend fun refreshExpensesFromSms(): Result<Int>

    /**
     * Re-apply the current keyword mapping rules to all non-manual expenses
     * whose merchant name or title contains [keyword].
     * Returns the count of updated expenses.
     */
    suspend fun recategorizeExpensesByKeyword(keyword: String): Result<Int>
}