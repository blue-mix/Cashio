package com.bluemix.cashio.domain.model

/**
 * Aggregated financial statistics for a given date range.
 *
 * All monetary values are in **paise** (smallest currency unit) consistent with [Expense.amountPaise].
 */
data class FinancialStats(
    val totalExpensesPaise: Long = 0L,
    val totalIncomePaise: Long = 0L,
    val expenseCount: Int = 0,
    val incomeCount: Int = 0,
    val averagePerDayPaise: Long = 0L,
    val topCategory: Category? = null,
    val topCategoryAmountPaise: Long = 0L,

    /**
     * Expense-only breakdown for the category pie/bar chart.
     * Values are in paise.
     */
    val categoryBreakdown: Map<Category, Long> = emptyMap()
) {
    /** Net balance = income − expenses, in paise. */
    val netPaise: Long get() = totalIncomePaise - totalExpensesPaise

    // ── Display helpers (use for formatting only, never arithmetic) ─────────
    val totalExpensesDouble: Double get() = totalExpensesPaise / 100.0
    val totalIncomeDouble: Double get() = totalIncomePaise / 100.0
    val netDouble: Double get() = netPaise / 100.0
    val averagePerDayDouble: Double get() = averagePerDayPaise / 100.0
    val topCategoryAmountDouble: Double get() = topCategoryAmountPaise / 100.0
}