//package com.bluemix.cashio.presentation.analytics
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.bluemix.cashio.core.common.Result
//import com.bluemix.cashio.domain.model.Category
//import com.bluemix.cashio.domain.model.DateRange
//import com.bluemix.cashio.domain.model.Expense
//import com.bluemix.cashio.domain.model.FinancialStats
//import com.bluemix.cashio.domain.model.TransactionType
//import com.bluemix.cashio.domain.usecase.expense.GetExpensesByDateRangeUseCase
//import com.bluemix.cashio.domain.usecase.expense.GetFinancialStatsUseCase
//import com.bluemix.cashio.presentation.common.UiState
//import kotlinx.coroutines.async
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import java.time.LocalDate
//import java.time.YearMonth
//import java.time.format.DateTimeFormatter
//
//data class AnalyticsState(
//    val stats: UiState<FinancialStats> = UiState.Idle,
//    val expenses: UiState<List<Expense>> = UiState.Idle,
//    val selectedDateRange: DateRange = DateRange.THIS_MONTH,
//
//    val selectedChartPeriod: ChartPeriod = ChartPeriod.MONTHLY,
//    val selectedChartType: ChartType = ChartType.BAR_CHART,
//
//    val barChartData: BarChartData = BarChartData(),
//    val categoryBreakdown: Map<Category, Double> = emptyMap(),
//    val topCategoryPercentage: Float = 0f,
//    val incomeChange: Double = 0.0,
//    val expenseChange: Double = 0.0
//)
//
//data class BarChartData(
//    val incomeData: List<Double> = emptyList(),
//    val expenseData: List<Double> = emptyList(),
//    val labels: List<String> = emptyList()
//)
//
//enum class ChartPeriod { WEEKLY, MONTHLY, YEARLY }
//enum class ChartType { PIE_CHART, BAR_CHART, LINE_CHART }
//
//class AnalyticsViewModel(
//    private val getFinancialStatsUseCase: GetFinancialStatsUseCase,
//    private val getExpensesByDateRangeUseCase: GetExpensesByDateRangeUseCase
//) : ViewModel() {
//
//    private val _state = MutableStateFlow(AnalyticsState())
//    val state: StateFlow<AnalyticsState> = _state.asStateFlow()
//
//    init {
//        loadAnalytics()
//    }
//
//    fun loadAnalytics() {
//        loadFinancialStats()
//        loadExpensesForCharts()
//    }
//
//    private fun loadFinancialStats() {
//        viewModelScope.launch {
//            _state.update { it.copy(stats = UiState.Loading) }
//
//            val currentRange = _state.value.selectedDateRange
//            val lastMonthRange = DateRange.LAST_MONTH // ensure exists
//
//            val currentDeferred = async { getFinancialStatsUseCase(currentRange) }
//            val lastDeferred = async { getFinancialStatsUseCase(lastMonthRange) }
//
//            val currentResult = currentDeferred.await()
//            val lastResult = lastDeferred.await()
//
//            when (currentResult) {
//                is Result.Success -> {
//                    val stats = currentResult.data
//                    val lastStats = (lastResult as? Result.Success)?.data
//
//                    val incomeChange = lastStats?.let { stats.totalIncome - it.totalIncome } ?: 0.0
//                    val expenseChange =
//                        lastStats?.let { stats.totalExpenses - it.totalExpenses } ?: 0.0
//
//                    val topPercentage =
//                        if (stats.totalExpenses > 0 && stats.topCategory != null) {
//                            (stats.topCategoryAmount / stats.totalExpenses).toFloat()
//                        } else 0f
//
//                    _state.update {
//                        it.copy(
//                            stats = UiState.Success(stats),
//                            categoryBreakdown = stats.categoryBreakdown,
//                            topCategoryPercentage = topPercentage,
//                            incomeChange = incomeChange,
//                            expenseChange = expenseChange
//                        )
//                    }
//                }
//
//                is Result.Error -> {
//                    _state.update {
//                        it.copy(
//                            stats = UiState.Error(
//                                currentResult.message ?: "Failed to load stats"
//                            )
//                        )
//                    }
//                }
//
//                else -> Unit
//            }
//        }
//    }
//
//    /**
//     * Load enough transactions to support the currently selected chart period.
//     * - WEEKLY: current month only
//     * - MONTHLY: last 6 months
//     * - YEARLY: last 4 years  ✅ (your previous code loaded only 1 year)
//     */
//    private fun loadExpensesForCharts() {
//        viewModelScope.launch {
//            _state.update { it.copy(expenses = UiState.Loading) }
//
//            val end = LocalDate.now()
//            val start = when (_state.value.selectedChartPeriod) {
//                ChartPeriod.WEEKLY -> YearMonth.now().atDay(1)
//                ChartPeriod.MONTHLY -> end.minusMonths(6).withDayOfMonth(1)
//                ChartPeriod.YEARLY -> end.minusYears(4).withDayOfYear(1)
//            }
//
//            val params = GetExpensesByDateRangeUseCase.Params(
//                startDate = start.atStartOfDay(),
//                endDate = end.atTime(23, 59, 59)
//            )
//
//            when (val result = getExpensesByDateRangeUseCase(params)) {
//                is Result.Success -> {
//                    val txns = result.data
//                    val barData = prepareBarChartData(txns, _state.value.selectedChartPeriod)
//
//                    _state.update {
//                        it.copy(
//                            expenses = UiState.Success(txns),
//                            barChartData = barData
//                        )
//                    }
//                }
//
//                is Result.Error -> {
//                    _state.update {
//                        it.copy(
//                            expenses = UiState.Error(
//                                result.message ?: "Failed to load transactions"
//                            )
//                        )
//                    }
//                }
//
//                else -> Unit
//            }
//        }
//    }
//
//    private fun prepareBarChartData(
//        expenses: List<Expense>,
//        chartPeriod: ChartPeriod
//    ): BarChartData = when (chartPeriod) {
//        ChartPeriod.WEEKLY -> prepareWeeklyChart(expenses)
//        ChartPeriod.MONTHLY -> prepareMonthlyChart(expenses)
//        ChartPeriod.YEARLY -> prepareYearlyChart(expenses)
//    }
//
//    private fun prepareWeeklyChart(expenses: List<Expense>): BarChartData {
//        val labels = mutableListOf<String>()
//        val expenseData = mutableListOf<Double>()
//        val incomeData = mutableListOf<Double>()
//
//        val currentMonth = YearMonth.now()
//        val firstDay = currentMonth.atDay(1)
//        val lastDay = currentMonth.atEndOfMonth()
//
//        var weekStart = firstDay
//        var weekNumber = 1
//
//        while (!weekStart.isAfter(lastDay)) {
//            val weekEnd = weekStart.plusDays(6).let { if (it.isAfter(lastDay)) lastDay else it }
//
//            labels.add("W$weekNumber")
//
//            val weekTransactions = expenses.filter { e ->
//                val d = e.date.toLocalDate()
//                !d.isBefore(weekStart) && !d.isAfter(weekEnd)
//            }
//
//            expenseData.add(weekTransactions.filter { it.transactionType == TransactionType.EXPENSE }
//                .sumOf { it.amount })
//            incomeData.add(weekTransactions.filter { it.transactionType == TransactionType.INCOME }
//                .sumOf { it.amount })
//
//            weekStart = weekEnd.plusDays(1)
//            weekNumber++
//        }
//
//        return BarChartData(incomeData = incomeData, expenseData = expenseData, labels = labels)
//    }
//
//    private fun prepareMonthlyChart(expenses: List<Expense>): BarChartData {
//        val labels = mutableListOf<String>()
//        val expenseData = mutableListOf<Double>()
//        val incomeData = mutableListOf<Double>()
//
//        val currentMonth = YearMonth.now()
//        val fmt = DateTimeFormatter.ofPattern("MMM")
//
//        for (i in 5 downTo 0) {
//            val target = currentMonth.minusMonths(i.toLong())
//            val start = target.atDay(1)
//            val end = target.atEndOfMonth()
//
//            labels.add(target.format(fmt))
//
//            val monthTx = expenses.filter { e ->
//                val d = e.date.toLocalDate()
//                !d.isBefore(start) && !d.isAfter(end)
//            }
//
//            expenseData.add(monthTx.filter { it.transactionType == TransactionType.EXPENSE }
//                .sumOf { it.amount })
//            incomeData.add(monthTx.filter { it.transactionType == TransactionType.INCOME }
//                .sumOf { it.amount })
//        }
//
//        return BarChartData(incomeData = incomeData, expenseData = expenseData, labels = labels)
//    }
//
//    private fun prepareYearlyChart(expenses: List<Expense>): BarChartData {
//        val labels = mutableListOf<String>()
//        val expenseData = mutableListOf<Double>()
//        val incomeData = mutableListOf<Double>()
//
//        val currentYear = LocalDate.now().year
//
//        for (i in 3 downTo 0) {
//            val year = currentYear - i
//            labels.add(year.toString())
//
//            val yearTx = expenses.filter { it.date.year == year }
//
//            expenseData.add(yearTx.filter { it.transactionType == TransactionType.EXPENSE }
//                .sumOf { it.amount })
//            incomeData.add(yearTx.filter { it.transactionType == TransactionType.INCOME }
//                .sumOf { it.amount })
//        }
//
//        return BarChartData(incomeData = incomeData, expenseData = expenseData, labels = labels)
//    }
//
//    /**
//     * Changes bar chart period:
//     * - updates selection
//     * - reloads data with correct historical window
//     */
//    fun changeChartPeriod(period: ChartPeriod) {
//        if (period == _state.value.selectedChartPeriod) return
//        _state.update { it.copy(selectedChartPeriod = period) }
//        loadExpensesForCharts()
//    }
//
//    fun changeDateRange(dateRange: DateRange) {
//        if (dateRange == _state.value.selectedDateRange) return
//        _state.update { it.copy(selectedDateRange = dateRange) }
//        loadFinancialStats()
//    }
//
//    fun changeChartType(chartType: ChartType) {
//        _state.update { it.copy(selectedChartType = chartType) }
//    }
//}
package com.bluemix.cashio.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.DateRange
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.FinancialStats
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.domain.usecase.expense.GetExpensesByDateRangeUseCase
import com.bluemix.cashio.domain.usecase.expense.GetFinancialStatsUseCase
import com.bluemix.cashio.presentation.common.UiState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class AnalyticsState(
    val stats: UiState<FinancialStats> = UiState.Idle,
    val expenses: UiState<List<Expense>> = UiState.Idle,

    val selectedDateRange: DateRange = DateRange.THIS_MONTH,
    val selectedChartPeriod: ChartPeriod = ChartPeriod.MONTHLY,
    val selectedChartType: ChartType = ChartType.BAR_CHART,

    val barChartData: BarChartData = BarChartData(),

    val categoryBreakdown: Map<Category, Double> = emptyMap(),
    val topCategoryPercentage: Float = 0f,
    val incomeChange: Double = 0.0,
    val expenseChange: Double = 0.0
)

