////package com.bluemix.cashio.presentation.home
////
////import androidx.lifecycle.ViewModel
////import androidx.lifecycle.viewModelScope
////import com.bluemix.cashio.core.common.Result
////import com.bluemix.cashio.domain.model.DateRange
////import com.bluemix.cashio.domain.model.Expense
////import com.bluemix.cashio.domain.model.TransactionType
////import com.bluemix.cashio.domain.usecase.expense.GetExpensesByDateRangeUseCase
////import com.bluemix.cashio.domain.usecase.expense.RefreshExpensesFromSmsUseCase
////import com.bluemix.cashio.presentation.common.UiState
////import kotlinx.coroutines.async
////import kotlinx.coroutines.flow.MutableStateFlow
////import kotlinx.coroutines.flow.StateFlow
////import kotlinx.coroutines.flow.asStateFlow
////import kotlinx.coroutines.flow.update
////import kotlinx.coroutines.launch
////import java.time.LocalDateTime
////import kotlin.math.absoluteValue
////
////data class DashboardState(
////    val totalExpenses: Double = 0.0,
////    val walletBalance: Double = 0.0,
////    val percentageChange: Float = 0f,
////    val isIncrease: Boolean = false,
////
////    val recentExpenses: UiState<List<Expense>> = UiState.Idle,
////
////    val isRefreshingSms: Boolean = false,
////    val smsRefreshMessage: String? = null,
////
////    val selectedDateRange: DateRange = DateRange.THIS_MONTH
////)
////
////class DashboardViewModel(
////    private val getExpensesByDateRangeUseCase: GetExpensesByDateRangeUseCase,
////    private val refreshExpensesFromSmsUseCase: RefreshExpensesFromSmsUseCase
////) : ViewModel() {
////
////    private val _state = MutableStateFlow(DashboardState())
////    val state: StateFlow<DashboardState> = _state.asStateFlow()
////
////    init {
////        loadDashboardData()
////    }
////
////    fun loadDashboardData() {
////        loadRecentExpenses()
////        calculateMonthlyComparison()
////    }
////
////    /**
////     * OPTION B:
////     * Recent list shows BOTH income + expense.
////     */
////    private fun loadRecentExpenses() {
////        viewModelScope.launch {
////            _state.update { it.copy(recentExpenses = UiState.Loading) }
////
////            val (startDate, endDate) = _state.value.selectedDateRange.getDateBounds()
////            val params = GetExpensesByDateRangeUseCase.Params(startDate, endDate)
////
////            when (val result = getExpensesByDateRangeUseCase(params)) {
////                is Result.Success -> {
////                    val recent = result.data
////                        .sortedByDescending { it.date }
////                        .take(6)
////
////                    _state.update { it.copy(recentExpenses = UiState.Success(recent)) }
////                }
////
////                is Result.Error -> {
////                    _state.update {
////                        it.copy(
////                            recentExpenses = UiState.Error(
////                                result.message ?: "Failed to load transactions"
////                            )
////                        )
////                    }
////                }
////
////                else -> Unit
////            }
////        }
////    }
////
////    /**
////     * This Month vs Last Month comparison for EXPENSES only.
////     * (So "This Month Spend" is correct even when you store incomes too.)
////     */
////    private fun calculateMonthlyComparison() {
////        viewModelScope.launch {
////            val now = LocalDateTime.now()
////
////            val thisMonthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay()
////            val thisMonthEnd = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
////                .toLocalDate()
////                .atTime(23, 59, 59)
////
////            val lastMonth = now.minusMonths(1)
////            val lastMonthStart = lastMonth.withDayOfMonth(1).toLocalDate().atStartOfDay()
////            val lastMonthEnd = lastMonth.withDayOfMonth(lastMonth.toLocalDate().lengthOfMonth())
////                .toLocalDate()
////                .atTime(23, 59, 59)
////
////            val thisMonthDeferred = async {
////                getExpensesByDateRangeUseCase(
////                    GetExpensesByDateRangeUseCase.Params(thisMonthStart, thisMonthEnd)
////                )
////            }
////            val lastMonthDeferred = async {
////                getExpensesByDateRangeUseCase(
////                    GetExpensesByDateRangeUseCase.Params(lastMonthStart, lastMonthEnd)
////                )
////            }
////
////            val thisMonthResult = thisMonthDeferred.await()
////            val lastMonthResult = lastMonthDeferred.await()
////
////            val thisMonthTotal = (thisMonthResult as? Result.Success)?.data
////                ?.asSequence()
////                ?.filter { it.transactionType == TransactionType.EXPENSE }
////                ?.sumOf { it.amount }
////                ?: 0.0
////
////            val lastMonthTotal = (lastMonthResult as? Result.Success)?.data
////                ?.asSequence()
////                ?.filter { it.transactionType == TransactionType.EXPENSE }
////                ?.sumOf { it.amount }
////                ?: 0.0
////
////            val rawChange = when {
////                lastMonthTotal > 0 ->
////                    ((thisMonthTotal - lastMonthTotal) / lastMonthTotal * 100).toFloat()
////
////                thisMonthTotal > 0 -> 100f
////                else -> 0f
////            }
////
////            // TODO: Replace with real wallet source
////            val walletBalance = 5631.22
////
////            _state.update {
////                it.copy(
////                    totalExpenses = thisMonthTotal,
////                    walletBalance = walletBalance,
////                    percentageChange = rawChange.absoluteValue,
////                    isIncrease = rawChange > 0
////                )
////            }
////        }
////    }
////
////    fun changeDateRange(dateRange: DateRange) {
////        if (dateRange == _state.value.selectedDateRange) return
////        _state.update { it.copy(selectedDateRange = dateRange) }
////        loadRecentExpenses()
////    }
////
////    fun refreshFromSms() {
////        viewModelScope.launch {
////            _state.update { it.copy(isRefreshingSms = true, smsRefreshMessage = null) }
////
////            when (val result = refreshExpensesFromSmsUseCase()) {
////                is Result.Success -> {
////                    val count = result.data
////                    val message = if (count > 0) {
////                        "Added $count new expense${if (count > 1) "s" else ""} from SMS"
////                    } else {
////                        "No new expenses found"
////                    }
////
////                    _state.update { it.copy(isRefreshingSms = false, smsRefreshMessage = message) }
////                    loadDashboardData()
////                }
////
////                is Result.Error -> {
////                    _state.update {
////                        it.copy(
////                            isRefreshingSms = false,
////                            smsRefreshMessage = "Failed: ${result.message}"
////                        )
////                    }
////                }
////
////                else -> {
////                    _state.update { it.copy(isRefreshingSms = false) }
////                }
////            }
////        }
////    }
////
////    fun clearSmsRefreshMessage() {
////        _state.update { it.copy(smsRefreshMessage = null) }
////    }
////
////    fun refresh() {
////        loadDashboardData()
////    }
////}
////package com.bluemix.cashio.presentation.home
////
////import androidx.lifecycle.ViewModel
////import androidx.lifecycle.viewModelScope
////import com.bluemix.cashio.domain.model.DateRange
////import com.bluemix.cashio.domain.model.Expense
////import com.bluemix.cashio.domain.model.TransactionType
////import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesByDateRangeUseCase
////import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesUseCase
////import com.bluemix.cashio.domain.usecase.expense.RefreshExpensesFromSmsUseCase
////import com.bluemix.cashio.presentation.common.UiState
////import kotlinx.coroutines.flow.MutableStateFlow
////import kotlinx.coroutines.flow.StateFlow
////import kotlinx.coroutines.flow.asStateFlow
////import kotlinx.coroutines.flow.collectLatest
////import kotlinx.coroutines.flow.distinctUntilChanged
////import kotlinx.coroutines.flow.map
////import kotlinx.coroutines.flow.update
////import kotlinx.coroutines.launch
////import java.time.LocalDateTime
////import kotlin.math.absoluteValue
////
////data class DashboardState(
////    val totalExpenses: Double = 0.0,
////    val walletBalance: Double = 0.0,
////    val percentageChange: Float = 0f,
////    val isIncrease: Boolean = false,
////
////    val recentExpenses: UiState<List<Expense>> = UiState.Idle,
////
////    val isRefreshingSms: Boolean = false,
////    val smsRefreshMessage: String? = null,
////
////    val selectedDateRange: DateRange = DateRange.THIS_MONTH
////)
////
////class DashboardViewModel(
////    private val observeExpensesUseCase: ObserveExpensesUseCase,
////    private val observeExpensesByDateRangeUseCase: ObserveExpensesByDateRangeUseCase,
////    private val refreshExpensesFromSmsUseCase: RefreshExpensesFromSmsUseCase
////) : ViewModel() {
////
////    private val _state = MutableStateFlow(DashboardState())
////    val state: StateFlow<DashboardState> = _state.asStateFlow()
////
////    init {
////        observeRecentForSelectedRange()
////        observeMonthComparisonLive()
////    }
////
////    /**
////     * Live "recent" list driven by selected date range + Realm flow.
////     * When you add/update/delete an expense, UI updates instantly.
////     */
////     fun observeRecentForSelectedRange() {
////        viewModelScope.launch {
////            _state
////                .map { it.selectedDateRange }
////                .distinctUntilChanged()
////                .collectLatest { range ->
////                    _state.update { it.copy(recentExpenses = UiState.Loading) }
////
////                    val (startDate, endDate) = range.getDateBounds()
////
////                    observeExpensesByDateRangeUseCase(
////                        ObserveExpensesByDateRangeUseCase.Params(startDate, endDate)
////                    ).collectLatest { inRange ->
////                        val recent = inRange
////                            .sortedByDescending { it.date }
////                            .take(4)
////
////                        _state.update { it.copy(recentExpenses = UiState.Success(recent)) }
////                    }
////                }
////        }
////    }
////
////    /**
////     * Live "This Month vs Last Month" comparison (EXPENSE only),
////     * computed from the full observed stream (no one-shot calls).
////     */
////    private fun observeMonthComparisonLive() {
////        viewModelScope.launch {
////            observeExpensesUseCase().collectLatest { all ->
////                val now = LocalDateTime.now()
////
////                val thisMonthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay()
////                val thisMonthEnd = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
////                    .toLocalDate()
////                    .atTime(23, 59, 59)
////
////                val lastMonth = now.minusMonths(1)
////                val lastMonthStart = lastMonth.withDayOfMonth(1).toLocalDate().atStartOfDay()
////                val lastMonthEnd = lastMonth.withDayOfMonth(lastMonth.toLocalDate().lengthOfMonth())
////                    .toLocalDate()
////                    .atTime(23, 59, 59)
////
////                fun inRange(e: Expense, start: LocalDateTime, end: LocalDateTime): Boolean {
////                    return (e.date.isEqual(start) || e.date.isAfter(start)) &&
////                            (e.date.isEqual(end) || e.date.isBefore(end))
////                }
////
////                val thisMonthTotal = all.asSequence()
////                    .filter { it.transactionType == TransactionType.EXPENSE }
////                    .filter { inRange(it, thisMonthStart, thisMonthEnd) }
////                    .sumOf { it.amount }
////
////                val lastMonthTotal = all.asSequence()
////                    .filter { it.transactionType == TransactionType.EXPENSE }
////                    .filter { inRange(it, lastMonthStart, lastMonthEnd) }
////                    .sumOf { it.amount }
////
////                val rawChange = when {
////                    lastMonthTotal > 0 ->
////                        ((thisMonthTotal - lastMonthTotal) / lastMonthTotal * 100).toFloat()
////                    thisMonthTotal > 0 -> 100f
////                    else -> 0f
////                }
////
////                // TODO: Replace with real wallet source later
////                val walletBalance = 5631.22
////
////                _state.update {
////                    it.copy(
////                        totalExpenses = thisMonthTotal,
////                        walletBalance = walletBalance,
////                        percentageChange = rawChange.absoluteValue,
////                        isIncrease = rawChange > 0
////                    )
////                }
////            }
////        }
////    }
////
////    fun changeDateRange(dateRange: DateRange) {
////        if (dateRange == _state.value.selectedDateRange) return
////        _state.update { it.copy(selectedDateRange = dateRange) }
////        // No manual reload needed; observer reacts automatically.
////    }
////
////    fun refreshFromSms() {
////        viewModelScope.launch {
////            _state.update { it.copy(isRefreshingSms = true, smsRefreshMessage = null) }
////
////            when (val result = refreshExpensesFromSmsUseCase()) {
////                is com.bluemix.cashio.core.common.Result.Success -> {
////                    val count = result.data
////                    val message = if (count > 0) {
////                        "Added $count new expense${if (count > 1) "s" else ""} from SMS"
////                    } else {
////                        "No new expenses found"
////                    }
////                    _state.update { it.copy(isRefreshingSms = false, smsRefreshMessage = message) }
////                    // No need to call loadDashboardData(); flows will update automatically.
////                }
////
////                is com.bluemix.cashio.core.common.Result.Error -> {
////                    _state.update {
////                        it.copy(
////                            isRefreshingSms = false,
////                            smsRefreshMessage = "Failed: ${result.message}"
////                        )
////                    }
////                }
////
////                else -> _state.update { it.copy(isRefreshingSms = false) }
////            }
////        }
////    }
////
////    fun clearSmsRefreshMessage() {
////        _state.update { it.copy(smsRefreshMessage = null) }
////    }
////}
//package com.bluemix.cashio.presentation.home
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.bluemix.cashio.core.common.Result
//import com.bluemix.cashio.domain.model.DateRange
//import com.bluemix.cashio.domain.model.Expense
//import com.bluemix.cashio.domain.model.TransactionType
//import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesByDateRangeUseCase
//import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesUseCase
//import com.bluemix.cashio.domain.usecase.expense.RefreshExpensesFromSmsUseCase
//import com.bluemix.cashio.presentation.common.UiState
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.flow.distinctUntilChanged
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import java.time.LocalDateTime
//import kotlin.math.absoluteValue
//
//data class DashboardState(
//    val totalExpenses: Double = 0.0,
//    val walletBalance: Double = 0.0,
//    val percentageChange: Float = 0f,
//    val isIncrease: Boolean = false,
//
//    val recentExpenses: UiState<List<Expense>> = UiState.Idle,
//
//    val isRefreshingSms: Boolean = false,
//    val smsRefreshMessage: String? = null,
//
//    val selectedDateRange: DateRange = DateRange.THIS_MONTH
//)
//
//class DashboardViewModel(
//    private val observeExpensesUseCase: ObserveExpensesUseCase,
//    private val observeExpensesByDateRangeUseCase: ObserveExpensesByDateRangeUseCase,
//    private val refreshExpensesFromSmsUseCase: RefreshExpensesFromSmsUseCase
//) : ViewModel() {
//
//    private val _state = MutableStateFlow(DashboardState())
//    val state: StateFlow<DashboardState> = _state.asStateFlow()
//
//    init {
//        observeRecentForSelectedRange()
//        observeMonthComparisonLive()
//    }
//
//    // ✅ Keep private: only ONE collector for the lifetime of this VM
//    private fun observeRecentForSelectedRange() {
//        viewModelScope.launch {
//            _state
//                .map { it.selectedDateRange }
//                .distinctUntilChanged()
//                .collectLatest { range ->
//                    _state.update { it.copy(recentExpenses = UiState.Loading) }
//
//                    val (startDate, endDate) = range.getDateBounds()
//
//                    observeExpensesByDateRangeUseCase(
//                        ObserveExpensesByDateRangeUseCase.Params(startDate, endDate)
//                    ).collectLatest { inRange ->
//                        val recent = inRange
//                            .sortedByDescending { it.date }
//                            .take(4)
//
//                        _state.update { it.copy(recentExpenses = UiState.Success(recent)) }
//                    }
//                }
//        }
//    }
//
//    private fun observeMonthComparisonLive() {
//        viewModelScope.launch {
//            observeExpensesUseCase()
//                .distinctUntilChanged()
//                .collectLatest { all ->
//                    val now = LocalDateTime.now()
//
//                    val thisMonthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay()
//                    val thisMonthEnd = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
//                        .toLocalDate()
//                        .atTime(23, 59, 59)
//
//                    val lastMonth = now.minusMonths(1)
//                    val lastMonthStart = lastMonth.withDayOfMonth(1).toLocalDate().atStartOfDay()
//                    val lastMonthEnd =
//                        lastMonth.withDayOfMonth(lastMonth.toLocalDate().lengthOfMonth())
//                            .toLocalDate()
//                            .atTime(23, 59, 59)
//
//                    fun inRange(e: Expense, start: LocalDateTime, end: LocalDateTime): Boolean {
//                        return (e.date.isEqual(start) || e.date.isAfter(start)) &&
//                                (e.date.isEqual(end) || e.date.isBefore(end))
//                    }
//
//                    val thisMonthTotal = all.asSequence()
//                        .filter { it.transactionType == TransactionType.EXPENSE }
//                        .filter { inRange(it, thisMonthStart, thisMonthEnd) }
//                        .sumOf { it.amount }
//
//                    val lastMonthTotal = all.asSequence()
//                        .filter { it.transactionType == TransactionType.EXPENSE }
//                        .filter { inRange(it, lastMonthStart, lastMonthEnd) }
//                        .sumOf { it.amount }
//
//                    val rawChange = when {
//                        lastMonthTotal > 0 ->
//                            ((thisMonthTotal - lastMonthTotal) / lastMonthTotal * 100).toFloat()
//
//                        thisMonthTotal > 0 -> 100f
//                        else -> 0f
//                    }
//
//                    val walletBalance = 5631.22 // TODO real source later
//
//                    _state.update {
//                        it.copy(
//                            totalExpenses = thisMonthTotal,
//                            walletBalance = walletBalance,
//                            percentageChange = rawChange.absoluteValue,
//                            isIncrease = rawChange > 0
//                        )
//                    }
//                }
//        }
//    }
//
//    fun changeDateRange(dateRange: DateRange) {
//        if (dateRange == _state.value.selectedDateRange) return
//        _state.update { it.copy(selectedDateRange = dateRange) }
//    }
//
//    /**
//     * ✅ UI Retry should NOT start new collectors.
//     * Just re-trigger by forcing Loading state (range observer will emit anyway).
//     */
//    fun retryRecent() {
//        _state.update { it.copy(recentExpenses = UiState.Loading) }
//        // If you want a hard retrigger even when same range:
//        _state.update { it.copy(selectedDateRange = it.selectedDateRange) }
//    }
//
//    fun refreshFromSms() {
//        viewModelScope.launch {
//            _state.update { it.copy(isRefreshingSms = true, smsRefreshMessage = null) }
//
//            when (val result = refreshExpensesFromSmsUseCase()) {
//                is Result.Success -> {
//                    val count = result.data
//                    val message = if (count > 0) {
//                        "Added $count new expense${if (count > 1) "s" else ""} from SMS"
//                    } else {
//                        "No new expenses found"
//                    }
//                    _state.update { it.copy(isRefreshingSms = false, smsRefreshMessage = message) }
//                }
//
//                is Result.Error -> {
//                    _state.update {
//                        it.copy(
//                            isRefreshingSms = false,
//                            smsRefreshMessage = "Failed: ${result.message}"
//                        )
//                    }
//                }
//
//                else -> _state.update { it.copy(isRefreshingSms = false) }
//            }
//        }
//    }
//
//    fun clearSmsRefreshMessage() {
//        _state.update { it.copy(smsRefreshMessage = null) }
//    }
//}
package com.bluemix.cashio.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.DateRange
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesByDateRangeUseCase
import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesUseCase
import com.bluemix.cashio.domain.usecase.expense.RefreshExpensesFromSmsUseCase
import com.bluemix.cashio.presentation.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.absoluteValue

