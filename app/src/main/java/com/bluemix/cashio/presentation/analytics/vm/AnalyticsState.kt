package com.bluemix.cashio.presentation.analytics.vm

import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.Currency
import com.bluemix.cashio.domain.model.DateRange
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.FinancialStats
import com.bluemix.cashio.presentation.common.UiState

/**
 * Represents the immutable UI state for the Analytics screen.
 *
 * All monetary values are in **paise (Long)**.
 *
 * @property statsState The loading state of the general financial statistics (totals, averages).
 * @property chartExpensesState The loading state specifically for the list of expenses used in charts.
 * @property selectedDateRange The global date range filter applied to the data.
 * @property selectedChartPeriod The specific time granularity selected for the bar chart.
 * @property selectedChartType The visual representation type of the chart (Bar, Pie, Line).
 * @property barChartData The processed data sets (income vs expense) ready for chart rendering.
 * @property categoryBreakdown A map associating categories with their total expense amounts (paise).
 * @property topCategoryRatio The ratio (0.0 to 1.0) of the highest spending category against total expenses.
 * @property incomeDeltaPaise The change in income compared to the previous period (paise).
 * @property expenseDeltaPaise The change in expenses compared to the previous period (paise).
 * @property selectedCurrency The user's selected currency for formatting.
 */
data class AnalyticsState(
    val statsState: UiState<FinancialStats> = UiState.Idle,
    val chartExpensesState: UiState<List<Expense>> = UiState.Idle,

    val selectedDateRange: DateRange = DateRange.THIS_MONTH,
    val selectedChartPeriod: ChartPeriod = ChartPeriod.MONTHLY,
    val selectedChartType: ChartType = ChartType.BAR_CHART,

    val barChartData: BarChartData = BarChartData(),

    val categoryBreakdown: Map<Category, Long> = emptyMap(),
    val topCategoryRatio: Float = 0f,
    val incomeDeltaPaise: Long = 0L,
    val expenseDeltaPaise: Long = 0L,

    val selectedCurrency: Currency = Currency.INR
)

/**
 * Data model containing the parallel lists required to render the comparison bar chart.
 *
 * All monetary values are in **paise (Long)**.
 *
 * @property incomeDataPaise List of income values in paise corresponding to the [labels].
 * @property expenseDataPaise List of expense values in paise corresponding to the [labels].
 * @property labels The x-axis labels (e.g., dates, months) for the data points.
 */
data class BarChartData(
    val incomeDataPaise: List<Long> = emptyList(),
    val expenseDataPaise: List<Long> = emptyList(),
    val labels: List<String> = emptyList()
)

/**
 * Enumeration of available time periods for chart analysis.
 */
enum class ChartPeriod(val label: String) {
    WEEKLY("This Month"),
    MONTHLY("6 Months"),
    YEARLY("Yearly")
}

/**
 * Enumeration of supported chart visualization types.
 */
enum class ChartType {
    PIE_CHART,
    BAR_CHART,
    LINE_CHART
}