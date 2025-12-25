package com.bluemix.cashio.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.domain.usecase.expense.DeleteExpenseUseCase
import com.bluemix.cashio.domain.usecase.expense.GetExpenseByIdUseCase
import com.bluemix.cashio.domain.usecase.expense.GetExpensesUseCase
import com.bluemix.cashio.presentation.common.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TxTypeFilter { ALL, EXPENSE, INCOME }

enum class TxSortBy {
    DATE_DESC, DATE_ASC,
    AMOUNT_DESC, AMOUNT_ASC,
    TITLE_ASC, TITLE_DESC
}
data class TransactionState(
    // List screen state
    val all: List<Expense> = emptyList(),
    val list: UiState<List<Expense>> = UiState.Idle,
    val query: String = "",
    val typeFilter: TxTypeFilter = TxTypeFilter.ALL,
    val sortBy: TxSortBy = TxSortBy.DATE_DESC,
    val isRefreshing: Boolean = false,

    // Details screen state
    val selectedId: String? = null,
    val details: UiState<Expense> = UiState.Idle,

    // Delete state
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,

    // optional: snack/banner
    val message: String? = null
)


class TransactionViewModel(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val getExpenseByIdUseCase: GetExpenseByIdUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionState())
    val state: StateFlow<TransactionState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadAll()
    }

    /* ----------------------------- LIST ----------------------------- */

    fun loadAll() {
        viewModelScope.launch {
            _state.update { it.copy(list = UiState.Loading, message = null) }

            when (val result = getExpensesUseCase()) {
                is Result.Success -> {
                    _state.update { it.copy(all = result.data) }
                    applyFiltersNow()

                    // If details screen is open, refresh it from new cache.
                    _state.value.selectedId?.let { id ->
                        syncDetailsFromCacheOrFetch(id, preferCache = true)
                    }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(list = UiState.Error(result.message ?: "Failed to load transactions"))
                    }
                }

                else -> Unit
            }
        }
    }
    fun deleteTransaction(expenseId: String) {
        if (_state.value.isDeleting) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isDeleting = true,
                    deleteSuccess = false,
                    message = null
                )
            }

            when (val result = deleteExpenseUseCase(expenseId)) {
                is Result.Success -> {
                    // 1) remove from cache
                    _state.update { old ->
                        val newAll = old.all.filterNot { it.id == expenseId }

                        // 2) if currently viewing details for this, clear it
                        val shouldClearDetails = old.selectedId == expenseId

                        old.copy(
                            all = newAll,
                            selectedId = if (shouldClearDetails) null else old.selectedId,
                            details = if (shouldClearDetails) UiState.Idle else old.details,
                            isDeleting = false,
                            deleteSuccess = true,
                            message = "Transaction deleted"
                        )
                    }

                    // 3) refresh list UI from updated cache
                    applyFiltersNow()
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isDeleting = false,
                            message = result.message ?: "Failed to delete transaction"
                        )
                    }
                }

                else -> {
                    _state.update { it.copy(isDeleting = false) }
                }
            }
        }
    }

    /**
     * Call after UI reacts (e.g., navigates back) so it doesn't repeat.
     */
    fun consumeDeleteSuccess() {
        _state.update { it.copy(deleteSuccess = false) }
    }

    fun refresh() {
        if (_state.value.isRefreshing) return
        _state.update { it.copy(isRefreshing = true, message = null) }

        viewModelScope.launch {
            when (val result = getExpensesUseCase()) {
                is Result.Success -> {
                    _state.update { it.copy(all = result.data, isRefreshing = false, message = "Updated") }
                    applyFiltersNow()

                    _state.value.selectedId?.let { id ->
                        syncDetailsFromCacheOrFetch(id, preferCache = true)
                    }
                }

                is Result.Error -> {
                    _state.update { it.copy(isRefreshing = false, message = result.message ?: "Refresh failed") }
                }

                else -> _state.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun setQuery(value: String) {
        _state.update { it.copy(query = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(180)
            applyFiltersNow()
        }
    }

    fun setTypeFilter(value: TxTypeFilter) {
        _state.update { it.copy(typeFilter = value) }
        applyFiltersNow()
    }

    fun setSortBy(value: TxSortBy) {
        _state.update { it.copy(sortBy = value) }
        applyFiltersNow()
    }

    private fun applyFiltersNow() {
        val s = _state.value
        val filtered = filterAndSort(
            list = s.all,
            query = s.query,
            typeFilter = s.typeFilter,
            sortBy = s.sortBy
        )
        _state.update { it.copy(list = UiState.Success(filtered)) }
    }

    private fun filterAndSort(
        list: List<Expense>,
        query: String,
        typeFilter: TxTypeFilter,
        sortBy: TxSortBy
    ): List<Expense> {
        var out = list

        // type filter
        out = when (typeFilter) {
            TxTypeFilter.ALL -> out
            TxTypeFilter.EXPENSE -> out.filter { it.transactionType == TransactionType.EXPENSE }
            TxTypeFilter.INCOME -> out.filter { it.transactionType == TransactionType.INCOME }
        }

        // search
        val q = query.trim().lowercase()
        if (q.isNotBlank()) {
            out = out.filter { tx ->
                tx.title.lowercase().contains(q) ||
                        tx.category.name.lowercase().contains(q) ||
                        (tx.merchantName?.lowercase()?.contains(q) == true) ||
                        tx.amount.toString().contains(q)
            }
        }

        // sort
        return when (sortBy) {
            TxSortBy.DATE_DESC -> out.sortedByDescending { it.date }
            TxSortBy.DATE_ASC -> out.sortedBy { it.date }
            TxSortBy.AMOUNT_DESC -> out.sortedByDescending { it.amount }
            TxSortBy.AMOUNT_ASC -> out.sortedBy { it.amount }
            TxSortBy.TITLE_ASC -> out.sortedBy { it.title }
            TxSortBy.TITLE_DESC -> out.sortedByDescending { it.title }
        }
    }

    /* ---------------------------- DETAILS ---------------------------- */

    /**
     * Call this from TransactionDetailsScreen (and also when user taps an item).
     * Works even if list isn’t loaded yet.
     */
    fun selectTransaction(expenseId: String) {
        _state.update { it.copy(selectedId = expenseId, deleteSuccess = false, message = null) }
        syncDetailsFromCacheOrFetch(expenseId, preferCache = true)
    }


    /**
     * If you want to clear selection when leaving details.
     */
    fun clearSelection() {
        _state.update { it.copy(selectedId = null, details = UiState.Idle) }
    }

    private fun syncDetailsFromCacheOrFetch(expenseId: String, preferCache: Boolean) {
        // 1) Try cache first (instant)
        val cached = _state.value.all.firstOrNull { it.id == expenseId }
        if (preferCache && cached != null) {
            _state.update { it.copy(details = UiState.Success(cached)) }
            return
        }

        // 2) Fetch from repo
        viewModelScope.launch {
            _state.update { it.copy(details = UiState.Loading) }

            when (val result = getExpenseByIdUseCase(expenseId)) {
                is Result.Success -> {
                    val tx = result.data
                    if (tx == null) {
                        _state.update { it.copy(details = UiState.Error("Transaction not found")) }
                        return@launch
                    }

                    // update cache so list/details stay consistent
                    _state.update { old ->
                        val replaced = old.all.toMutableList().apply {
                            val idx = indexOfFirst { it.id == expenseId }
                            if (idx >= 0) set(idx, tx) else add(0, tx)
                        }
                        old.copy(all = replaced)
                    }
                    applyFiltersNow()
                    _state.update { it.copy(details = UiState.Success(tx)) }
                }

                is Result.Error -> {
                    _state.update { it.copy(details = UiState.Error(result.message ?: "Failed to load details")) }
                }

                else -> Unit
            }
        }
    }

    /* ---------------------------- MISC ---------------------------- */

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}
