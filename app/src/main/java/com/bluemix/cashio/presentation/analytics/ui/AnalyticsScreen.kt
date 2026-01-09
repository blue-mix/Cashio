package com.bluemix.cashio.presentation.analytics.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.presentation.analytics.vm.AnalyticsViewModel
import com.bluemix.cashio.ui.components.defaults.CashioPadding
import com.bluemix.cashio.ui.components.defaults.CashioSpacing
import org.koin.compose.viewmodel.koinViewModel

/**
 * Composable representing the Analytics screen of the Cashio application.
 *
 * This screen aggregates and displays financial data to the user, including:
 * - An interactive bar chart for expenses over specific time periods.
 * - Summary cards showing income/expense totals and their period-over-period deltas.
 * - A breakdown of the top spending category.
 *
 * @param onNavigateBack Callback invoked when the user requests navigation back to the previous screen.
 * @param viewModel The [AnalyticsViewModel] responsible for managing the UI state and business logic.
 * Defaults to [koinViewModel].
 */
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AnalyticsViewModel = koinViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val listContentPadding = remember(bottomInset) {
        PaddingValues(
            start = CashioPadding.screen,
            end = CashioPadding.screen,
            top = 0.dp,
            bottom = bottomInset + CashioPadding.screen
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        LargeTopAppBar(
            title = {
                Text(
                    modifier = Modifier,
                    text = "Analytics",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.background),
            modifier = Modifier.padding(horizontal = CashioPadding.screen) // 16.dp -> screen gutter
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(CashioSpacing.xl),
            contentPadding = listContentPadding
        ) {
            item { Spacer(modifier = Modifier.height(CashioSpacing.small)) }

            item {
                AnalyticsChartSection(
                    chartExpensesState = uiState.chartExpensesState,
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
                    statsState = uiState.statsState,
                    incomeDelta = uiState.incomeDelta,
                    expenseDelta = uiState.expenseDelta,
                    onIncomeClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onExpenseClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                )
            }

            item {
                AnalyticsTopCategorySection(
                    statsState = uiState.statsState,
                    topCategoryRatio = uiState.topCategoryRatio
                )
            }
        }
    }
}