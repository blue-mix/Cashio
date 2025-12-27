package com.bluemix.cashio.presentation.analytics.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.DateRange
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.FinancialStats
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.domain.usecase.expense.GetExpensesByDateRangeUseCase
import com.bluemix.cashio.domain.usecase.expense.GetFinancialStatsUseCase
import com.bluemix.cashio.presentation.common.UiState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class AnalyticsViewModel(
    private val getFinancialStatsUseCase: GetFinancialStatsUseCase,
    private val getExpensesByDateRangeUseCase: GetExpensesByDateRangeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        loadFinancialStats()
        loadChartTransactions()
    }

    fun changeChartPeriod(period: ChartPeriod) {
        if (period == _state.value.selectedChartPeriod) return
        _state.update { it.copy(selectedChartPeriod = period) }
        loadChartTransactions()
    }

    fun changeDateRange(dateRange: DateRange) {
        if (dateRange == _state.value.selectedDateRange) return
        _state.update { it.copy(selectedDateRange = dateRange) }
        loadFinancialStats()
    }

    fun changeChartType(type: ChartType) {
        if (type == _state.value.selectedChartType) return
        _state.update { it.copy(selectedChartType = type) }
        // no-op for now (future: swap chart renderer)
    }

    private fun loadFinancialStats() {
        viewModelScope.launch {
            _state.update { it.copy(statsState = UiState.Loading) }

            val currentRange = _state.value.selectedDateRange
            val previousRange = DateRange.LAST_MONTH // keep your current behavior

            val currentResult: Result<FinancialStats>
            val previousResult: Result<FinancialStats>

            try {
                coroutineScope {
                    val currentDeferred = async { getFinancialStatsUseCase(currentRange) }
                    val previousDeferred = async { getFinancialStatsUseCase(previousRange) }
                    currentResult = currentDeferred.await()
                    previousResult = previousDeferred.await()
                }
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        statsState = UiState.Error(
                            t.message ?: "Failed to load stats"
                        )
                    )
                }
                return@launch
            }

            when (currentResult) {
                is Result.Success -> {
                    val current = currentResult.data
                    val previous = (previousResult as? Result.Success)?.data

                    val incomeDelta = previous?.let { current.totalIncome - it.totalIncome } ?: 0.0
                    val expenseDelta =
                        previous?.let { current.totalExpenses - it.totalExpenses } ?: 0.0

                    val topCategoryRatio =
                        if (current.totalExpenses > 0 && current.topCategory != null) {
                            (current.topCategoryAmount / current.totalExpenses).toFloat()
                        } else 0f

                    _state.update {
                        it.copy(
                            statsState = UiState.Success(current),
                            categoryBreakdown = current.categoryBreakdown,
                            topCategoryRatio = topCategoryRatio,
                            incomeDelta = incomeDelta,
                            expenseDelta = expenseDelta
                        )
                    }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            statsState = UiState.Error(
                                currentResult.message ?: "Failed to load stats"
                            )
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    /**
     * WEEKLY  -> this month
     * MONTHLY -> last 6 months
     * YEARLY  -> last 4 years
     */
    private fun loadChartTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(chartExpensesState = UiState.Loading) }

            val now = LocalDate.now()
            val period = _state.value.selectedChartPeriod

            val startDate = when (period) {
                ChartPeriod.WEEKLY -> YearMonth.from(now).atDay(1)
                ChartPeriod.MONTHLY -> now.minusMonths(6).withDayOfMonth(1)
                ChartPeriod.YEARLY -> now.minusYears(4).withDayOfYear(1)
            }

            val params = GetExpensesByDateRangeUseCase.Params(
                startDate = startDate.atStartOfDay(),
                endDate = now.atTime(23, 59, 59)
            )

            when (val result = getExpensesByDateRangeUseCase(params)) {
                is Result.Success -> {
                    val transactions = result.data
                    val chartData = buildChartData(transactions, period)

                    _state.update {
                        it.copy(
                            chartExpensesState = UiState.Success(transactions),
                            barChartData = chartData
                        )
                    }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            chartExpensesState = UiState.Error(
                                result.message ?: "Failed to load transactions"
                            )
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    private fun buildChartData(
        expenses: List<Expense>,
        period: ChartPeriod
    ): BarChartData = when (period) {
        ChartPeriod.WEEKLY -> buildWeeklyBuckets(expenses)
        ChartPeriod.MONTHLY -> buildMonthlyBuckets(expenses)
        ChartPeriod.YEARLY -> buildYearlyBuckets(expenses)
    }

    private fun buildWeeklyBuckets(expenses: List<Expense>): BarChartData {
        val currentMonth = YearMonth.now()
        val firstDay = currentMonth.atDay(1)
        val lastDay = currentMonth.atEndOfMonth()

        // weekIndex -> (income, expense)
        val totals = HashMap<Int, Pair<Double, Double>>(8)

        for (e in expenses) {
            val date = e.date.toLocalDate()
            if (date.isBefore(firstDay) || date.isAfter(lastDay)) continue

            val weekIndex = (date.dayOfMonth - 1) / 7
            val (income, expense) = totals[weekIndex] ?: (0.0 to 0.0)

            totals[weekIndex] = when (e.transactionType) {
                TransactionType.INCOME -> (income + e.amount) to expense
                TransactionType.EXPENSE -> income to (expense + e.amount)
            }
        }

        val labels = mutableListOf<String>()
        val incomeData = mutableListOf<Double>()
        val expenseData = mutableListOf<Double>()

        var cursor = firstDay
        var index = 0
        while (!cursor.isAfter(lastDay)) {
            labels += "W${index + 1}"
            val pair = totals[index]
            incomeData += pair?.first ?: 0.0
            expenseData += pair?.second ?: 0.0

            cursor = cursor.plusDays(7)
            index++
        }

        return BarChartData(labels = labels, incomeData = incomeData, expenseData = expenseData)
    }

    private fun buildMonthlyBuckets(expenses: List<Expense>): BarChartData {
        val months = (5 downTo 0).map { YearMonth.now().minusMonths(it.toLong()) }
        val monthFmt = DateTimeFormatter.ofPattern("MMM")

        val totals = HashMap<YearMonth, Pair<Double, Double>>(months.size)

        for (e in expenses) {
            val ym = YearMonth.from(e.date.toLocalDate())
            if (ym !in months) continue

            val (income, expense) = totals[ym] ?: (0.0 to 0.0)
            totals[ym] = when (e.transactionType) {
                TransactionType.INCOME -> (income + e.amount) to expense
                TransactionType.EXPENSE -> income to (expense + e.amount)
            }
        }

        return BarChartData(
            labels = months.map { it.format(monthFmt) },
            incomeData = months.map { totals[it]?.first ?: 0.0 },
            expenseData = months.map { totals[it]?.second ?: 0.0 }
        )
    }

    private fun buildYearlyBuckets(expenses: List<Expense>): BarChartData {
        val currentYear = LocalDate.now().year
        val years = (3 downTo 0).map { currentYear - it }

        val totals = HashMap<Int, Pair<Double, Double>>(years.size)

        for (e in expenses) {
            val year = e.date.year
            if (year !in years) continue

            val (income, expense) = totals[year] ?: (0.0 to 0.0)
            totals[year] = when (e.transactionType) {
                TransactionType.INCOME -> (income + e.amount) to expense
                TransactionType.EXPENSE -> income to (expense + e.amount)
            }
        }

        return BarChartData(
            labels = years.map { it.toString() },
            incomeData = years.map { totals[it]?.first ?: 0.0 },
            expenseData = years.map { totals[it]?.second ?: 0.0 }
        )
    }
}