data class DashboardState(
    val totalExpenses: Double = 0.0,
    val walletBalance: Double = 0.0,
    val percentageChange: Float = 0f,
    val isIncrease: Boolean = false,

    val recentExpenses: UiState<List<Expense>> = UiState.Idle,

    val isRefreshingSms: Boolean = false,
    val smsRefreshMessage: String? = null,

    val selectedDateRange: DateRange = DateRange.THIS_MONTH
)

class DashboardViewModel(
    private val observeExpensesUseCase: ObserveExpensesUseCase,
    private val observeExpensesByDateRangeUseCase: ObserveExpensesByDateRangeUseCase,
    private val refreshExpensesFromSmsUseCase: RefreshExpensesFromSmsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    // ✅ “retry token” so retry actually retriggers even if same range
    private val retryTick = MutableStateFlow(0)

    init {
        observeRecentForSelectedRange()
        observeMonthComparisonLive()
    }

    private fun observeRecentForSelectedRange() {
        viewModelScope.launch {
            combine(
                _state.map { it.selectedDateRange }.distinctUntilChanged(),
                retryTick
            ) { range, _ -> range }
                .collectLatest { range ->
                    _state.update { it.copy(recentExpenses = UiState.Loading) }

                    val (startDate, endDate) = range.getDateBounds()

                    observeExpensesByDateRangeUseCase(
                        ObserveExpensesByDateRangeUseCase.Params(startDate, endDate)
                    ).collectLatest { inRange ->
                        val recent = inRange
                            .sortedByDescending { it.date }
                            .take(4)

                        _state.update { it.copy(recentExpenses = UiState.Success(recent)) }
                    }
                }
        }
    }

    private fun observeMonthComparisonLive() {
        viewModelScope.launch {
            observeExpensesUseCase()
                .distinctUntilChanged()
                .collectLatest { all ->
                    val now = LocalDateTime.now()

                    val thisMonthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay()
                    val thisMonthEnd = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                        .toLocalDate()
                        .atTime(23, 59, 59)

                    val lastMonth = now.minusMonths(1)
                    val lastMonthStart = lastMonth.withDayOfMonth(1).toLocalDate().atStartOfDay()
                    val lastMonthEnd = lastMonth.withDayOfMonth(lastMonth.toLocalDate().lengthOfMonth())
                        .toLocalDate()
                        .atTime(23, 59, 59)

                    fun inRange(e: Expense, start: LocalDateTime, end: LocalDateTime): Boolean {
                        return (e.date.isEqual(start) || e.date.isAfter(start)) &&
                                (e.date.isEqual(end) || e.date.isBefore(end))
                    }

                    val thisMonthTotal = all.asSequence()
                        .filter { it.transactionType == TransactionType.EXPENSE }
                        .filter { inRange(it, thisMonthStart, thisMonthEnd) }
                        .sumOf { it.amount }

                    val lastMonthTotal = all.asSequence()
                        .filter { it.transactionType == TransactionType.EXPENSE }
                        .filter { inRange(it, lastMonthStart, lastMonthEnd) }
                        .sumOf { it.amount }

                    val rawChange = when {
                        lastMonthTotal > 0 ->
                            ((thisMonthTotal - lastMonthTotal) / lastMonthTotal * 100).toFloat()
                        thisMonthTotal > 0 -> 100f
                        else -> 0f
                    }

                    val walletBalance = 5631.22 // TODO real source later

                    _state.update {
                        it.copy(
                            totalExpenses = thisMonthTotal,
                            walletBalance = walletBalance,
                            percentageChange = rawChange.absoluteValue,
                            isIncrease = rawChange > 0
                        )
                    }
                }
        }
    }

    fun changeDateRange(dateRange: DateRange) {
        if (dateRange == _state.value.selectedDateRange) return
        _state.update { it.copy(selectedDateRange = dateRange) }
    }

    // ✅ This now actually works.
    fun retryRecent() {
        retryTick.update { it + 1 }
    }

    fun refreshFromSms() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshingSms = true, smsRefreshMessage = null) }

            when (val result = refreshExpensesFromSmsUseCase()) {
                is Result.Success -> {
                    val count = result.data
                    val message = if (count > 0) {
                        "Added $count new expense${if (count > 1) "s" else ""} from SMS"
                    } else {
                        "No new expenses found"
                    }
                    _state.update { it.copy(isRefreshingSms = false, smsRefreshMessage = message) }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isRefreshingSms = false,
                            smsRefreshMessage = "Failed: ${result.message}"
                        )
                    }
                }

                else -> _state.update { it.copy(isRefreshingSms = false) }
            }
        }
    }

    fun clearSmsRefreshMessage() {
        _state.update { it.copy(smsRefreshMessage = null) }
    }
}
