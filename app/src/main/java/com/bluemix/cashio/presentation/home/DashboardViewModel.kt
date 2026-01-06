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
import java.time.LocalTime
import kotlin.math.absoluteValue

/**
 * UI State for the Dashboard screen.
 *
 * @property totalExpenses The total amount spent in the current month.
 * @property walletBalance The current available balance (aggregated or manually set).
 * @property percentageChange The percentage difference in spending compared to the previous month.
 * @property isIncrease True if spending has increased compared to last month (visualized as Red/Bad).
 * @property recentExpenses The list of 5 most recent transactions for the selected range.
 * @property selectedDateRange The time filter for the recent transactions list.
 * @property isRefreshingSms True when the background SMS parsing job is active.
 * @property smsRefreshMessage One-time event message showing the result of the SMS sync.
 */
data class DashboardState(
    // Summary Metrics
    val totalExpenses: Double = 0.0,
    val walletBalance: Double = 0.0,
    val percentageChange: Float = 0f,
    val isIncrease: Boolean = false,

    // Recent Activity
    val recentExpenses: UiState<List<Expense>> = UiState.Idle,
    val selectedDateRange: DateRange = DateRange.THIS_MONTH,

    // Sync Status
    val isRefreshingSms: Boolean = false,
    val smsRefreshMessage: String? = null
)

/**
 * ViewModel for the main Dashboard.
 *
 * Responsibilities:
 * 1. Aggregates monthly statistics (Current vs Previous month) for the Hero card.
 * 2. Fetches a limited list of recent transactions.
 * 3. Triggers the [RefreshExpensesFromSmsUseCase] to sync new data from the device.
 */
class DashboardViewModel(
    private val observeExpensesUseCase: ObserveExpensesUseCase,
    private val observeExpensesByDateRangeUseCase: ObserveExpensesByDateRangeUseCase,
    private val refreshExpensesFromSmsUseCase: RefreshExpensesFromSmsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    // Internal trigger to force re-fetching recent transactions (e.g. after error)
    private val retryTrigger = MutableStateFlow(0)

    init {
        observeRecentTransactions()
        observeMonthlyStats()
    }

    /* -------------------------------------------------------------------------- */
    /* Recent Transactions Logic                                                  */
    /* -------------------------------------------------------------------------- */

    private fun observeRecentTransactions() {
        viewModelScope.launch {
            // Re-fetch whenever the date range changes OR a retry is triggered
            combine(
                state.map { it.selectedDateRange }.distinctUntilChanged(),
                retryTrigger
            ) { range, _ -> range }
                .collectLatest { range ->
                    updateState { it.copy(recentExpenses = UiState.Loading) }

                    val (start, end) = range.getDateBounds()

                    observeExpensesByDateRangeUseCase(
                        ObserveExpensesByDateRangeUseCase.Params(start, end)
                    ).collectLatest { transactions ->
                        // sorting logic
                        val recent = transactions.sortedByDescending { it.date }.take(5)
                        updateState { it.copy(recentExpenses = UiState.Success(recent)) }
                    }
                }
        }
    }

    fun changeDateRange(range: DateRange) {
        if (state.value.selectedDateRange == range) return
        updateState { it.copy(selectedDateRange = range) }
    }

    fun retryRecent() {
        retryTrigger.update { it + 1 }
    }

    /* -------------------------------------------------------------------------- */
    /* Monthly Statistics Logic                                                   */
    /* -------------------------------------------------------------------------- */

    /**
     * Observes the full transaction stream to calculate the "This Month vs Last Month" snapshot.
     */
    private fun observeMonthlyStats() {
        viewModelScope.launch {
            observeExpensesUseCase()
                .distinctUntilChanged()
                .collectLatest { allExpenses ->
                    val now = LocalDateTime.now()

                    // Define Time Windows
                    val (thisStart, thisEnd) = getMonthBounds(now)
                    val (lastStart, lastEnd) = getMonthBounds(now.minusMonths(1))

                    // Calculate Totals
                    val thisMonthTotal = calculateTotal(allExpenses, thisStart, thisEnd)
                    val lastMonthTotal = calculateTotal(allExpenses, lastStart, lastEnd)

                    // Calculate Trend
                    val (pctChange, isIncrease) = calculatePercentageChange(
                        thisMonthTotal,
                        lastMonthTotal
                    )

                    // TODO: Connect to real Wallet/Budget source in future modules
                    val walletBalance = 5631.22

                    updateState {
                        it.copy(
                            totalExpenses = thisMonthTotal,
                            walletBalance = walletBalance,
                            percentageChange = pctChange,
                            isIncrease = isIncrease
                        )
                    }
                }
        }
    }

    /* -------------------------------------------------------------------------- */
    /* SMS Sync Logic                                                             */
    /* -------------------------------------------------------------------------- */

    fun refreshFromSms() {
        if (state.value.isRefreshingSms) return

        viewModelScope.launch {
            updateState { it.copy(isRefreshingSms = true, smsRefreshMessage = null) }

            when (val result = refreshExpensesFromSmsUseCase()) {
                is Result.Success -> {
                    val count = result.data
                    val msg = if (count > 0) {
                        "Imported $count new transaction${if (count > 1) "s" else ""}"
                    } else {
                        "No new transactions found"
                    }
                    updateState { it.copy(isRefreshingSms = false, smsRefreshMessage = msg) }
                }

                is Result.Error -> {
                    val msg = "Sync failed: ${result.message ?: "Unknown error"}"
                    updateState { it.copy(isRefreshingSms = false, smsRefreshMessage = msg) }
                }

                else -> updateState { it.copy(isRefreshingSms = false) }
            }
        }
    }

    fun clearSmsRefreshMessage() = updateState { it.copy(smsRefreshMessage = null) }

    /* -------------------------------------------------------------------------- */
    /* Helpers                                                                    */
    /* -------------------------------------------------------------------------- */

    private fun updateState(transform: (DashboardState) -> DashboardState) {
        _state.update(transform)
    }

    /**
     * Sums up EXPENSE transactions within the given time window.
     */
    private fun calculateTotal(
        expenses: List<Expense>,
        start: LocalDateTime,
        end: LocalDateTime
    ): Double {
        return expenses.asSequence()
            .filter { it.transactionType == TransactionType.EXPENSE }
            .filter { !it.date.isBefore(start) && !it.date.isAfter(end) }
            .sumOf { it.amount }
    }

    /**
     * Calculates percentage delta. Returns 100% if previous was 0 but current > 0.
     */
    private fun calculatePercentageChange(current: Double, previous: Double): Pair<Float, Boolean> {
        val rawChange = when {
            previous > 0 -> ((current - previous) / previous * 100).toFloat()
            current > 0 -> 100f
            else -> 0f
        }
        return rawChange.absoluteValue to (rawChange > 0)
    }

    /**
     * Returns the exact start (00:00:00) and end (23:59:59.999) of the month for the given date.
     */
    private fun getMonthBounds(date: LocalDateTime): Pair<LocalDateTime, LocalDateTime> {
        val start = date.withDayOfMonth(1).toLocalDate().atStartOfDay()
        val end = date.withDayOfMonth(date.toLocalDate().lengthOfMonth())
            .toLocalDate()
            .atTime(LocalTime.MAX)
        return start to end
    }
}