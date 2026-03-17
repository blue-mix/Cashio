//package com.bluemix.cashio.presentation.analytics.ui
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.asPaddingValues
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.navigationBars
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.statusBarsPadding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.material3.LargeTopAppBar
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.hapticfeedback.HapticFeedbackType
//import androidx.compose.ui.platform.LocalHapticFeedback
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.bluemix.cashio.presentation.analytics.vm.AnalyticsViewModel
//import com.bluemix.cashio.presentation.analytics.vm.ChartPeriod
//import com.bluemix.cashio.ui.defaults.CashioPadding
//import com.bluemix.cashio.ui.defaults.CashioSpacing
//import org.koin.compose.viewmodel.koinViewModel
//
//private object AnalyticsDefaults {
//    val SectionSpacing = CashioSpacing.xl
//    val TopPadding = CashioSpacing.small
//}
//
///**
// * Analytics screen — all ViewModel interaction is confined here.
// *
// * Child sections ([AnalyticsChartSection], [AnalyticsSummarySection],
// * [AnalyticsTopCategorySection]) are fully stateless.
// */
//@Composable
//fun AnalyticsScreen(
//    onNavigateBack: () -> Unit = {},
//    viewModel: AnalyticsViewModel = koinViewModel()
//) {
//    val uiState by viewModel.state.collectAsStateWithLifecycle()
//    val haptic = LocalHapticFeedback.current
//
//    val bottomInset = WindowInsets.navigationBars
//        .asPaddingValues()
//        .calculateBottomPadding()
//
//    val listContentPadding = remember(bottomInset) {
//        PaddingValues(
//            start = CashioPadding.screen,
//            end = CashioPadding.screen,
//            top = 0.dp,
//            bottom = bottomInset + CashioPadding.screen
//        )
//    }
//
//    // ── Memoized callbacks ──────────────────────────────────────────────
//    val onPeriodChange = remember(haptic) {
//        { period: ChartPeriod ->
//            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//            viewModel.changeChartPeriod(period)
//        }
//    }
//
//    val onIncomeClick = remember(haptic) {
//        {
//            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//            // TODO: Navigate to income details
//        }
//    }
//
//    val onExpenseClick = remember(haptic) {
//        {
//            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//            // TODO: Navigate to expense details
//        }
//    }
//
//    // ── Layout ──────────────────────────────────────────────────────────
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .statusBarsPadding()
//    ) {
//        LargeTopAppBar(
//            title = {
//                Text(
//                    text = "Analytics",
//                    style = MaterialTheme.typography.displaySmall,
//                    color = MaterialTheme.colorScheme.onSurface,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            },
//            colors = TopAppBarDefaults.topAppBarColors(
//                containerColor = MaterialTheme.colorScheme.background
//            ),
//            modifier = Modifier.padding(horizontal = CashioPadding.screen)
//        )
//
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.spacedBy(AnalyticsDefaults.SectionSpacing),
//            contentPadding = listContentPadding
//        ) {
//            item { Spacer(Modifier.height(AnalyticsDefaults.TopPadding)) }
//
//            item {
//                AnalyticsChartSection(
//                    chartExpensesState = uiState.chartExpensesState,
//                    chartData = uiState.barChartData,
//                    selectedPeriod = uiState.selectedChartPeriod,
//                    onPeriodChange = onPeriodChange
//                )
//            }
//
//            item {
//                AnalyticsSummarySection(
//                    statsState = uiState.statsState,
//                    incomeDeltaPaise = uiState.incomeDeltaPaise,
//                    expenseDeltaPaise = uiState.expenseDeltaPaise,
//                    onIncomeClick = onIncomeClick,
//                    onExpenseClick = onExpenseClick
//                )
//            }
//
//            item {
//                AnalyticsTopCategorySection(
//                    statsState = uiState.statsState,
//                    topCategoryRatio = uiState.topCategoryRatio
//                )
//            }
//        }
//    }
//}
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
import com.bluemix.cashio.presentation.analytics.vm.ChartPeriod
import com.bluemix.cashio.ui.defaults.CashioPadding
import com.bluemix.cashio.ui.defaults.CashioSpacing
import org.koin.compose.viewmodel.koinViewModel

private object AnalyticsDefaults {
    val SectionSpacing = CashioSpacing.lg
    val TopPadding = CashioSpacing.xs
}

/**
 * Analytics screen — all ViewModel interaction is confined here.
 *
 * Child sections ([AnalyticsChartSection], [AnalyticsSummarySection],
 * [AnalyticsTopCategorySection]) are fully stateless.
 */
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AnalyticsViewModel = koinViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    val bottomInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    val listContentPadding = remember(bottomInset) {
        PaddingValues(
            start = CashioPadding.screen,
            end = CashioPadding.screen,
            top = 0.dp,
            bottom = bottomInset + CashioPadding.screen
        )
    }

    // ── Memoized callbacks ──────────────────────────────────────────────
    val onPeriodChange = remember(haptic) {
        { period: ChartPeriod ->
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.changeChartPeriod(period)
        }
    }

    val onIncomeClick = remember(haptic) {
        {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            // TODO: Navigate to income details
        }
    }

    val onExpenseClick = remember(haptic) {
        {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            // TODO: Navigate to expense details
        }
    }

    // ── Layout ──────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        LargeTopAppBar(
            title = {
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier.padding(horizontal = CashioPadding.screen)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AnalyticsDefaults.SectionSpacing),
            contentPadding = listContentPadding
        ) {
            item { Spacer(Modifier.height(AnalyticsDefaults.TopPadding)) }

            item {
                AnalyticsChartSection(
                    chartExpensesState = uiState.chartExpensesState,
                    chartData = uiState.barChartData,
                    selectedPeriod = uiState.selectedChartPeriod,
                    onPeriodChange = onPeriodChange
                )
            }

            item {
                AnalyticsSummarySection(
                    statsState = uiState.statsState,
                    incomeDeltaPaise = uiState.incomeDeltaPaise,
                    expenseDeltaPaise = uiState.expenseDeltaPaise,
                    onIncomeClick = onIncomeClick,
                    onExpenseClick = onExpenseClick
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