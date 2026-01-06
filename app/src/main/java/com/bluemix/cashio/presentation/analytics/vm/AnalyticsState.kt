package com.bluemix.cashio.presentation.analytics.vm

import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.DateRange
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.FinancialStats
import com.bluemix.cashio.presentation.common.UiState

/**
 * Represents the immutable UI state for the Analytics screen.
 *
 * This state holder aggregates data from multiple sources (stats, charts, categories)
 * to ensure the UI is rendered consistently.
 *
 * @property statsState The loading state of the general financial statistics (totals, averages).
 * @property chartExpensesState The loading state specifically for the list of expenses used in charts.
 * @property selectedDateRange The global date range filter applied to the data.
 * @property selectedChartPeriod The specific time granularity selected for the bar chart.
 * @property selectedChartType The visual representation type of the chart (Bar, Pie, Line).
 * @property barChartData The processed data sets (income vs expense) ready for chart rendering.
 * @property categoryBreakdown A map associating categories with their total expense amounts.
 * @property topCategoryRatio The ratio (0.0 to 1.0) of the highest spending category against total expenses.
 * @property incomeDelta The percentage change in income compared to the previous period.
 * @property expenseDelta The percentage change in expenses compared to the previous period.
 */
data class AnalyticsState(
    val statsState: UiState<FinancialStats> = UiState.Idle,
    val chartExpensesState: UiState<List<Expense>> = UiState.Idle,

    val selectedDateRange: DateRange = DateRange.THIS_MONTH,
    val selectedChartPeriod: ChartPeriod = ChartPeriod.MONTHLY,
    val selectedChartType: ChartType = ChartType.BAR_CHART,

    val barChartData: BarChartData = BarChartData(),

    val categoryBreakdown: Map<Category, Double> = emptyMap(),
    val topCategoryRatio: Float = 0f,
    val incomeDelta: Double = 0.0,
    val expenseDelta: Double = 0.0
)

/**
 * Data model containing the parallel lists required to render the comparison bar chart.
 *
 * @property incomeData List of income values corresponding to the [labels].
 * @property expenseData List of expense values corresponding to the [labels].
 * @property labels The x-axis labels (e.g., dates, months) for the data points.
 */
data class BarChartData(
    val incomeData: List<Double> = emptyList(),
    val expenseData: List<Double> = emptyList(),
    val labels: List<String> = emptyList()
)

/**
 * Enumeration of available time periods for chart analysis.
 *
 * @property label The human-readable text displayed in the UI for this period.
 */
enum class ChartPeriod(val label: String) {
    /**
     * Represents a weekly breakdown of the current month.
     */
    WEEKLY("This Month"),

    /**
     * Represents a monthly breakdown over the last 6 months.
     */
    MONTHLY("6 Months"),

    /**
     * Represents an annual breakdown.
     */
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