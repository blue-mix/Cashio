package com.bluemix.cashio.domain.model

/**
 * Financial statistics for dashboard
 */
data class FinancialStats(
    val totalExpenses: Double,
    val totalIncome: Double,
    val expenseCount: Int,
    val averagePerDay: Double,
    val topCategory: Category?,
    val topCategoryAmount: Double,
    val categoryBreakdown: Map<Category, Double>
)
