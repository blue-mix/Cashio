//package com.bluemix.cashio.presentation.home
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.asPaddingValues
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.navigationBars
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.statusBarsPadding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.pulltorefresh.PullToRefreshBox
//import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.hapticfeedback.HapticFeedbackType
//import androidx.compose.ui.platform.LocalHapticFeedback
//import androidx.compose.ui.text.font.FontWeight
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.bluemix.cashio.R
//import com.bluemix.cashio.presentation.common.UiState
//import com.bluemix.cashio.ui.components.cards.StateCard
//import com.bluemix.cashio.ui.components.cards.StateCardAction
//import com.bluemix.cashio.ui.components.cards.StateCardVariant
//import com.bluemix.cashio.ui.components.cards.TransactionListItem
//import com.bluemix.cashio.ui.defaults.CashioPadding
//import com.bluemix.cashio.ui.defaults.CashioSpacing
//import com.bluemix.cashio.ui.defaults.CashioTopBar
//import com.bluemix.cashio.ui.defaults.CashioTopBarTitle
//import com.bluemix.cashio.ui.defaults.TopBarAction
//import com.bluemix.cashio.ui.defaults.TopBarIcon
//import com.bluemix.cashio.ui.theme.toComposeColor
//import kotlinx.coroutines.delay
//import org.koin.compose.viewmodel.koinViewModel
//
//private object DashboardDefaults {
//    const val SmsMessageDismissDelayMs = 3500L
//}
//
///**
// * Main landing screen.
// *
// * All ViewModel / NavController logic is here.
// * Children ([MonthSpendCard], [AnimatedTransactionItem], etc.) are stateless.
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DashboardScreen(
//    onNavigateToWallet: () -> Unit = {},
//    onNavigateToTransactionDetails: (String) -> Unit = {},
//    onNavigateToAllTransactions: () -> Unit = {},
//    viewModel: DashboardViewModel = koinViewModel()
//) {
//    val state by viewModel.state.collectAsStateWithLifecycle()
//    val haptic = LocalHapticFeedback.current
//    val listState = rememberLazyListState()
//    val pullState = rememberPullToRefreshState()
//
//    // Auto-dismiss SMS message
//    LaunchedEffect(state.smsRefreshMessage) {
//        state.smsRefreshMessage ?: return@LaunchedEffect
//        delay(DashboardDefaults.SmsMessageDismissDelayMs)
//        viewModel.clearSmsRefreshMessage()
//    }
//
//    // Memoized callbacks
//    val onRefresh = remember(haptic) {
//        {
//            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
//            viewModel.refreshFromSms()
//        }
//    }
//
//    val onTransactionClick = remember(haptic, onNavigateToTransactionDetails) {
//        { id: String ->
//            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//            onNavigateToTransactionDetails(id)
//        }
//    }
//
//    // ── Layout ──────────────────────────────────────────────────────────
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(horizontal = CashioPadding.screen)
//            .statusBarsPadding()
//    ) {
//        CashioTopBar(
//            title = CashioTopBarTitle.Date(icon = TopBarIcon.Drawable(R.drawable.calendar)),
//            contentColor = MaterialTheme.colorScheme.onBackground,
//            trailingAction = TopBarAction(
//                icon = TopBarIcon.Drawable(R.drawable.refresh),
//                enabled = !state.isRefreshingSms,
//                onClick = onRefresh
//            )
//        )
//
//        // Non-scrollable summary
//        DashboardSummary(
//            totalExpensesPaise = state.totalExpensesPaise,
//            percentageChange = state.percentageChange,
//            isIncrease = state.isIncrease,
//            selectedCurrency = state.selectedCurrency,
//            smsRefreshMessage = state.smsRefreshMessage,
//            onSeeAllClick = onNavigateToAllTransactions
//        )
//
//        Spacer(Modifier.height(CashioSpacing.small))
//
//        // Scrollable + pull-to-refresh list
//        val navInsets = WindowInsets.navigationBars.asPaddingValues()
//
//        Box(Modifier.fillMaxWidth()) {
//            PullToRefreshBox(
//                state = pullState,
//                isRefreshing = state.isRefreshingSms,
//                onRefresh = onRefresh,
//                modifier = Modifier.fillMaxSize()
//            ) {
//                when (val ui = state.recentExpenses) {
//                    is UiState.Loading, is UiState.Idle -> {
//                        StateCard(variant = StateCardVariant.LOADING, animated = true)
//                    }
//
//                    is UiState.Success -> {
//                        if (ui.data.isEmpty()) {
//                            StateCard(
//                                variant = StateCardVariant.EMPTY,
//                                emoji = "📝",
//                                title = "No transactions yet",
//                                message = "Import your expenses from SMS to get started.",
//                                action = StateCardAction(
//                                    text = if (state.isRefreshingSms) "Syncing..." else "Import from SMS",
//                                    onClick = onRefresh
//                                )
//                            )
//                        } else {
//                            LazyColumn(
//                                state = listState,
//                                modifier = Modifier.fillMaxSize(),
//                                verticalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
//                                contentPadding = PaddingValues(
//                                    bottom = navInsets.calculateBottomPadding() + CashioPadding.screen
//                                )
//                            ) {
//                                itemsIndexed(ui.data, key = { _, item -> item.id }) { index, expense ->
//                                    val categoryColor = remember(expense.category.colorHex) {
//                                        expense.category.colorHex.toComposeColor()
//                                    }
//                                    AnimatedTransactionItem(key = expense.id, index = index) {
//                                        TransactionListItem(
//                                            title = expense.title,
//                                            amountPaise = expense.amountPaise,
//                                            type = expense.transactionType,
//                                            dateTime = expense.date,
//                                            categoryIcon = expense.category.icon,
//                                            categoryColor = categoryColor,
//                                            onClick = { onTransactionClick(expense.id) }
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    is UiState.Error -> {
//                        StateCard(
//                            variant = StateCardVariant.ERROR,
//                            title = "Oops!",
//                            message = ui.message,
//                            action = StateCardAction("Retry", viewModel::retryRecent)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Summary (non-scrollable header)                                             */
///* -------------------------------------------------------------------------- */
//
//@Composable
//private fun DashboardSummary(
//    totalExpensesPaise: Long,
//    percentageChange: Float,
//    isIncrease: Boolean,
//    selectedCurrency: com.bluemix.cashio.domain.model.Currency,
//    smsRefreshMessage: String?,
//    onSeeAllClick: () -> Unit
//) {
//    Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.default)) {
//        Spacer(Modifier.height(CashioSpacing.small))
//
//        MonthSpendCard(
//            amountPaise = totalExpensesPaise,
//            percentageChange = percentageChange,
//            isIncrease = isIncrease,
//            currency = selectedCurrency
//        )
//
//        smsRefreshMessage?.let { msg ->
//            Text(
//                text = msg,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.primary
//            )
//        }
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "Recent Transactions",
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.SemiBold,
//                color = MaterialTheme.colorScheme.onBackground
//            )
//            TextButton(onClick = onSeeAllClick) { Text("See All") }
//        }
//    }
//}
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
import androidx.compose.runtime.remember
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
import com.bluemix.cashio.ui.defaults.CashioPadding
import com.bluemix.cashio.ui.defaults.CashioSpacing
import com.bluemix.cashio.ui.defaults.CashioTopBar
import com.bluemix.cashio.ui.defaults.CashioTopBarTitle
import com.bluemix.cashio.ui.defaults.TopBarAction
import com.bluemix.cashio.ui.defaults.TopBarIcon
import com.bluemix.cashio.ui.theme.toComposeColor
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

private object DashboardDefaults {
    const val SmsMessageDismissDelayMs = 3500L
}

/**
 * Main landing screen.
 *
 * All ViewModel / NavController logic is here.
 * Children ([MonthSpendCard], [AnimatedTransactionItem], etc.) are stateless.
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

    // Auto-dismiss SMS message
    LaunchedEffect(state.smsRefreshMessage) {
        state.smsRefreshMessage ?: return@LaunchedEffect
        delay(DashboardDefaults.SmsMessageDismissDelayMs)
        viewModel.clearSmsRefreshMessage()
    }

    // Memoized callbacks
    val onRefresh = remember(haptic) {
        {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.refreshFromSms()
        }
    }

    val onTransactionClick = remember(haptic, onNavigateToTransactionDetails) {
        { id: String ->
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onNavigateToTransactionDetails(id)
        }
    }

    // ── Layout ──────────────────────────────────────────────────────────
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
                onClick = onRefresh
            )
        )

        // Non-scrollable summary
        DashboardSummary(
            totalExpensesPaise = state.totalExpensesPaise,
            percentageChange = state.percentageChange,
            isIncrease = state.isIncrease,
            selectedCurrency = state.selectedCurrency,
            smsRefreshMessage = state.smsRefreshMessage,
            onSeeAllClick = onNavigateToAllTransactions
        )

        Spacer(Modifier.height(CashioSpacing.xs))

        // Scrollable + pull-to-refresh list
        val navInsets = WindowInsets.navigationBars.asPaddingValues()

        Box(Modifier.fillMaxWidth()) {
            PullToRefreshBox(
                state = pullState,
                isRefreshing = state.isRefreshingSms,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when (val ui = state.recentExpenses) {
                    is UiState.Loading, is UiState.Idle -> {
                        StateCard(variant = StateCardVariant.LOADING, animated = true)
                    }

                    is UiState.Success -> {
                        if (ui.data.isEmpty()) {
                            StateCard(
                                variant = StateCardVariant.EMPTY,
                                emoji = "📝",
                                title = "No transactions yet",
                                message = "Import your expenses from SMS to get started.",
                                action = StateCardAction(
                                    text = if (state.isRefreshingSms) "Syncing..." else "Import from SMS",
                                    onClick = onRefresh
                                )
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(CashioSpacing.sm),
                                contentPadding = PaddingValues(
                                    bottom = navInsets.calculateBottomPadding() + CashioPadding.screen
                                )
                            ) {
                                itemsIndexed(ui.data, key = { _, item -> item.id }) { index, expense ->
                                    val categoryColor = remember(expense.category.colorHex) {
                                        expense.category.colorHex.toComposeColor()
                                    }
                                    AnimatedTransactionItem(key = expense.id, index = index) {
                                        TransactionListItem(
                                            title = expense.title,
                                            amountPaise = expense.amountPaise,
                                            type = expense.transactionType,
                                            dateTime = expense.date,
                                            categoryIcon = expense.category.icon,
                                            categoryColor = categoryColor,
                                            onClick = { onTransactionClick(expense.id) }
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
                            message = ui.message,
                            action = StateCardAction("Retry", viewModel::retryRecent)
                        )
                    }
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Summary (non-scrollable header)                                             */
/* -------------------------------------------------------------------------- */

@Composable
private fun DashboardSummary(
    totalExpensesPaise: Long,
    percentageChange: Float,
    isIncrease: Boolean,
    selectedCurrency: com.bluemix.cashio.domain.model.Currency,
    smsRefreshMessage: String?,
    onSeeAllClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.md)) {
        Spacer(Modifier.height(CashioSpacing.xs))

        MonthSpendCard(
            amountPaise = totalExpensesPaise,
            percentageChange = percentageChange,
            isIncrease = isIncrease,
            currency = selectedCurrency
        )

        smsRefreshMessage?.let { msg ->
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
            TextButton(onClick = onSeeAllClick) { Text("See All") }
        }
    }
}