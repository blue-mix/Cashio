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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.presentation.analytics.vm.AnalyticsViewModel
import com.bluemix.cashio.ui.components.defaults.CashioTopBar
import com.bluemix.cashio.ui.components.defaults.CashioTopBarTitle
import org.koin.compose.viewmodel.koinViewModel

private val ScreenPadding = 16.dp
private val SectionSpacing = 20.dp
const val CurrencySymbol = "₹"

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
            start = ScreenPadding,
            end = ScreenPadding,
            top = 0.dp,
            bottom = bottomInset + ScreenPadding
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        CashioTopBar(
            title = CashioTopBarTitle.Text("Analytics"),
            contentColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = ScreenPadding)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(SectionSpacing),
            contentPadding = listContentPadding
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

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
                        // TODO: navigate
                    },
                    onExpenseClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        // TODO: navigate
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