data class BarChartData(
    val incomeData: List<Double> = emptyList(),
    val expenseData: List<Double> = emptyList(),
    val labels: List<String> = emptyList()
)

enum class ChartPeriod { WEEKLY, MONTHLY, YEARLY }
enum class ChartType { PIE_CHART, BAR_CHART, LINE_CHART }

class AnalyticsViewModel(
    private val getFinancialStats: GetFinancialStatsUseCase,
    private val getExpensesByDateRange: GetExpensesByDateRangeUseCase
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

    private fun loadFinancialStats() {
        viewModelScope.launch {
            _state.update { it.copy(stats = UiState.Loading) }

            val currentRange = _state.value.selectedDateRange
            val previousRange = DateRange.LAST_MONTH

            val currentDeferred = async { getFinancialStats(currentRange) }
            val previousDeferred = async { getFinancialStats(previousRange) }

            val currentResult = currentDeferred.await()
            val previousResult = previousDeferred.await()

            when (currentResult) {
                is Result.Success -> {
                    val current = currentResult.data
                    val previous = (previousResult as? Result.Success)?.data

                    val incomeDelta = previous?.let { current.totalIncome - it.totalIncome } ?: 0.0
                    val expenseDelta =
                        previous?.let { current.totalExpenses - it.totalExpenses } ?: 0.0

                    val topCategoryPercent =
                        if (current.totalExpenses > 0 && current.topCategory != null) {
                            (current.topCategoryAmount / current.totalExpenses).toFloat()
                        } else 0f

                    _state.update {
                        it.copy(
                            stats = UiState.Success(current),
                            categoryBreakdown = current.categoryBreakdown,
                            topCategoryPercentage = topCategoryPercent,
                            incomeChange = incomeDelta,
                            expenseChange = expenseDelta
                        )
                    }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            stats = UiState.Error(currentResult.message ?: "Failed to load stats")
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
            _state.update { it.copy(expenses = UiState.Loading) }

            val endDate = LocalDate.now()
            val period = _state.value.selectedChartPeriod

            val startDate = when (period) {
                ChartPeriod.WEEKLY -> YearMonth.now().atDay(1)
                ChartPeriod.MONTHLY -> endDate.minusMonths(6).withDayOfMonth(1)
                ChartPeriod.YEARLY -> endDate.minusYears(4).withDayOfYear(1)
            }

            val params = GetExpensesByDateRangeUseCase.Params(
                startDate = startDate.atStartOfDay(),
                endDate = endDate.atTime(23, 59, 59)
            )

            when (val result = getExpensesByDateRange(params)) {
                is Result.Success -> {
                    val transactions = result.data
                    val chartData = buildChartData(transactions, period)

                    _state.update {
                        it.copy(
                            expenses = UiState.Success(transactions),
                            barChartData = chartData
                        )
                    }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            expenses = UiState.Error(
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
    }
}
