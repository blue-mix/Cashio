package com.bluemix.cashio.presentation.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.ui.components.cards.DayTransactionCard
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate

private val ScreenPadding = 16.dp
private val ItemVerticalPadding = 4.dp

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        HistoryTopBar(
            selectedDate = selectedDate,
            onTodayClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.onDateClicked(today)
                scope.launch { listState.animateScrollToItem(0) }
            },
            modifier = Modifier.padding(horizontal = ScreenPadding)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomInset + ScreenPadding)
        ) {
            stickyHeader {
                CalendarStickyHeader(
                    listState = listState,
                    expenseTotalByDate = state.expenseTotalByDate,
                    expenseHeatLevelByDate = state.expenseHeatLevelByDate, // ✅ new
                    today = today,
                    selectedDate = selectedDate,
                    onDayClick = { date ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDateClicked(date)
                    }
                )
            }

            val dayGroups = state.visibleDayGroups

            when {
                state.isLoading -> {
                    item { CenterMessage("Loading...", Modifier.padding(24.dp)) }
                }

                state.errorMessage != null -> {
                    item {
                        CenterMessage(
                            text = state.errorMessage ?: "Something went wrong",
                            color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }

                dayGroups.isEmpty() -> {
                    item {
                        EmptyTransactionsState(
                            selectedDate = selectedDate,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = ScreenPadding, vertical = 24.dp)
                        )
                    }
                }

                else -> {
                    itemsIndexed(
                        items = dayGroups,
                        key = { _, (date, _) -> date.toString() }
                    ) { _, (date, dayTransactions) ->
                        DayTransactionCard(
                            date = date,
                            transactions = dayTransactions,
                            onTransactionClick = { id ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTransactionClick(id)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = ScreenPadding, vertical = ItemVerticalPadding)
                        )
                    }
                }
            }
        }
    }
}
