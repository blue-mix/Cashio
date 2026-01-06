package com.bluemix.cashio.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.usecase.expense.DeleteExpenseUseCase
import com.bluemix.cashio.domain.usecase.expense.GetExpenseByIdUseCase
import com.bluemix.cashio.domain.usecase.expense.GetExpensesUseCase
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.presentation.transaction.util.filterAndSortTransactions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val getExpenseByIdUseCase: GetExpenseByIdUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionsState())
    val state: StateFlow<TransactionsState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadAll()
    }

    /* ------------------------- Data Loading ------------------------- */

    fun loadAll() = fetchTransactions(showLoading = true)

    fun refresh() {
        if (state.value.isRefreshing) return
        fetchTransactions(showLoading = false)
    }

    private fun fetchTransactions(showLoading: Boolean) {
        viewModelScope.launch {
            updateState {
                it.copy(
                    transactionsUi = if (showLoading) UiState.Loading else it.transactionsUi,
                    isRefreshing = !showLoading,
                    message = null
                )
            }

            when (val result = getExpensesUseCase()) {
                is Result.Success -> {
                    updateState { it.copy(allTransactions = result.data, isRefreshing = false) }
                    recalculateList()

                    // If we have a selection, ensure its details are fresh
                    state.value.selectedId?.let { id ->
                        syncDetailsFromCacheOrFetch(id, preferCache = true)
                    }
                }

                is Result.Error -> {
                    updateState {
                        it.copy(
                            isRefreshing = false,
                            transactionsUi = UiState.Error(
                                result.message ?: "Failed to load transactions"
                            )
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    /* ------------------------- Filtering & Sorting ------------------------- */

    fun setQuery(value: String) {
        updateState { it.copy(query = value) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(180) // Debounce
            recalculateList()
        }
    }

    fun setTypeFilter(value: TransactionTypeFilter) {
        updateState { it.copy(typeFilter = value) }
        recalculateList()
    }

    fun setSort(value: TransactionSort) {
        updateState { it.copy(sort = value) }
        recalculateList()
    }

    /**
     * Applies filters to the master list and updates the UI state.
     * Keeps the "Source of Truth" (allTransactions) separate from "View" (transactionsUi).
     */
    private fun recalculateList() {
        val s = state.value
        val filtered = filterAndSortTransactions(
            list = s.allTransactions,
            query = s.query,
            typeFilter = s.typeFilter,
            sort = s.sort
        )
        updateState { it.copy(transactionsUi = UiState.Success(filtered)) }
    }

    /* ------------------------- Details Selection ------------------------- */

    fun selectTransaction(expenseId: String) {
        updateState { it.copy(selectedId = expenseId, deleteSuccess = false, message = null) }
        syncDetailsFromCacheOrFetch(expenseId, preferCache = true)
    }

    fun clearSelection() {
        updateState { it.copy(selectedId = null, detailsUi = UiState.Idle) }
    }

    private fun syncDetailsFromCacheOrFetch(expenseId: String, preferCache: Boolean) {
        val cached = state.value.allTransactions.find { it.id == expenseId }

        if (preferCache && cached != null) {
            updateState { it.copy(detailsUi = UiState.Success(cached)) }
            return
        }

        viewModelScope.launch {
            updateState { it.copy(detailsUi = UiState.Loading) }

            when (val result = getExpenseByIdUseCase(expenseId)) {
                is Result.Success -> {
                    val tx = result.data
                    if (tx != null) {
                        // Update Master List with fresh data if it exists there
                        updateState { old ->
                            val updatedList =
                                old.allTransactions.map { if (it.id == expenseId) tx else it }
                            old.copy(allTransactions = updatedList, detailsUi = UiState.Success(tx))
                        }
                        recalculateList() // Re-apply sort/filter to reflect changes
                    } else {
                        updateState { it.copy(detailsUi = UiState.Error("Transaction not found")) }
                    }
                }

                is Result.Error -> {
                    updateState {
                        it.copy(
                            detailsUi = UiState.Error(
                                result.message ?: "Failed to load details"
                            )
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    /* ------------------------- Deletion ------------------------- */

    fun deleteTransaction(expenseId: String) {
        if (state.value.isDeleting) return

        viewModelScope.launch {
            updateState { it.copy(isDeleting = true, deleteSuccess = false, message = null) }

            when (val result = deleteExpenseUseCase(expenseId)) {
                is Result.Success -> {
                    // Optimistic update: remove locally immediately
                    updateState { old ->
                        val newAll = old.allTransactions.filterNot { it.id == expenseId }
                        val wasSelected = old.selectedId == expenseId

                        old.copy(
                            allTransactions = newAll,
                            selectedId = if (wasSelected) null else old.selectedId,
                            detailsUi = if (wasSelected) UiState.Idle else old.detailsUi,
                            isDeleting = false,
                            deleteSuccess = true,
                            message = "Transaction deleted"
                        )
                    }
                    recalculateList()
                }

                is Result.Error -> {
                    updateState {
                        it.copy(
                            isDeleting = false,
                            message = result.message ?: "Failed to delete"
                        )
                    }
                }

                else -> updateState { it.copy(isDeleting = false) }
            }
        }
    }

    fun consumeDeleteSuccess() = updateState { it.copy(deleteSuccess = false) }
    fun clearMessage() = updateState { it.copy(message = null) }

    /* ------------------------- Helpers ------------------------- */

    private fun updateState(transform: (TransactionsState) -> TransactionsState) {
        _state.update(transform)
    }

    override fun onCleared() {
        searchJob?.cancel()
        super.onCleared()
    }
}