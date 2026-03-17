package com.bluemix.cashio.presentation.analytics.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.DateRange
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.domain.usecase.expense.GetExpensesByDateRangeUseCase
import com.bluemix.cashio.domain.usecase.expense.GetFinancialStatsUseCase
import com.bluemix.cashio.domain.usecase.preferences.ObserveSelectedCurrencyUseCase
import com.bluemix.cashio.presentation.common.UiState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ViewModel responsible for managing the UI state and business logic of the Analytics screen.
 *
 * This ViewModel coordinates the fetching of financial statistics, calculating period-over-period
 * deltas, and processing transaction data into visualization-ready chart buckets.
 *
 * **All monetary values use paise (Long) internally.**
 *
 * @property getFinancialStatsUseCase Use case for retrieving aggregated financial statistics.
 * @property getExpensesByDateRangeUseCase Use case for retrieving raw transaction lists for charting.
 * @property observeSelectedCurrencyUseCase Use case for observing user's currency preference.
 */
class AnalyticsViewModel(
    private val getFinancialStatsUseCase: GetFinancialStatsUseCase,
    private val getExpensesByDateRangeUseCase: GetExpensesByDateRangeUseCase,
    private val observeSelectedCurrencyUseCase: ObserveSelectedCurrencyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsState())

    /**
     * The immutable stream of UI state for the Analytics screen.
     */
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    init {
        observeCurrency()
        refresh()
    }

    private fun observeCurrency() {
        viewModelScope.launch {
            observeSelectedCurrencyUseCase()
                .distinctUntilChanged()
                .collectLatest { currency ->
                    updateState { it.copy(selectedCurrency = currency) }
                }
        }
    }

    /**
     * Triggers a reload of both summary statistics and chart transaction data.
     */
    fun refresh() {
        loadFinancialStats()
        loadChartTransactions()
    }

    /**
     * Updates the time granularity used for the main bar chart (e.g., Weekly, Monthly).
     *
     * If the period is different from the current one, the state is updated and
     * chart data is reloaded.
     *
     * @param period The new [ChartPeriod] to apply.
     */
    fun changeChartPeriod(period: ChartPeriod) {
        if (period == _state.value.selectedChartPeriod) return
        updateState { it.copy(selectedChartPeriod = period) }
        loadChartTransactions()
    }

    /**
     * Updates the date range used for calculating financial summary statistics.
     *
     * @param dateRange The new [DateRange] to apply for stats calculation.
     */
    fun changeDateRange(dateRange: DateRange) {
        if (dateRange == _state.value.selectedDateRange) return
        updateState { it.copy(selectedDateRange = dateRange) }
        loadFinancialStats()
    }

    /**
     * Fetches financial statistics for the selected date range and compares them
     * against the previous month to calculate deltas.
     */
    private fun loadFinancialStats() {
        viewModelScope.launch {
            updateState { it.copy(statsState = UiState.Loading) }

            val currentRange = GetFinancialStatsUseCase.Params(
                state.value.selectedDateRange
            )
            val previousRange = GetFinancialStatsUseCase.Params(
                state.value.selectedDateRange
            )

            try {
                coroutineScope {
                    val currentDeferred = async { getFinancialStatsUseCase(currentRange) }
                    val previousDeferred = async { getFinancialStatsUseCase(previousRange) }

                    val currentResult = currentDeferred.await()
                    val previousResult = previousDeferred.await()

                    if (currentResult is Result.Success) {
                        val current = currentResult.data
                        val previous = (previousResult as? Result.Success)?.data

                        // Calculate deltas in paise
                        val incomeDeltaPaise =
                            previous?.let { current.totalIncomePaise - it.totalIncomePaise } ?: 0L
                        val expenseDeltaPaise =
                            previous?.let { current.totalExpensesPaise - it.totalExpensesPaise }
                                ?: 0L

                        val topCategoryRatio =
                            if (current.totalExpensesPaise > 0 && current.topCategory != null) {
                                (current.topCategoryAmountPaise.toFloat() / current.totalExpensesPaise.toFloat())
                            } else 0f

                        updateState {
                            it.copy(
                                statsState = UiState.Success(current),
                                categoryBreakdown = current.categoryBreakdown,
                                topCategoryRatio = topCategoryRatio,
                                incomeDeltaPaise = incomeDeltaPaise,
                                expenseDeltaPaise = expenseDeltaPaise
                            )
                        }
                    } else {
                        updateState { it.copy(statsState = UiState.Error("Failed to load stats")) }
                    }
                }
            } catch (t: Throwable) {
                updateState { it.copy(statsState = UiState.Error(t.message ?: "Unknown error")) }
            }
        }
    }

    /**
     * Fetches raw transactions based on the selected chart period and processes them
     * into chart data buckets.
     */
    private fun loadChartTransactions() {
        viewModelScope.launch {
            updateState { it.copy(chartExpensesState = UiState.Loading) }

            val now = LocalDate.now()
            val period = state.value.selectedChartPeriod

            val startDate = when (period) {
                ChartPeriod.WEEKLY -> YearMonth.from(now).atDay(1)
                ChartPeriod.MONTHLY -> now.minusMonths(5).withDayOfMonth(1)
                ChartPeriod.YEARLY -> now.minusYears(3).withDayOfYear(1)
            }

            val params = GetExpensesByDateRangeUseCase.Params(
                startDate = startDate.atStartOfDay(),
                endDate = now.atTime(23, 59, 59)
            )

            when (val result = getExpensesByDateRangeUseCase(params)) {
                is Result.Success -> {
                    val transactions = result.data
                    val chartData = buildChartData(transactions, period)
                    updateState {
                        it.copy(
                            chartExpensesState = UiState.Success(transactions),
                            barChartData = chartData
                        )
                    }
                }

                is Result.Error -> {
                    updateState {
                        it.copy(
                            chartExpensesState = UiState.Error(
                                result.message ?: "Failed to load chart"
                            )
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    private fun buildChartData(expenses: List<Expense>, period: ChartPeriod): BarChartData =
        when (period) {
            ChartPeriod.WEEKLY -> buildWeeklyBuckets(expenses)
            ChartPeriod.MONTHLY -> buildMonthlyBuckets(expenses)
            ChartPeriod.YEARLY -> buildYearlyBuckets(expenses)
        }

    /**
     * Aggregates expenses into weekly buckets (approx. 5 weeks) for the current month.
     * All values in paise (Long).
     */
    private fun buildWeeklyBuckets(expenses: List<Expense>): BarChartData {
        val currentMonth = YearMonth.now()
        val totals = mutableMapOf<Int, Pair<Long, Long>>()

        expenses.forEach { e ->
            val date = e.date.toLocalDate()
            if (YearMonth.from(date) == currentMonth) {
                val weekIndex = (date.dayOfMonth - 1) / 7
                val (inc, exp) = totals.getOrDefault(weekIndex, 0L to 0L)
                totals[weekIndex] = if (e.transactionType == TransactionType.INCOME) {
                    (inc + e.amountPaise) to exp
                } else {
                    inc to (exp + e.amountPaise)
                }
            }
        }

        val weeksCount = 5
        return BarChartData(
            labels = (1..weeksCount).map { "W$it" },
            incomeDataPaise = (0 until weeksCount).map { totals[it]?.first ?: 0L },
            expenseDataPaise = (0 until weeksCount).map { totals[it]?.second ?: 0L }
        )
    }

    /**
     * Aggregates expenses into monthly buckets for the last 6 months.
     * All values in paise (Long).
     */
    private fun buildMonthlyBuckets(expenses: List<Expense>): BarChartData {
        val months = (5 downTo 0).map { YearMonth.now().minusMonths(it.toLong()) }
        val formatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())

        val totals = expenses.groupBy { YearMonth.from(it.date.toLocalDate()) }
            .mapValues { (_, list) ->
                val income = list
                    .filter { it.transactionType == TransactionType.INCOME }
                    .sumOf { it.amountPaise }
                val expense = list
                    .filter { it.transactionType == TransactionType.EXPENSE }
                    .sumOf { it.amountPaise }
                income to expense
            }

        return BarChartData(
            labels = months.map { it.format(formatter) },
            incomeDataPaise = months.map { totals[it]?.first ?: 0L },
            expenseDataPaise = months.map { totals[it]?.second ?: 0L }
        )
    }

    /**
     * Aggregates expenses into yearly buckets for the last 4 years.
     * All values in paise (Long).
     */
    private fun buildYearlyBuckets(expenses: List<Expense>): BarChartData {
        val currentYear = LocalDate.now().year
        val years = (3 downTo 0).map { currentYear - it }

        val totals = expenses.groupBy { it.date.year }
            .mapValues { (_, list) ->
                val income = list
                    .filter { it.transactionType == TransactionType.INCOME }
                    .sumOf { it.amountPaise }
                val expense = list
                    .filter { it.transactionType == TransactionType.EXPENSE }
                    .sumOf { it.amountPaise }
                income to expense
            }

        return BarChartData(
            labels = years.map { it.toString() },
            incomeDataPaise = years.map { totals[it]?.first ?: 0L },
            expenseDataPaise = years.map { totals[it]?.second ?: 0L }
        )
    }

    private fun updateState(transform: (AnalyticsState) -> AnalyticsState) {
        _state.update(transform)
    }
}