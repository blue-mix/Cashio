package com.bluemix.cashio.presentation.transaction

import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.presentation.common.UiState

enum class TransactionTypeFilter { ALL, EXPENSE, INCOME }

enum class TransactionSort {
    DATE_DESC, DATE_ASC,
    AMOUNT_DESC, AMOUNT_ASC,
    TITLE_ASC, TITLE_DESC
}

data class TransactionsState(
    // Cache
    val allTransactions: List<Expense> = emptyList(),

    // List UI
    val transactionsUi: UiState<List<Expense>> = UiState.Idle,
    val query: String = "",
    val typeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val sort: TransactionSort = TransactionSort.DATE_DESC,
    val isRefreshing: Boolean = false,

    // Details UI
    val selectedId: String? = null,
    val detailsUi: UiState<Expense> = UiState.Idle,

    // Delete UI
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,

    // Banner/snackbar
    val message: String? = null
)
