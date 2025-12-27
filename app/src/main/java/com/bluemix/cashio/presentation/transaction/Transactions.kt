package com.bluemix.cashio.presentation.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.presentation.transaction.components.TransactionEmptyCard
import com.bluemix.cashio.presentation.transaction.components.TransactionErrorCard
import com.bluemix.cashio.presentation.transaction.components.TransactionSearchBar
import com.bluemix.cashio.presentation.transaction.components.TransactionTypeFilterRow
import com.bluemix.cashio.ui.components.cards.TransactionListItem
import com.bluemix.cashio.ui.components.defaults.CashioTopBar
import com.bluemix.cashio.ui.components.defaults.CashioTopBarTitle
import com.bluemix.cashio.ui.components.defaults.TopBarAction
import com.bluemix.cashio.ui.components.defaults.TopBarIcon
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

    ColumnScaffold(
        top = {
            CashioTopBar(
                title = CashioTopBarTitle.Text("Transactions"),
                leadingAction = TopBarAction(
                    icon = TopBarIcon.Vector(Icons.Default.Close),
                    onClick = onNavigateBack
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val ui = state.transactionsUi) {
                UiState.Idle, UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Error -> {
                    TransactionErrorCard(
                        message = ui.message,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        onRetry = viewModel::loadAll
                    )
                }

                is UiState.Success -> {
                    val filtered = ui.data

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = navBarPadding + 16.dp + 72.dp
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
                                TransactionEmptyCard(
                                    title = if (state.query.isBlank()) "No transactions yet" else "No matches",
                                    subtitle = if (state.query.isBlank())
                                        "Once you add or parse transactions, they’ll show up here."
                                    else "Try a different keyword.",
                                    modifier = Modifier.fillMaxWidth()
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
                                    showCategoryIcon = true,
                                    showChevron = true,
                                    showDate = true,
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

@Composable
private fun ColumnScaffold(
    top: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
        top()
        content()
    }
}
