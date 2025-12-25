package com.bluemix.cashio.domain.repository

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.DateRange
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.FinancialStats
import com.bluemix.cashio.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Repository interface for Expense operations
 */
interface ExpenseRepository {

    /**
     * Observe all expenses as Flow
     */
    fun observeExpenses(): Flow<List<Expense>>

    /**
     * Observe expenses within a date range
     */
    fun observeExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Expense>>

    /**
     * Get all expenses (one-time)
     */
    suspend fun getAllExpenses(): Result<List<Expense>>

    /**
     * Get expense by ID
     */
    suspend fun getExpenseById(id: String): Result<Expense?>

    /**
     * Get expenses by date range
     */
    suspend fun getExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<List<Expense>>

    /**
     * Get expenses by category
     */
    suspend fun getExpensesByCategory(categoryId: String): Result<List<Expense>>

    /**
     * Add new expense
     */
    suspend fun addExpense(expense: Expense): Result<Unit>

    /**
     * Update existing expense
     */
    suspend fun updateExpense(expense: Expense): Result<Unit>

    /**
     * Delete expense
     */
    suspend fun deleteExpense(expenseId: String): Result<Unit>

    /**
     * Delete multiple expenses
     */
    suspend fun deleteExpenses(expenseIds: List<String>): Result<Unit>

    /**
     * Get financial statistics for a date range
     */
    suspend fun getFinancialStats(dateRange: DateRange): Result<FinancialStats>

    /**
     * Refresh expenses from SMS (parse recent SMS)
     */
    suspend fun refreshExpensesFromSms(): Result<Int>  // Returns count of new expenses

    suspend fun getExpensesByType(
        transactionType: TransactionType,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<List<Expense>>

    /**
     * Re-apply keyword mappings to existing expenses that match a keyword.
     * Returns how many expenses were updated.
     */
    suspend fun recategorizeExpensesByKeyword(keyword: String): Result<Int>

}
