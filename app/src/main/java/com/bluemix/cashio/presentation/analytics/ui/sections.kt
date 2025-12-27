package com.bluemix.cashio.presentation.analytics.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.FinancialStats
import com.bluemix.cashio.presentation.analytics.vm.BarChartData
import com.bluemix.cashio.presentation.analytics.vm.ChartPeriod
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.ui.components.cards.FinancialSummaryCard
import com.bluemix.cashio.ui.components.chart.FinanceChartUi
import com.bluemix.cashio.ui.components.chart.FinanceStatsCard
import com.bluemix.cashio.ui.components.chart.SpendingOverviewCard
import com.bluemix.cashio.ui.components.chart.label
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
                EmptyStateCard(text = "No data for ${selectedPeriod.label}")
                return
            }

            FinanceStatsCard(
                chart = chartUi,
                selectedPeriod = selectedPeriod,
                onPeriodChange = onPeriodChange,
                currencySymbol = CurrencySymbol
            )
        }

        is UiState.Loading -> LoadingStateCard(height = 250.dp)
        is UiState.Error -> ErrorStateCard(message = chartExpensesState.message)
        else -> Unit
    }
}

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

@Composable
fun AnalyticsTopCategorySection(
    statsState: UiState<FinancialStats>,
    topCategoryRatio: Float
) {
    val stats = (statsState as? UiState.Success)?.data ?: return
    val topCategory = stats.topCategory ?: return

    SpendingOverviewCard(
        totalAmount = stats.totalExpenses,
        periodLabel = "Expense in ${currentMonthLabel()}",
        expenseRatio = topCategoryRatio,
        topCategory = topCategory.name,
        topCategoryAmount = stats.topCategoryAmount,
        topCategoryIcon = topCategory.icon ?: "💰",
        topCategoryColor = topCategory.color ?: MaterialTheme.colorScheme.primary,
        onTopCategoryClick = {},
        currencySymbol = CurrencySymbol
    )
}

fun currentMonthLabel(): String =
    LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM"))