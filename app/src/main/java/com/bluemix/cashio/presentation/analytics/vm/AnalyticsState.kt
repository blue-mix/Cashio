package com.bluemix.cashio.presentation.analytics.vm

import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.DateRange
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.FinancialStats
import com.bluemix.cashio.presentation.common.UiState

data class AnalyticsState(
    val statsState: UiState<FinancialStats> = UiState.Idle,
    val chartExpensesState: UiState<List<Expense>> = UiState.Idle,

    val selectedDateRange: DateRange = DateRange.THIS_MONTH,
    val selectedChartPeriod: ChartPeriod = ChartPeriod.MONTHLY,
    val selectedChartType: ChartType = ChartType.BAR_CHART,

    val barChartData: BarChartData = BarChartData(),

    val categoryBreakdown: Map<Category, Double> = emptyMap(),
    val topCategoryRatio: Float = 0f, // 0f..1f
    val incomeDelta: Double = 0.0,
    val expenseDelta: Double = 0.0
)

data class BarChartData(
    val incomeData: List<Double> = emptyList(),
    val expenseData: List<Double> = emptyList(),
    val labels: List<String> = emptyList()
)

enum class ChartPeriod { WEEKLY, MONTHLY, YEARLY }
enum class ChartType { PIE_CHART, BAR_CHART, LINE_CHART }
