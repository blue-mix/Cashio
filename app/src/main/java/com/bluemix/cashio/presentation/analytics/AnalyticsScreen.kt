
package com.bluemix.cashio.presentation.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.components.CashioTopBar
import com.bluemix.cashio.components.CashioTopBarTitle
import com.bluemix.cashio.components.FinanceChartUi
import com.bluemix.cashio.components.FinanceStatsCard
import com.bluemix.cashio.components.FinancialSummaryCard
import com.bluemix.cashio.components.SpendingOverviewCard
import com.bluemix.cashio.components.label
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.FinancialStats
import com.bluemix.cashio.presentation.common.UiState
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ScreenPadding = 16.dp
private val SectionSpacing = 20.dp

@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit = {}, // currently unused but kept for future back navigation
    viewModel: AnalyticsViewModel = koinViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val listContentPadding = PaddingValues(
        start = ScreenPadding,
        end = ScreenPadding,
        top = 0.dp,
        bottom = bottomInset + ScreenPadding
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top bar
        CashioTopBar(
            title = CashioTopBarTitle.Text("Analytics"),
            contentColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = ScreenPadding)
        )

        // Main content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(SectionSpacing),
            contentPadding = listContentPadding
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                AnalyticsChartSection(
                    expensesState = uiState.expenses,
                    chartData = uiState.barChartData,
                    selectedPeriod = uiState.selectedChartPeriod,
                    onPeriodChange = { newPeriod ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.changeChartPeriod(newPeriod)
                    }
                )
            }

            item {
                AnalyticsSummarySection(
                    statsState = uiState.stats,
                    incomeDelta = uiState.incomeChange,
                    expenseDelta = uiState.expenseChange,
                    onIncomeClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        // TODO: navigate to income breakdown
                    },
                    onExpenseClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        // TODO: navigate to expense breakdown
                    }
                )
            }

            item {
                AnalyticsTopCategorySection(
                    statsState = uiState.stats,
                    topCategoryRatio = uiState.topCategoryPercentage // should be 0f..1f
                )
            }
        }
    }
}

/**
 * Section: Bar chart (Income vs Expense) + period selector.
 */
@Composable
private fun AnalyticsChartSection(
    expensesState: UiState<List<Expense>>,
    chartData: BarChartData,
    selectedPeriod: ChartPeriod,
    onPeriodChange: (ChartPeriod) -> Unit
) {
    when (expensesState) {
        is UiState.Success -> {
            val chartUi = FinanceChartUi(
                incomeData = chartData.incomeData,
                expenseData = chartData.expenseData,
                labels = chartData.labels
            )

            if (chartUi.labels.isEmpty()) {
                EmptyStateCard(text = "No data for ${selectedPeriod.label}")
                return
            }

            FinanceStatsCard(
                chart = chartUi,
                selectedPeriod = selectedPeriod,
                onPeriodChange = onPeriodChange,
                currencySymbol = "₹"
            )
        }

        is UiState.Loading -> LoadingStateCard(height = 250.dp)

        is UiState.Error -> ErrorStateCard(message = expensesState.message)

        else -> Unit
    }
}

/**
 * Section: Total Income/Expense + month-over-month deltas.
 */
@Composable
private fun AnalyticsSummarySection(
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
        currencySymbol = "₹",
        showChevron = true,
        onIncomeClick = onIncomeClick,
        onExpenseClick = onExpenseClick
    )
}

/**
 * Section: Ring chart + top spending category.
 *
 * NOTE: SpendingOverviewCard now expects `expenseRatio` (0..1), not `expensePercentage`.
 */
@Composable
private fun AnalyticsTopCategorySection(
    statsState: UiState<FinancialStats>,
    topCategoryRatio: Float
) {
    val stats = (statsState as? UiState.Success)?.data ?: return
    val topCategory = stats.topCategory ?: return

    SpendingOverviewCard(
        totalAmount = stats.totalExpenses,
        periodLabel = "Expense in ${currentMonthLabel()}",
        expenseRatio = topCategoryRatio, // ✅ updated param name
        topCategory = topCategory.name,
        topCategoryAmount = stats.topCategoryAmount,
        topCategoryIcon = topCategory.icon ?: "💰",
        topCategoryColor = topCategory.color ?: MaterialTheme.colorScheme.primary,
        onTopCategoryClick = {},
        currencySymbol = "₹"
    )
}

private fun currentMonthLabel(): String =
    LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM"))

/* -------------------------------------------------------------------------- */
/* UI Helpers                                                                 */
/* -------------------------------------------------------------------------- */

@Composable
private fun LoadingStateCard(height: Dp) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun EmptyStateCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorStateCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Medium
        )
    }
}
