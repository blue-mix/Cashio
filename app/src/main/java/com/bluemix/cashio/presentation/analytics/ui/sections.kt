package com.bluemix.cashio.presentation.analytics.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
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
import com.bluemix.cashio.ui.theme.toComposeColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private object AnalyticsSectionDefaults {
    val ChartHeight = 250.dp
}

/* -------------------------------------------------------------------------- */
/* Chart                                                                       */
/* -------------------------------------------------------------------------- */

/**
 * Renders the bar-chart section handling Loading / Error / Success.
 *
 * Stateless — all data comes from the parent screen.
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
                    incomeDataPaise = chartData.incomeDataPaise,
                    expenseDataPaise = chartData.expenseDataPaise,
                    labels = chartData.labels
                )
            }

            if (chartUi.isEmpty) {
                EmptyChartState(selectedPeriod.label)
            } else {
                FinanceStatsCard(
                    chart = chartUi,
                    selectedPeriod = selectedPeriod,
                    onPeriodChange = onPeriodChange
                )
            }
        }

        is UiState.Loading -> {
            StateCard(
                variant = StateCardVariant.LOADING,
                height = AnalyticsSectionDefaults.ChartHeight,
                animated = true
            )
        }

        is UiState.Error -> {
            StateCard(
                variant = StateCardVariant.ERROR,
                title = "Chart Error",
                message = chartExpensesState.message
            )
        }

        else -> Unit
    }
}

@Composable
private fun EmptyChartState(periodLabel: String) {
    StateCard(
        variant = StateCardVariant.EMPTY,
        title = "No data available",
        message = "No data for $periodLabel",
        emoji = "📊"
    )
}

/* -------------------------------------------------------------------------- */
/* Summary                                                                     */
/* -------------------------------------------------------------------------- */

/**
 * Income / Expense summary with period-over-period deltas.
 *
 * Renders only when [statsState] is [UiState.Success].
 * All monetary values in **paise**.
 */
@Composable
fun AnalyticsSummarySection(
    statsState: UiState<FinancialStats>,
    incomeDeltaPaise: Long,
    expenseDeltaPaise: Long,
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit
) {
    val stats = (statsState as? UiState.Success)?.data ?: return

    FinancialSummaryCard(
        totalIncomePaise = stats.totalIncomePaise,
        totalExpensesPaise = stats.totalExpensesPaise,
        incomeDeltaPaise = incomeDeltaPaise,
        expenseDeltaPaise = expenseDeltaPaise,
        comparisonLabel = "last month",
        showChevron = true,
        onIncomeClick = onIncomeClick,
        onExpenseClick = onExpenseClick
    )
}

/* -------------------------------------------------------------------------- */
/* Top Category                                                                */
/* -------------------------------------------------------------------------- */

/**
 * Highlights the category with the highest spending.
 *
 * All monetary values in **paise**.
 */
@Composable
fun AnalyticsTopCategorySection(
    statsState: UiState<FinancialStats>,
    topCategoryRatio: Float
) {
    val stats = (statsState as? UiState.Success)?.data ?: return
    val topCategory = stats.topCategory ?: return

    val currentMonthLabel = remember {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        LocalDate.now().format(formatter)
    }

    val topCategoryColor = remember(topCategory.colorHex) {
        topCategory.colorHex.toComposeColor()
    }

    SpendingOverviewCard(
        totalAmountPaise = stats.totalExpensesPaise,
        periodLabel = currentMonthLabel,
        expenseRatio = topCategoryRatio,
        topCategory = topCategory.name,
        topCategoryAmountPaise = stats.topCategoryAmountPaise,
        topCategoryIcon = topCategory.icon,
        topCategoryColor = topCategoryColor,
        onTopCategoryClick = { /* TODO: Navigate to category details */ }
    )
}