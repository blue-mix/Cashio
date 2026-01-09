//package com.bluemix.cashio.presentation.history
//
//import androidx.compose.foundation.ExperimentalFoundationApi
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.asPaddingValues
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.navigationBars
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.statusBarsPadding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.hapticfeedback.HapticFeedbackType
//import androidx.compose.ui.platform.LocalHapticFeedback
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.bluemix.cashio.ui.components.cards.DayTransactionCard
//import com.bluemix.cashio.ui.components.cards.StateCard
//import com.bluemix.cashio.ui.components.cards.StateCardAction
//import com.bluemix.cashio.ui.components.cards.StateCardVariant
//import com.bluemix.cashio.ui.components.defaults.CashioPadding
//import com.bluemix.cashio.ui.components.defaults.CashioSpacing
//import kotlinx.coroutines.launch
//import org.koin.compose.viewmodel.koinViewModel
//import java.time.LocalDate
//
/////**
// * The main History screen displaying a chronological log of transactions.
// *
// * Layout Structure:
// * 1. **Top Bar:** Shows the currently selected date context and a "Jump to Today" action.
// * 2. **Sticky Header:** A horizontal calendar that stays pinned to the top. It visualizes spending intensity (heatmap) and allows filtering by date.
// * 3. **Transaction List:** A grouped list of transactions, organized by day.
// *
// * Interaction Logic:
// * - Clicking a date on the calendar filters the list to show only that day.
// * - Clicking "Today" clears filters and scrolls the list to the top.
// */
//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun HistoryScreen(
//    onTransactionClick: (String) -> Unit,
//    viewModel: HistoryViewModel = koinViewModel()
//) {
//    val state by viewModel.state.collectAsStateWithLifecycle()
//    val haptic = LocalHapticFeedback.current
//    val scope = rememberCoroutineScope()
//
//    val today = LocalDate.now()
//    val selectedDate = state.selectedDate
//
//    val listState = rememberLazyListState()
//    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .statusBarsPadding()
//    ) {
//        HistoryTopBar(
//            selectedDate = selectedDate,
//            onTodayClick = {
//                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//                viewModel.onDateClicked(today)
//                // Smoothly scroll back to the top when resetting view
//                scope.launch { listState.animateScrollToItem(0) }
//            },
//            modifier = Modifier.padding(horizontal = CashioPadding.screen)
//        )
//
//        LazyColumn(
//            state = listState,
//            modifier = Modifier.fillMaxSize(),
//            contentPadding = PaddingValues(bottom = bottomInset + CashioPadding.screen)
//        ) {
//            // The Calendar remains visible while scrolling through the list
//            stickyHeader {
//                CalendarStickyHeader(
//                    listState = listState,
//                    expenseTotalByDate = state.expenseTotalByDate,
//                    expenseHeatLevelByDate = state.expenseHeatLevelByDate,
//                    today = today,
//                    selectedDate = selectedDate,
//                    onDayClick = { date ->
//                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//                        viewModel.onDateClicked(date)
//                    }
//                )
//            }
//
//            val dayGroups = state.visibleDayGroups
//
//            // Render Content based on State (Loading -> Error -> Empty -> Data)
//            when {
//                state.isLoading -> {
//                    item {
//                        StateCard(
//                            variant = StateCardVariant.LOADING,
//                            modifier = Modifier.padding(CashioSpacing.huge),
//                            animated = true
//                        )
//                    }
//                }
//
//                state.errorMessage != null -> {
//                    item {
//                        StateCard(
//                            variant = StateCardVariant.ERROR,
//                            title = "Error loading history",
//                            message = state.errorMessage ?: "Something went wrong",
//                            action = StateCardAction(
//                                text = "Retry",
//                                onClick = { viewModel.onDateClicked(selectedDate ?: today) }
//                            ),
//                            modifier = Modifier.padding(CashioPadding.screen)
//                        )
//                    }
//                }
//
//                dayGroups.isEmpty() -> {
//                    item {
//                        EmptyTransactionsState(
//                            selectedDate = selectedDate,
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .padding(
//                                    horizontal = CashioPadding.screen,
//                                    vertical = CashioSpacing.huge
//                                )
//                        )
//                    }
//                }
//
//                else -> {
//                    itemsIndexed(
//                        items = dayGroups,
//                        key = { _, (date, _) -> date.toString() }
//                    ) { _, (date, dayTransactions) ->
//                        DayTransactionCard(
//                            date = date,
//                            transactions = dayTransactions,
//                            onTransactionClick = { id ->
//                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//                                onTransactionClick(id)
//                            },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(
//                                    horizontal = CashioPadding.screen,
//                                    vertical = CashioSpacing.xs
//                                )
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
package com.bluemix.cashio.presentation.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.ui.components.cards.DayTransactionCard
import com.bluemix.cashio.ui.components.cards.StateCard
import com.bluemix.cashio.ui.components.cards.StateCardAction
import com.bluemix.cashio.ui.components.cards.StateCardVariant
import com.bluemix.cashio.ui.components.defaults.CashioPadding
import com.bluemix.cashio.ui.components.defaults.CashioSpacing
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    onTransactionClick: (String) -> Unit,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val today = LocalDate.now()
    val selectedDate = state.selectedDate

    val listState = rememberLazyListState()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // ✅ stable callback (less allocations + cleaner)
    val onDayClick: (LocalDate) -> Unit = remember(viewModel, haptic) {
        { date ->
            // Use the best "tap" feedback your build supports.
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.onDateClicked(date)
        }
    }

    val dayGroups = state.visibleDayGroups

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        HistoryTopBar(
            selectedDate = selectedDate,
            onTodayClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                // Decide what Today means.
                // ✅ Here: jump to today's group (does NOT clear filter)
                onDayClick(today)

                // smooth jump to today's card (fallback to top)
                val todayIndex = dayGroups.indexOfFirst { (d, _) -> d == today }
                if (todayIndex != -1) {
                    scope.launch { listState.animateScrollToItem(todayIndex + 1) } // +1 for sticky header
                } else {
                    scope.launch { listState.animateScrollToItem(0) }
                }
            },
            modifier = Modifier.padding(horizontal = CashioPadding.screen)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomInset + CashioPadding.screen)
        ) {
            stickyHeader {
                CalendarStickyHeader(
                    listState = listState,
                    expenseTotalByDate = state.expenseTotalByDate,
                    expenseHeatLevelByDate = state.expenseHeatLevelByDate,
                    today = today,
                    selectedDate = selectedDate,
                    onDayClick = onDayClick
                )
            }

            when {
                state.isLoading -> item {
                    StateCard(
                        variant = StateCardVariant.LOADING,
                        modifier = Modifier.padding(CashioSpacing.huge),
                        animated = true
                    )
                }

                state.errorMessage != null -> item {
                    StateCard(
                        variant = StateCardVariant.ERROR,
                        title = "Error loading history",
                        message = state.errorMessage ?: "Something went wrong",
                        action = StateCardAction(
                            text = "Retry",
                            onClick = { onDayClick(selectedDate ?: today) }
                        ),
                        modifier = Modifier.padding(CashioPadding.screen)
                    )
                }

                dayGroups.isEmpty() -> item {
                    EmptyTransactionsState(
                        selectedDate = selectedDate,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = CashioPadding.screen,
                                vertical = CashioSpacing.huge
                            )
                    )
                }

                else -> {
                    items(
                        items = dayGroups,
                        key = { (date, _) -> date.toString() }
                    ) { (date, dayTransactions) ->
                        DayTransactionCard(
                            date = date,
                            transactions = dayTransactions,
                            onTransactionClick = { id ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTransactionClick(id)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = CashioPadding.screen,
                                    vertical = CashioSpacing.xs
                                )
                        )
                    }
                }
            }
        }
    }
}
