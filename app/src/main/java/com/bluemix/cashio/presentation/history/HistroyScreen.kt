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
import com.bluemix.cashio.ui.defaults.CashioPadding
import com.bluemix.cashio.ui.defaults.CashioSpacing
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate

/**
 * History screen showing transaction history with calendar heatmap.
 *
 * All monetary values are in **paise (Long)**.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    onTransactionClick: (String) -> Unit,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val today = remember { LocalDate.now() }
    val selectedDate = state.selectedDate

    val listState = rememberLazyListState()
    val bottomInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    // Memoized callbacks
    val onDayClick = remember(viewModel, haptic) {
        { date: LocalDate ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.onDateClicked(date)
        }
    }

    // Explicitly define the type to avoid inference errors
    val onTodayClick: () -> Unit =
        remember(haptic, scope, state.visibleDayGroups, today, onDayClick) {
            {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDayClick(today)

                // Find the index of today in the grouped list
                val todayIndex = state.visibleDayGroups.indexOfFirst { it.first == today }

                scope.launch {
                    if (todayIndex != -1) {
                        // +1 to account for any items before the transaction list if necessary
                        listState.animateScrollToItem(todayIndex)
                    } else {
                        listState.animateScrollToItem(0)
                    }
                }
            }
        }
    val onTransactionClickMemoized = remember(haptic, onTransactionClick) {
        { id: String ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onTransactionClick(id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        HistoryTopBar(
            selectedDate = selectedDate,
            onTodayClick = onTodayClick,
            modifier = Modifier.padding(horizontal = CashioPadding.screen)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = bottomInset + CashioPadding.screen
            )
        ) {
            stickyHeader {
                CalendarStickyHeader(
                    listState = listState,
                    expenseTotalByDatePaise = state.expenseTotalByDatePaise,
                    expenseHeatLevelByDate = state.expenseHeatLevelByDate,
                    today = today,
                    selectedDate = selectedDate,
                    onDayClick = onDayClick
                )
            }

            when {
                state.isLoading -> {
                    item {
                        StateCard(
                            variant = StateCardVariant.LOADING,
                            modifier = Modifier.padding(CashioSpacing.lg),
                            animated = true
                        )
                    }
                }

                state.errorMessage != null -> {
                    item {
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
                }

                state.visibleDayGroups.isEmpty() -> {
                    item {
                        EmptyTransactionsState(
                            selectedDate = selectedDate,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    horizontal = CashioPadding.screen,
                                    vertical = CashioSpacing.lg
                                )
                        )
                    }
                }

                else -> {
                    items(
                        items = state.visibleDayGroups,
                        key = { (date, _) -> date.toString() }
                    ) { (date, dayTransactions) ->
                        DayTransactionCard(
                            date = date,
                            transactions = dayTransactions,
                            onTransactionClick = onTransactionClickMemoized,
                            currency = state.selectedCurrency,
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