package com.bluemix.cashio.presentation.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.presentation.transaction.components.TransactionSearchBar
import com.bluemix.cashio.presentation.transaction.components.TransactionTypeFilterRow
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
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionsScreen(
    onNavigateBack: () -> Unit,
    onTransactionClick: (String) -> Unit,
    viewModel: TransactionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        CashioTopBar(
            title = CashioTopBarTitle.Text("Transactions"),
            leadingAction = TopBarAction(
                icon = TopBarIcon.Vector(Icons.Default.Close),
                onClick = onNavigateBack
            ),
            modifier = Modifier.padding(horizontal = CashioPadding.screen)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (val ui = state.transactionsUi) {
                UiState.Idle, UiState.Loading -> {
                    StateCard(variant = StateCardVariant.LOADING, animated = true)
                }

                is UiState.Error -> {
                    StateCard(
                        variant = StateCardVariant.ERROR,
                        title = "Load Failed",
                        message = ui.message,
                        action = StateCardAction("Retry", viewModel::loadAll),
                        modifier = Modifier.padding(CashioPadding.screen)
                    )
                }

                is UiState.Success -> {
                    val filtered = ui.data

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(CashioSpacing.medium), // 12dp
                        contentPadding = PaddingValues(
                            start = CashioPadding.screen,
                            end = CashioPadding.screen,
                            top = CashioSpacing.small,
                            bottom = navBarPadding + CashioSpacing.massive // Space for bottom nav/breathing room
                        )
                    ) {
                        item {
                            TransactionSearchBar(
                                query = state.query,
                                onQueryChange = viewModel::setQuery,
                                onClear = { viewModel.setQuery("") }
                            )
                        }

                        item {
                            TransactionTypeFilterRow(
                                filter = state.typeFilter,
                                onChange = viewModel::setTypeFilter
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Results",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${filtered.size}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (filtered.isEmpty()) {
                            item {
                                StateCard(
                                    variant = StateCardVariant.EMPTY,
                                    emoji = if (state.query.isBlank()) "🧾" else "🔍",
                                    title = if (state.query.isBlank()) "No transactions yet" else "No matches",
                                    message = if (state.query.isBlank())
                                        "Once you add or parse transactions, they’ll show up here."
                                    else "Try a different keyword."
                                )
                            }
                        } else {
                            items(filtered, key = { it.id }) { tx ->
                                TransactionListItem(
                                    title = tx.title,
                                    amount = tx.amount,
                                    type = tx.transactionType,
                                    dateTime = tx.date,
                                    categoryIcon = tx.category.icon,
                                    categoryColor = tx.category.color,
                                    onClick = { onTransactionClick(tx.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}