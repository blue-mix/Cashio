package com.bluemix.cashio.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.usecase.expense.DeleteExpenseUseCase
import com.bluemix.cashio.domain.usecase.expense.GetExpenseByIdUseCase
import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesUseCase
import com.bluemix.cashio.domain.usecase.preferences.ObserveSelectedCurrencyUseCase
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.presentation.transaction.util.filterAndSortTransactions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing the Transactions list and details screens.
 *
 * All monetary values are in **paise (Long)** within Expense objects.
 */
class TransactionViewModel(
    private val observeExpensesUseCase: ObserveExpensesUseCase,
    private val getExpenseByIdUseCase: GetExpenseByIdUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val observeSelectedCurrencyUseCase: ObserveSelectedCurrencyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionsState())
    val state: StateFlow<TransactionsState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        observeTransactions()
    }

    /* ------------------------- Data Loading ------------------------- */

    private fun observeTransactions() {
        viewModelScope.launch {
            updateState { it.copy(transactionsUi = UiState.Loading) }

            combine(
                observeExpensesUseCase(),
                observeSelectedCurrencyUseCase()
            ) { transactions, currency ->
                transactions to currency
            }
                .distinctUntilChanged()
                .catch { t ->
                    updateState {
                        it.copy(
                            transactionsUi = UiState.Error(
                                t.message ?: "Failed to load transactions"
                            ),
                            isRefreshing = false
                        )
                    }
                }
                .collectLatest { (transactions, currency) ->
                    updateState {
                        it.copy(
                            allTransactions = transactions,
                            selectedCurrency = currency
                        )
                    }
                    recalculateList()

                    // If we have a selection, ensure its details are fresh
                    state.value.selectedId?.let { id ->
                        syncDetailsFromCacheOrFetch(id, preferCache = true)
                    }
                }
        }
    }

    fun loadAll() {
        // With reactive flow, this is a no-op or can trigger refresh if needed
        recalculateList()
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
                        updateState { it.copy(detailsUi = UiState.Success(tx)) }
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
                    // The reactive flow will automatically update allTransactions,
                    // but we can set the success flag immediately
                    updateState { old ->
                        val wasSelected = old.selectedId == expenseId

                        old.copy(
                            selectedId = if (wasSelected) null else old.selectedId,
                            detailsUi = if (wasSelected) UiState.Idle else old.detailsUi,
                            isDeleting = false,
                            deleteSuccess = true,
                            message = "Transaction deleted"
                        )
                    }
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