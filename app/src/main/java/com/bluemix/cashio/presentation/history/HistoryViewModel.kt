//package com.bluemix.cashio.presentation.history
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.bluemix.cashio.core.common.Result
//import com.bluemix.cashio.domain.model.Expense
//import com.bluemix.cashio.domain.model.TransactionType
//import com.bluemix.cashio.domain.repository.ExpenseRepository
//import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesUseCase
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import java.time.LocalDate
//
//data class HistoryState(
//    val allTransactions: Map<LocalDate, List<Expense>> = emptyMap(),
//    val filteredTransactions: List<Pair<LocalDate, List<Expense>>> = emptyList(),
//    val spendingByDate: Map<LocalDate, Double> = emptyMap(), // EXPENSE-only for heatmap
//    val selectedDate: LocalDate? = null,
//    val isLoading: Boolean = false,
//    val errorMessage: String? = null
//)
//
//class HistoryViewModel(
//    private val expenseRepository: ExpenseRepository,
//    private val observeExpensesUseCase: ObserveExpensesUseCase
//) : ViewModel() {
//
//    private val _state = MutableStateFlow(HistoryState())
//    val state: StateFlow<HistoryState> = _state.asStateFlow()
//
//
//    init {
//        observeAllTransactions()
//    }
//
//    private fun observeAllTransactions() {
//        viewModelScope.launch {
//            _state.update { it.copy(isLoading = true, errorMessage = null) }
//
//            observeExpensesUseCase().collect { transactions ->
//                val grouped = transactions
//                    .groupBy { it.date.toLocalDate() }
//                    .mapValues { (_, list) -> list.sortedByDescending { it.date } }
//
//                val spendingMap = grouped.mapValues { (_, items) ->
//                    items.asSequence()
//                        .filter { it.transactionType == TransactionType.EXPENSE }
//                        .sumOf { it.amount }
//                }
//
//                _state.update { old ->
//                    val newState = old.copy(
//                        allTransactions = grouped,
//                        spendingByDate = spendingMap,
//                        isLoading = false,
//                        errorMessage = null
//                    )
//                    newState.copy(filteredTransactions = computeFilteredTransactions(newState))
//                }
//            }
//        }
//    }
//
////
////    init {
////        loadAllTransactions()
////    }
////    fun loadAllTransactions() {
////        viewModelScope.launch {
////            _state.update { it.copy(isLoading = true, errorMessage = null) }
////
////            when (val result = expenseRepository.getAllExpenses()) {
////                is Result.Success -> {
////                    val transactions = result.data
////
////                    // Group by LocalDate
////                    val grouped = transactions
////                        .groupBy { it.date.toLocalDate() }
////                        .mapValues { (_, list) -> list.sortedByDescending { it.date } }
////
////                    // Heatmap spending = EXPENSE-only
////                    val spendingMap = grouped.mapValues { (_, items) ->
////                        items
////                            .asSequence()
////                            .filter { it.transactionType == TransactionType.EXPENSE }
////                            .sumOf { it.amount }
////                    }
////
////                    _state.update { old ->
////                        val newState = old.copy(
////                            allTransactions = grouped,
////                            spendingByDate = spendingMap,
////                            isLoading = false,
////                            errorMessage = null
////                        )
////                        newState.copy(
////                            filteredTransactions = computeFilteredTransactions(newState),
////                        )
////                    }
////                }
////
////                is Result.Error -> {
////                    _state.update {
////                        it.copy(
////                            isLoading = false,
////                            errorMessage = result.message ?: "Failed to load transactions"
////                        )
////                    }
////                }
////
////                else -> {
////                    _state.update { it.copy(isLoading = false) }
////                }
////            }
////        }
////    }
//
//    /**
//     * Click a date:
//     * - If same date is clicked again => unselect (show all)
//     * - Else select that date
//     */
//    fun onDateClicked(date: LocalDate) {
//        _state.update { old ->
//            val newSelected = if (old.selectedDate == date) null else date
//            val newState = old.copy(selectedDate = newSelected)
//            newState.copy(filteredTransactions = computeFilteredTransactions(newState))
//        }
//    }
//
//    fun showAllTransactions() {
//        _state.update { old ->
//            val newState = old.copy(selectedDate = null)
//            newState.copy(filteredTransactions = computeFilteredTransactions(newState))
//        }
//    }
//
//    private fun computeFilteredTransactions(state: HistoryState): List<Pair<LocalDate, List<Expense>>> {
//        val selected = state.selectedDate
//
//        return if (selected == null) {
//            state.allTransactions
//                .toList()
//                .sortedByDescending { it.first }
//        } else {
//            state.allTransactions[selected]
//                ?.let { listOf(selected to it) }
//                ?: emptyList()
//        }
//    }
//
//    fun scrollToToday() {
//        // Keep scroll behavior in Composable (listState/calendarState).
//        // If you want, you can expose an event flow for "scrollToTodayRequested".
//    }
//}
package com.bluemix.cashio.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HistoryState(
    val allTransactions: Map<LocalDate, List<Expense>> = emptyMap(),
    val filteredTransactions: List<Pair<LocalDate, List<Expense>>> = emptyList(),
    val spendingByDate: Map<LocalDate, Double> = emptyMap(),
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HistoryViewModel(
    private val observeExpensesUseCase: ObserveExpensesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState(isLoading = true))
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        observeAllTransactions()
    }

    private fun observeAllTransactions() {
        viewModelScope.launch {
            observeExpensesUseCase()
                .distinctUntilChanged()
                .collectLatest { transactions ->
                    val grouped = transactions
                        .groupBy { it.date.toLocalDate() }
                        .mapValues { (_, list) -> list.sortedByDescending { it.date } }

                    val spendingMap = grouped.mapValues { (_, items) ->
                        items.asSequence()
                            .filter { it.transactionType == TransactionType.EXPENSE }
                            .sumOf { it.amount }
                    }

                    _state.update { old ->
                        val newState = old.copy(
                            allTransactions = grouped,
                            spendingByDate = spendingMap,
                            isLoading = false,
                            errorMessage = null
                        )
                        newState.copy(filteredTransactions = computeFilteredTransactions(newState))
                    }
                }
        }
    }

    fun onDateClicked(date: LocalDate) {
        _state.update { old ->
            val newSelected = if (old.selectedDate == date) null else date
            val newState = old.copy(selectedDate = newSelected)
            newState.copy(filteredTransactions = computeFilteredTransactions(newState))
        }
    }

    fun showAllTransactions() {
        _state.update { old ->
            val newState = old.copy(selectedDate = null)
            newState.copy(filteredTransactions = computeFilteredTransactions(newState))
        }
    }

    private fun computeFilteredTransactions(state: HistoryState): List<Pair<LocalDate, List<Expense>>> {
        val selected = state.selectedDate
        return if (selected == null) {
            state.allTransactions.toList().sortedByDescending { it.first }
        } else {
            state.allTransactions[selected]?.let { listOf(selected to it) } ?: emptyList()
        }
    }
}
