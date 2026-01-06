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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.R
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.ui.components.cards.StateCard
import com.bluemix.cashio.ui.components.cards.StateCardAction
import com.bluemix.cashio.ui.components.cards.StateCardVariant
import com.bluemix.cashio.ui.components.cards.TransactionListItem
import com.bluemix.cashio.ui.components.defaults.CashioTopBar
import com.bluemix.cashio.ui.components.defaults.CashioTopBarTitle
import com.bluemix.cashio.ui.components.defaults.TopBarAction
import com.bluemix.cashio.ui.components.defaults.TopBarIcon
import com.bluemix.cashio.ui.theme.CashioPadding
import com.bluemix.cashio.ui.theme.CashioSpacing
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

/**
 * The main landing screen of the application.
 *
 * Responsibilities:
 * 1. **Snapshot:** Displays high-level monthly spending and wallet balance.
 * 2. **Sync:** Provides triggers (Pull-to-Refresh & TopBar Action) to parse SMS for new transactions.
 * 3. **Recents:** Lists the most recent transactions for quick review.
 *
 * @param onNavigateToWallet Navigates to the Wallet/History screen.
 * @param onNavigateToTransactionDetails Navigates to the details view of a specific transaction.
 * @param onNavigateToAllTransactions Navigates to the full transaction history.
 */
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

    // Auto-dismiss the SMS refresh toast/message after a few seconds
    LaunchedEffect(state.smsRefreshMessage) {
        if (state.smsRefreshMessage == null) return@LaunchedEffect
        delay(3500)
        viewModel.clearSmsRefreshMessage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = CashioPadding.screen)
            .statusBarsPadding()
    ) {
        CashioTopBar(
            title = CashioTopBarTitle.Date(icon = TopBarIcon.Drawable(R.drawable.calendar)),
            contentColor = MaterialTheme.colorScheme.onBackground,
            trailingAction = TopBarAction(
                icon = TopBarIcon.Drawable(R.drawable.refresh),
                enabled = !state.isRefreshingSms,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.refreshFromSms()
                }
            )
        )

        // --- Summary Section (Non-Scrollable) ---
        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.default)) {
            Spacer(modifier = Modifier.height(CashioSpacing.small))

            MonthSpendCard(
                amount = state.totalExpenses,
                percentageChange = state.percentageChange,
                isIncrease = state.isIncrease
            )
//
//            SpendingWalletCard(
//                balance = state.walletBalance,
//                onClick = {
//                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//                    onNavigateToWallet()
//                }
//            )

            // Inline Feedback for SMS Sync
            state.smsRefreshMessage?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Section Header
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
                TextButton(onClick = onNavigateToAllTransactions) {
                    Text("See All")
                }
            }
        }

        Spacer(modifier = Modifier.height(CashioSpacing.small))

        // --- Recent Transactions List (Scrollable + Refreshable) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val navInsets = WindowInsets.navigationBars.asPaddingValues()

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
                    is UiState.Loading, is UiState.Idle -> {
                        StateCard(variant = StateCardVariant.LOADING, animated = true)
                    }

                    is UiState.Success -> {
                        val recent = ui.data
                        if (recent.isEmpty()) {
                            StateCard(
                                variant = StateCardVariant.EMPTY,
                                emoji = "📝",
                                title = "No transactions yet",
                                message = "Import your expenses from SMS to get started.",
                                action = StateCardAction(
                                    text = if (state.isRefreshingSms) "Syncing..." else "Import from SMS",
                                    onClick = { viewModel.refreshFromSms() }
                                )
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
                                contentPadding = PaddingValues(
                                    bottom = navInsets.calculateBottomPadding() + CashioPadding.screen
                                )
                            ) {
                                itemsIndexed(
                                    recent,
                                    key = { _, item -> item.id }
                                ) { index, expense ->
                                    AnimatedTransactionItem(key = expense.id, index = index) {
                                        TransactionListItem(
                                            title = expense.title,
                                            amount = expense.amount,
                                            type = expense.transactionType,
                                            dateTime = expense.date,
                                            categoryIcon = expense.category.icon,
                                            categoryColor = expense.category.color,
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
                        StateCard(
                            variant = StateCardVariant.ERROR,
                            title = "Oops!",
                            message = ui.message ?: "Something went wrong",
                            action = StateCardAction(
                                text = "Retry",
                                onClick = { viewModel.retryRecent() }
                            )
                        )
                    }
                }
            }
        }
    }
}