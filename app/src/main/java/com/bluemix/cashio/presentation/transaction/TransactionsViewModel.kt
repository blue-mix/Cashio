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
        fetchTransactions(showLoading = true)
    }

    fun loadAll() = fetchTransactions(showLoading = true)

    fun refresh() {
        if (_state.value.isRefreshing) return
        fetchTransactions(showLoading = false)
    }

    private fun fetchTransactions(showLoading: Boolean) {
        viewModelScope.launch {
            _state.update { s ->
                s.copy(
                    transactionsUi = if (showLoading) UiState.Loading else s.transactionsUi,
                    isRefreshing = !showLoading,
                    message = null
                )
            }

            when (val result = getExpensesUseCase()) {
                is Result.Success -> {
                    _state.update { it.copy(allTransactions = result.data, isRefreshing = false) }
                    applyFilters()

                    _state.value.selectedId?.let { id ->
                        syncDetailsFromCacheOrFetch(id, preferCache = true)
                    }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            transactionsUi = UiState.Error(
                                result.message ?: "Failed to load transactions"
                            )
                        )
                    }
                }

                Result.Loading -> _state.update { it.copy(isRefreshing = !showLoading) }
            }
        }
    }

    fun setQuery(value: String) {
        _state.update { it.copy(query = value) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(180)
            if (_state.value.transactionsUi is UiState.Loading) return@launch
            applyFilters()
        }
    }

    fun setTypeFilter(value: TransactionTypeFilter) {
        _state.update { it.copy(typeFilter = value) }
        applyFilters()
    }

    fun setSort(value: TransactionSort) {
        _state.update { it.copy(sort = value) }
        applyFilters()
    }

    private fun applyFilters() {
        val s = _state.value
        val filtered = filterAndSortTransactions(
            list = s.allTransactions,
            query = s.query,
            typeFilter = s.typeFilter,
            sort = s.sort
        )
        _state.update { it.copy(transactionsUi = UiState.Success(filtered)) }
    }

    fun selectTransaction(expenseId: String) {
        _state.update { it.copy(selectedId = expenseId, deleteSuccess = false, message = null) }
        syncDetailsFromCacheOrFetch(expenseId, preferCache = true)
    }

    fun clearSelection() {
        _state.update { it.copy(selectedId = null, detailsUi = UiState.Idle) }
    }

    private fun syncDetailsFromCacheOrFetch(expenseId: String, preferCache: Boolean) {
        val cached = _state.value.allTransactions.firstOrNull { it.id == expenseId }
        if (preferCache && cached != null) {
            _state.update { it.copy(detailsUi = UiState.Success(cached)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(detailsUi = UiState.Loading) }

            when (val result = getExpenseByIdUseCase(expenseId)) {
                is Result.Success -> {
                    val tx = result.data
                    if (tx == null) {
                        _state.update { it.copy(detailsUi = UiState.Error("Transaction not found")) }
                        return@launch
                    }

                    _state.update { old ->
                        val replaced = old.allTransactions.toMutableList().apply {
                            val idx = indexOfFirst { it.id == expenseId }
                            if (idx >= 0) set(idx, tx) else add(0, tx)
                        }
                        old.copy(allTransactions = replaced)
                    }

                    applyFilters()
                    _state.update { it.copy(detailsUi = UiState.Success(tx)) }
                }

                is Result.Error -> _state.update {
                    it.copy(detailsUi = UiState.Error(result.message ?: "Failed to load details"))
                }

                Result.Loading -> Unit
            }
        }
    }

    fun deleteTransaction(expenseId: String) {
        if (_state.value.isDeleting) return

        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true, deleteSuccess = false, message = null) }

            when (val result = deleteExpenseUseCase(expenseId)) {
                is Result.Success -> {
                    _state.update { old ->
                        val newAll = old.allTransactions.filterNot { it.id == expenseId }
                        val clearingDetails = old.selectedId == expenseId

                        old.copy(
                            allTransactions = newAll,
                            selectedId = if (clearingDetails) null else old.selectedId,
                            detailsUi = if (clearingDetails) UiState.Idle else old.detailsUi,
                            isDeleting = false,
                            deleteSuccess = true,
                            message = "Transaction deleted"
                        )
                    }
                    applyFilters()
                }

                is Result.Error -> _state.update {
                    it.copy(
                        isDeleting = false,
                        message = result.message ?: "Failed to delete transaction"
                    )
                }

                Result.Loading -> _state.update { it.copy(isDeleting = true) }
            }
        }
    }

    fun consumeDeleteSuccess() {
        _state.update { it.copy(deleteSuccess = false) }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    override fun onCleared() {
        searchJob?.cancel()
        super.onCleared()
    }
}
