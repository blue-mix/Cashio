package com.bluemix.cashio.presentation.transactions

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.R
import com.bluemix.cashio.components.CashioTopBar
import com.bluemix.cashio.components.CashioTopBarTitle
import com.bluemix.cashio.components.TopBarAction
import com.bluemix.cashio.components.TopBarIcon
import com.bluemix.cashio.components.TransactionListItem
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.presentation.transaction.TransactionViewModel
import com.bluemix.cashio.presentation.transaction.TxTypeFilter
import org.koin.compose.viewmodel.koinViewModel

/**
 * All transactions screen (search + type filter) using the shared TransactionViewModel.
 */
@Composable
fun TransactionsScreen(
    onNavigateBack: () -> Unit,
    onTransactionClick: (String) -> Unit,
    viewModel: TransactionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
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
            when (val ui = state.list) {
                is UiState.Idle, is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Error -> {
                    ErrorCard(
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
                            SearchBar(
                                query = state.query,
                                onQueryChange = viewModel::setQuery,
                                onClear = { viewModel.setQuery("") }
                            )
                        }

                        item {
                            TypeFilterRow(
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
                                EmptyCard(
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
                                    onClick = {
                                        // keep VM in sync + navigate
                                        viewModel.selectTransaction(tx.id)
                                        onTransactionClick(tx.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ----------------------------- UI Pieces ----------------------------- */

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        leadingIcon = { Icon(painter = painterResource(R.drawable.search), contentDescription = null,
            Modifier.size(20.dp)) },
        trailingIcon = {
            AnimatedVisibility(visible = query.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        placeholder = { Text("Search title, category, amount…") },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun TypeFilterRow(
    filter: TxTypeFilter,
    onChange: (TxTypeFilter) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FilterChip(
            selected = filter == TxTypeFilter.ALL,
            onClick = { onChange(TxTypeFilter.ALL) },
            label = { Text("All") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                selectedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        FilterChip(
            selected = filter == TxTypeFilter.EXPENSE,
            onClick = { onChange(TxTypeFilter.EXPENSE) },
            label = { Text("Expense") }
        )
        FilterChip(
            selected = filter == TxTypeFilter.INCOME,
            onClick = { onChange(TxTypeFilter.INCOME) },
            label = { Text("Income") }
        )
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

@Composable
private fun EmptyCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("⚠️", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            androidx.compose.material3.OutlinedButton(onClick = onRetry) { Text("Retry") }
        }
    }
}
