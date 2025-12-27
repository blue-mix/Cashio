package com.bluemix.cashio.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.R
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.ui.components.cards.TransactionListItem
import com.bluemix.cashio.ui.components.defaults.CashioTopBar
import com.bluemix.cashio.ui.components.defaults.CashioTopBarTitle
import com.bluemix.cashio.ui.components.defaults.TopBarIcon
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToWallet: () -> Unit = {},
    onNavigateToTransactionDetails: (String) -> Unit = {},
    onNavigateToAllTransactions: () -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val pullState = rememberPullToRefreshState()
    val screenGutter = 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = screenGutter)
            .statusBarsPadding()
    ) {
        CashioTopBar(
            title = CashioTopBarTitle.Date(icon = TopBarIcon.Drawable(R.drawable.calendar)),
            contentColor = MaterialTheme.colorScheme.onBackground
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            MonthSpendCard(
                amount = state.totalExpenses,
                percentageChange = state.percentageChange,
                isIncrease = state.isIncrease
            )

            SpendingWalletCard(
                balance = state.walletBalance,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNavigateToWallet()
                }
            )

            state.smsRefreshMessage?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(
                    onClick = {
                        onNavigateToAllTransactions()
                    }
                ) { Text("See All") }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            val navInsets = WindowInsets.navigationBars.asPaddingValues()
            val extraBottomPadding = 16.dp // breathing room

            PullToRefreshBox(
                state = pullState,
                isRefreshing = state.isRefreshingSms,
                onRefresh = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.refreshFromSms()
                },
                modifier = Modifier.fillMaxSize()
            ) {
                when (val ui = state.recentExpenses) {
                    is UiState.Loading, is UiState.Idle -> DashboardLoadingState()

                    is UiState.Success -> {
                        val recent = ui.data
                        if (recent.isEmpty()) {
                            EmptyTransactionsCard()
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(
                                    bottom = navInsets.calculateBottomPadding() + extraBottomPadding
                                )

                            ) {
                                itemsIndexed(recent, key = { _, item -> item.id }) { index, expense ->
                                    AnimatedTransactionItem(key = expense.id,index = index) {
                                        TransactionListItem(
                                            title = expense.title,
                                            amount = expense.amount,
                                            type = expense.transactionType,
                                            dateTime = expense.date,
                                            categoryIcon = expense.category.icon,
                                            categoryColor = expense.category.color,
                                            showCategoryIcon = true,
                                            showChevron = true,
                                            showDate = true,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onNavigateToTransactionDetails(expense.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is UiState.Error -> {
                        DashboardErrorCard(
                            message = ui.message,
                            onRetry = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.retryRecent()
                            }
                        )
                    }
                }
            }
        }
    }
}
