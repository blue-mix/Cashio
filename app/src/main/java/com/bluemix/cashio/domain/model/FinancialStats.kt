package com.bluemix.cashio.domain.model

/**
 * Financial statistics for dashboard
 */
data class FinancialStats(
    val totalExpenses: Double = 0.0,
    val totalIncome: Double = 0.0,
    val expenseCount: Int = 0,
    val averagePerDay: Double = 0.0,
    val topCategory: Category? = null,
    val topCategoryAmount: Double = 0.0,
    val categoryBreakdown: Map<Category, Double> = emptyMap()
)