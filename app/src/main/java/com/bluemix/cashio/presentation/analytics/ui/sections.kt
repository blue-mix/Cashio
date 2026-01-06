package com.bluemix.cashio.presentation.analytics.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.core.format.CashioFormat.currentMonthLabel
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.FinancialStats
import com.bluemix.cashio.presentation.analytics.vm.BarChartData
import com.bluemix.cashio.presentation.analytics.vm.ChartPeriod
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.ui.components.cards.FinancialSummaryCard
import com.bluemix.cashio.ui.components.cards.StateCard
import com.bluemix.cashio.ui.components.cards.StateCardVariant
import com.bluemix.cashio.ui.components.chart.FinanceChartUi
import com.bluemix.cashio.ui.components.chart.FinanceStatsCard
import com.bluemix.cashio.ui.components.chart.SpendingOverviewCard
import java.time.LocalDate

private const val CurrencySymbol = "₹"

/**
 * Renders the main chart section of the Analytics screen.
 *
 * Handles different UI states (Loading, Error, Success) for the chart data.
 * In the Success state, it renders a bar chart comparing income vs. expenses
 * across the [selectedPeriod].
 *
 * @param chartExpensesState The current UI state of the expense data loading process.
 * @param chartData The processed data points (income, expense, labels) for the chart.
 * @param selectedPeriod The currently selected time period filter.
 * @param onPeriodChange Callback triggered when the user selects a new time period.
 */
@Composable
fun AnalyticsChartSection(
    chartExpensesState: UiState<List<Expense>>,
    chartData: BarChartData,
    selectedPeriod: ChartPeriod,
    onPeriodChange: (ChartPeriod) -> Unit
) {
    when (chartExpensesState) {
        is UiState.Success -> {
            val chartUi = remember(chartData) {
                FinanceChartUi(
                    incomeData = chartData.incomeData,
                    expenseData = chartData.expenseData,
                    labels = chartData.labels
                )
            }

            if (chartUi.labels.isEmpty()) {
                StateCard(
                    variant = StateCardVariant.EMPTY,
                    title = "No data available",
                    message = "No data for ${selectedPeriod.label}",
                    emoji = "📊"
                )
                return
            }

            FinanceStatsCard(
                chart = chartUi,
                selectedPeriod = selectedPeriod,
                onPeriodChange = onPeriodChange,
                currencySymbol = CurrencySymbol
            )
        }

        is UiState.Loading -> {
            StateCard(
                variant = StateCardVariant.LOADING,
                height = 250.dp,
                animated = true
            )
        }

        is UiState.Error -> {
            StateCard(
                variant = StateCardVariant.ERROR,
                title = "Chart Error",
                message = chartExpensesState.message ?: "Could not load chart data"
            )
        }

        else -> Unit
    }
}

/**
 * Renders the summary cards displaying total income and expenses.
 *
 * Only renders if [statsState] is [UiState.Success].
 *
 * @param statsState The UI state containing aggregated financial statistics.
 * @param incomeDelta The percentage change in income compared to the previous period.
 * @param expenseDelta The percentage change in expenses compared to the previous period.
 * @param onIncomeClick Callback triggered when the income summary is clicked.
 * @param onExpenseClick Callback triggered when the expense summary is clicked.
 */
@Composable
fun AnalyticsSummarySection(
    statsState: UiState<FinancialStats>,
    incomeDelta: Double,
    expenseDelta: Double,
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit
) {
    val stats = (statsState as? UiState.Success)?.data ?: return

    FinancialSummaryCard(
        totalIncome = stats.totalIncome,
        totalExpenses = stats.totalExpenses,
        incomeDelta = incomeDelta,
        expenseDelta = expenseDelta,
        currencySymbol = CurrencySymbol,
        showChevron = true,
        onIncomeClick = onIncomeClick,
        onExpenseClick = onExpenseClick
    )
}

/**
 * Renders a card highlighting the category with the highest spending.
 *
 * Only renders if [statsState] is [UiState.Success] and a top category exists.
 *
 * @param statsState The UI state containing aggregated financial statistics.
 * @param topCategoryRatio The ratio (0.0 - 1.0) of the top category's spending relative to total expenses.
 */
@Composable
fun AnalyticsTopCategorySection(
    statsState: UiState<FinancialStats>,
    topCategoryRatio: Float
) {
    val stats = (statsState as? UiState.Success)?.data ?: return
    val topCategory = stats.topCategory ?: return

    SpendingOverviewCard(
        totalAmount = stats.totalExpenses,
        periodLabel = "Expense in ${LocalDate.now().currentMonthLabel()}",
        expenseRatio = topCategoryRatio,
        topCategory = topCategory.name,
        topCategoryAmount = stats.topCategoryAmount,
        topCategoryIcon = topCategory.icon ?: "💰",
        topCategoryColor = topCategory.color ?: MaterialTheme.colorScheme.primary,
        onTopCategoryClick = {},
        currencySymbol = CurrencySymbol
    )
}