package com.bluemix.cashio.presentation.transactiondetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.components.CashioCard
import com.bluemix.cashio.components.CashioTopBar
import com.bluemix.cashio.components.CashioTopBarTitle
import com.bluemix.cashio.components.TopBarAction
import com.bluemix.cashio.components.TopBarIcon
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.presentation.transaction.TransactionViewModel
import com.bluemix.cashio.ui.theme.CashioSemantic.ExpenseRed
import com.bluemix.cashio.ui.theme.CashioSemantic.IncomeGreen
import org.koin.compose.viewmodel.koinViewModel
import java.time.format.DateTimeFormatter

@Composable
fun TransactionDetailsScreen(
    transactionId: String,
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: TransactionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(transactionId) {
        viewModel.selectTransaction(transactionId)
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = navBarPadding)
    ) {
        CashioTopBar(
            title = CashioTopBarTitle.Text("Transaction Detail"),
            leadingAction = TopBarAction(
                icon = TopBarIcon.Vector(Icons.Default.Close),
                onClick = onNavigateBack
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        when (val ui = state.details) {
            is UiState.Loading, is UiState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Error -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = ui.message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            is UiState.Success -> {
                val tx = ui.data

                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = navBarPadding + 16.dp + 72.dp
                    )
                ) {
                    item { HeaderCard(tx) }

                    item {
                        InfoCard(
                            title = "Details",
                            rows = listOf(
                                "Type" to (if (tx.transactionType == TransactionType.EXPENSE) "Expense" else "Income"),
                                "Category" to "${tx.category.icon}  ${tx.category.name}",
                                "Date" to tx.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                            ) + listOfNotNull(
                                tx.note?.takeIf { it.isNotBlank() }?.let { "Note" to it }
                            )
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onEditClick(tx.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(Modifier.padding(start = 8.dp))
                                Text("Edit")
                            }

                            Button(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(Modifier.padding(start = 8.dp))
                                Text("Delete")
                            }
                        }
                    }
                }

                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        title = { Text("Delete transaction?") },
                        text = { Text("This action can’t be undone.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteConfirm = false
                                    viewModel.deleteTransaction(tx.id)
                                    onNavigateBack()
                                    viewModel.consumeDeleteSuccess()
                                }
                            ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderCard(tx: Expense) {
    val color = if (tx.transactionType == TransactionType.EXPENSE) ExpenseRed else IncomeGreen

    CashioCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(16.dp),
        cornerRadius = 16.dp,
        showBorder = true
    ) {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = tx.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "₹${String.format("%,.2f", tx.amount)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = "${tx.category.icon}  ${tx.category.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    rows: List<Pair<String, String>>
) {
    CashioCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(16.dp),
        cornerRadius = 16.dp,
        showBorder = true
    ) {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            rows.forEach { (k, v) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(k, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(v, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
