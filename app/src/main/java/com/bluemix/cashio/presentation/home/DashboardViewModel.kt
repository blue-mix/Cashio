package com.bluemix.cashio.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Currency
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesByDateRangeUseCase
import com.bluemix.cashio.domain.usecase.expense.RefreshExpensesFromSmsUseCase
import com.bluemix.cashio.domain.usecase.preferences.ObserveSelectedCurrencyUseCase
import com.bluemix.cashio.presentation.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import kotlin.math.absoluteValue

/**
 * UI State for the Dashboard screen.
 *
 * All monetary values are in **paise (Long)** — the smallest currency unit.
 *
 * @property totalExpensesPaise Total amount spent in the current month (paise).
 * @property percentageChange Percentage difference vs last month (0-100+).
 * @property isIncrease True if spending increased compared to last month.
 * @property recentExpenses List of recent transactions.
 * @property selectedCurrency The user's selected currency for formatting.
 * @property isRefreshingSms True when SMS sync is active.
 * @property smsRefreshMessage One-time feedback message from SMS sync.
 */
data class DashboardState(
    // Summary Metrics (in paise)
    val totalExpensesPaise: Long = 0L,
    val percentageChange: Float = 0f,
    val isIncrease: Boolean = false,

    // Recent Activity
    val recentExpenses: UiState<List<Expense>> = UiState.Idle,

    // Currency
    val selectedCurrency: Currency = Currency.INR,

    // Sync Status
    val isRefreshingSms: Boolean = false,
    val smsRefreshMessage: String? = null
)

/**
 * ViewModel for the main Dashboard.
 *
 * Responsibilities:
 * 1. Aggregates monthly statistics (Current vs Previous month).
 * 2. Fetches recent transactions for the current month.
 * 3. Triggers SMS sync via [RefreshExpensesFromSmsUseCase].
 *
 * **All monetary values use paise (Long) internally.**
 */
class DashboardViewModel(
    private val observeExpensesByDateRangeUseCase: ObserveExpensesByDateRangeUseCase,
    private val observeSelectedCurrencyUseCase: ObserveSelectedCurrencyUseCase,
    private val refreshExpensesFromSmsUseCase: RefreshExpensesFromSmsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    // Internal trigger to force re-fetching (e.g., after error)
    private val retryTrigger = MutableStateFlow(0)

    init {
        observeDashboardData()
    }

    /* -------------------------------------------------------------------------- */
    /* Core Data Observation                                                      */
    /* -------------------------------------------------------------------------- */

    private fun observeDashboardData() {
        viewModelScope.launch {
            updateState { it.copy(recentExpenses = UiState.Loading) }

            val now = LocalDateTime.now()

            // Define month bounds
            val thisMonthStart =
                now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay()
            val thisMonthEnd =
                now.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atTime(23, 59, 59)

            val lastMonthStart = now.minusMonths(1)
                .with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay()
            val lastMonthEnd = now.minusMonths(1)
                .with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atTime(23, 59, 59)

            // Observe this month's expenses
            val thisMonthFlow = observeExpensesByDateRangeUseCase(
                ObserveExpensesByDateRangeUseCase.Params(thisMonthStart, thisMonthEnd)
            )

            // Observe last month's expenses
            val lastMonthFlow = observeExpensesByDateRangeUseCase(
                ObserveExpensesByDateRangeUseCase.Params(lastMonthStart, lastMonthEnd)
            )

            // Observe selected currency
            val currencyFlow = observeSelectedCurrencyUseCase()

            // Combine retry trigger to allow manual refresh
            combine(
                thisMonthFlow,
                lastMonthFlow,
                currencyFlow,
                retryTrigger.map { }
            ) { thisMonth, lastMonth, currency, _ ->
                Triple(thisMonth, lastMonth, currency)
            }
                .distinctUntilChanged()
                .catch { t ->
                    updateState {
                        it.copy(
                            recentExpenses = UiState.Error(t.message ?: "Failed to load data")
                        )
                    }
                }
                .collectLatest { (thisMonth, lastMonth, currency) ->
                    // Calculate totals
                    val thisMonthTotal = calculateExpenseTotal(thisMonth)
                    val lastMonthTotal = calculateExpenseTotal(lastMonth)

                    // Calculate percentage change
                    val (pctChange, isIncrease) = calculatePercentageChange(
                        thisMonthTotal,
                        lastMonthTotal
                    )

                    // Get recent 5 transactions (newest first)
                    val recent = thisMonth
                        .sortedByDescending { it.date }
                        .take(5)

                    updateState {
                        it.copy(
                            totalExpensesPaise = thisMonthTotal,
                            percentageChange = pctChange,
                            isIncrease = isIncrease,
                            recentExpenses = UiState.Success(recent),
                            selectedCurrency = currency
                        )
                    }
                }
        }
    }

    fun retryRecent() {
        retryTrigger.update { it + 1 }
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
                    val msg = when {
                        count > 0 -> "Imported $count new transaction${if (count > 1) "s" else ""}"
                        else -> "No new transactions found"
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

    fun clearSmsRefreshMessage() {
        updateState { it.copy(smsRefreshMessage = null) }
    }

    /* -------------------------------------------------------------------------- */
    /* Helpers                                                                    */
    /* -------------------------------------------------------------------------- */

    private fun updateState(transform: (DashboardState) -> DashboardState) {
        _state.update(transform)
    }

    /**
     * Sums up EXPENSE transactions only (income excluded).
     * Returns total in **paise**.
     */
    private fun calculateExpenseTotal(expenses: List<Expense>): Long {
        return expenses
            .filter { it.transactionType == TransactionType.EXPENSE }
            .sumOf { it.amountPaise }
    }

    /**
     * Calculates percentage delta between current and previous period.
     * Returns (absolutePercentage, isIncrease).
     *
     * Special cases:
     * - If previous = 0 and current > 0 → 100% increase
     * - If both are 0 → 0% (no change)
     */
    private fun calculatePercentageChange(
        currentPaise: Long,
        previousPaise: Long
    ): Pair<Float, Boolean> {
        if (previousPaise == 0L) {
            return if (currentPaise > 0L) 100f to true else 0f to false
        }

        val rawChange = ((currentPaise - previousPaise).toFloat() / previousPaise) * 100f
        return rawChange.absoluteValue to (rawChange > 0f)
    }
}