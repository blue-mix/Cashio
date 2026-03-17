//package com.bluemix.cashio.presentation.transaction
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
//import androidx.compose.foundation.layout.navigationBars
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.bluemix.cashio.presentation.common.UiState
//import com.bluemix.cashio.presentation.transaction.components.TransactionDetailsHeaderCard
//import com.bluemix.cashio.presentation.transaction.components.TransactionDetailsInfoCard
//import com.bluemix.cashio.ui.defaults.CashioPadding
//import com.bluemix.cashio.ui.defaults.CashioRadius
//import com.bluemix.cashio.ui.defaults.CashioSpacing
//import com.bluemix.cashio.ui.defaults.CashioTopBar
//import com.bluemix.cashio.ui.defaults.CashioTopBarTitle
//import com.bluemix.cashio.ui.defaults.TopBarAction
//import com.bluemix.cashio.ui.defaults.TopBarIcon
//import org.koin.compose.viewmodel.koinViewModel
//
///**
// * Transaction details screen.
// *
// * ViewModel interaction is confined here.
// * [TransactionDetailsHeaderCard] and [TransactionDetailsInfoCard] are stateless.
// */
//@Composable
//fun TransactionDetailsScreen(
//    transactionId: String,
//    onNavigateBack: () -> Unit,
//    onEditClick: (String) -> Unit,
//    viewModel: TransactionViewModel = koinViewModel()
//) {
//    val state by viewModel.state.collectAsStateWithLifecycle()
//    val navBarPadding = WindowInsets.navigationBars
//        .asPaddingValues()
//        .calculateBottomPadding()
//
//    LaunchedEffect(transactionId) { viewModel.selectTransaction(transactionId) }
//
//    LaunchedEffect(state.deleteSuccess) {
//        if (state.deleteSuccess) {
//            viewModel.consumeDeleteSuccess()
//            onNavigateBack()
//        }
//    }
//
//    var showDeleteConfirm by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(bottom = navBarPadding)
//    ) {
//        CashioTopBar(
//            title = CashioTopBarTitle.Text("Transaction Detail"),
//            leadingAction = TopBarAction(
//                icon = TopBarIcon.Vector(Icons.Default.Close),
//                onClick = onNavigateBack
//            ),
//            modifier = Modifier.padding(
//                horizontal = CashioPadding.screen,
//                vertical = CashioPadding.screen
//            )
//        )
//
//        when (val ui = state.detailsUi) {
//            UiState.Loading, UiState.Idle -> {
//                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    CircularProgressIndicator()
//                }
//            }
//
//            is UiState.Error -> {
//                Surface(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(CashioPadding.screen),
//                    shape = RoundedCornerShape(CashioRadius.medium),
//                    color = MaterialTheme.colorScheme.errorContainer
//                ) {
//                    Text(
//                        text = ui.message,
//                        modifier = Modifier.padding(CashioPadding.screen),
//                        color = MaterialTheme.colorScheme.onErrorContainer
//                    )
//                }
//            }
//
//            is UiState.Success -> {
//                val tx = ui.data
//
//                LazyColumn(
//                    modifier = Modifier.fillMaxSize(),
//                    verticalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
//                    contentPadding = PaddingValues(
//                        start = CashioPadding.screen,
//                        end = CashioPadding.screen,
//                        top = CashioSpacing.compact,
//                        bottom = navBarPadding + CashioPadding.screen + CashioSpacing.massive
//                    )
//                ) {
//                    item { TransactionDetailsHeaderCard(tx = tx, currency = state.selectedCurrency) }
//                    item { TransactionDetailsInfoCard(tx = tx) }
//                    item {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
//                        ) {
//                            Button(
//                                enabled = !state.isDeleting,
//                                onClick = { onEditClick(tx.id) },
//                                modifier = Modifier.weight(1f)
//                            ) {
//                                Icon(Icons.Default.Edit, contentDescription = null)
//                                Spacer(Modifier.padding(start = CashioSpacing.compact))
//                                Text("Edit")
//                            }
//                            Button(
//                                enabled = !state.isDeleting,
//                                onClick = { showDeleteConfirm = true },
//                                modifier = Modifier.weight(1f)
//                            ) {
//                                Icon(Icons.Default.Delete, contentDescription = null)
//                                Spacer(Modifier.padding(start = CashioSpacing.compact))
//                                Text(if (state.isDeleting) "Deleting..." else "Delete")
//                            }
//                        }
//                    }
//                }
//
//                // Delete confirmation
//                if (showDeleteConfirm) {
//                    AlertDialog(
//                        onDismissRequest = { if (!state.isDeleting) showDeleteConfirm = false },
//                        title = { Text("Delete transaction?") },
//                        text = { Text("This action can't be undone.") },
//                        confirmButton = {
//                            TextButton(
//                                enabled = !state.isDeleting,
//                                onClick = { viewModel.deleteTransaction(tx.id) }
//                            ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
//                        },
//                        dismissButton = {
//                            TextButton(
//                                enabled = !state.isDeleting,
//                                onClick = { showDeleteConfirm = false }
//                            ) { Text("Cancel") }
//                        }
//                    )
//                }
//
//                // Auto-close dialog
//                LaunchedEffect(state.isDeleting) {
//                    if (state.isDeleting) showDeleteConfirm = false
//                }
//            }
//        }
//    }
//}
package com.bluemix.cashio.presentation.transaction

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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.presentation.transaction.components.TransactionDetailsHeaderCard
import com.bluemix.cashio.presentation.transaction.components.TransactionDetailsInfoCard
import com.bluemix.cashio.ui.defaults.CashioPadding
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioSpacing
import com.bluemix.cashio.ui.defaults.CashioTopBar
import com.bluemix.cashio.ui.defaults.CashioTopBarTitle
import com.bluemix.cashio.ui.defaults.TopBarAction
import com.bluemix.cashio.ui.defaults.TopBarIcon
import org.koin.compose.viewmodel.koinViewModel

/**
 * Transaction details screen.
 *
 * ViewModel interaction is confined here.
 * [TransactionDetailsHeaderCard] and [TransactionDetailsInfoCard] are stateless.
 */
@Composable
fun TransactionDetailsScreen(
    transactionId: String,
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: TransactionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navBarPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    LaunchedEffect(transactionId) { viewModel.selectTransaction(transactionId) }

    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) {
            viewModel.consumeDeleteSuccess()
            onNavigateBack()
        }
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
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
            modifier = Modifier.padding(
                horizontal = CashioPadding.screen,
                vertical = CashioPadding.screen
            )
        )

        when (val ui = state.detailsUi) {
            UiState.Loading, UiState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Error -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(CashioPadding.screen),
                    shape = RoundedCornerShape(CashioRadius.medium),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = ui.message,
                        modifier = Modifier.padding(CashioPadding.screen),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            is UiState.Success -> {
                val tx = ui.data

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(CashioSpacing.sm),
                    contentPadding = PaddingValues(
                        start = CashioPadding.screen,
                        end = CashioPadding.screen,
                        top = CashioSpacing.sm,
                        bottom = navBarPadding + CashioPadding.screen + CashioSpacing.xl
                    )
                ) {
                    item { TransactionDetailsHeaderCard(tx = tx, currency = state.selectedCurrency) }
                    item { TransactionDetailsInfoCard(tx = tx) }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.sm)
                        ) {
                            Button(
                                enabled = !state.isDeleting,
                                onClick = { onEditClick(tx.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(Modifier.padding(start = CashioSpacing.sm))
                                Text("Edit")
                            }
                            Button(
                                enabled = !state.isDeleting,
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(Modifier.padding(start = CashioSpacing.sm))
                                Text(if (state.isDeleting) "Deleting..." else "Delete")
                            }
                        }
                    }
                }

                // Delete confirmation
                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { if (!state.isDeleting) showDeleteConfirm = false },
                        title = { Text("Delete transaction?") },
                        text = { Text("This action can't be undone.") },
                        confirmButton = {
                            TextButton(
                                enabled = !state.isDeleting,
                                onClick = { viewModel.deleteTransaction(tx.id) }
                            ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                        },
                        dismissButton = {
                            TextButton(
                                enabled = !state.isDeleting,
                                onClick = { showDeleteConfirm = false }
                            ) { Text("Cancel") }
                        }
                    )
                }

                // Auto-close dialog
                LaunchedEffect(state.isDeleting) {
                    if (state.isDeleting) showDeleteConfirm = false
                }
            }
        }
    }
}