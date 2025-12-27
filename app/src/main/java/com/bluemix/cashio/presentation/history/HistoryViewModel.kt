package com.bluemix.cashio.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

data class HeatmapThresholds(
    val p50: Double = 0.0,
    val p75: Double = 0.0,
    val p90: Double = 0.0
)

data class HistoryState(
    // Source of truth: grouped transactions (each day list sorted desc by time)
    val transactionsByDate: Map<LocalDate, List<Expense>> = emptyMap(),

    // Pre-sorted day groups (sorted once on data updates)
    val sortedDayGroups: List<Pair<LocalDate, List<Expense>>> = emptyList(),

    // What UI renders (either sortedDayGroups or selected day)
    val visibleDayGroups: List<Pair<LocalDate, List<Expense>>> = emptyList(),

    // Expense-only totals for calendar heatmap
    val expenseTotalByDate: Map<LocalDate, Double> = emptyMap(),

    // ✅ Dynamic heatmap outputs (0..4)
    val expenseHeatLevelByDate: Map<LocalDate, Int> = emptyMap(),
    val heatmapThresholds: HeatmapThresholds = HeatmapThresholds(),

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
        observeTransactions()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            observeExpensesUseCase()
                .distinctUntilChanged()
                .catch { t ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = t.message ?: "Failed to load transactions"
                        )
                    }
                }
                .collectLatest { transactions ->
                    // Group by day + sort each day’s list desc by date-time
                    val transactionsByDate = transactions
                        .groupBy { it.date.toLocalDate() }
                        .mapValues { (_, list) -> list.sortedByDescending { it.date } }

                    // Sorted day groups (desc)
                    val sortedDayGroups = transactionsByDate
                        .toList()
                        .sortedByDescending { (date, _) -> date }

                    // Expense-only totals for heatmap (per day)
                    val expenseTotalByDate = transactionsByDate.mapValues { (_, dayItems) ->
                        dayItems.asSequence()
                            .filter { it.transactionType == TransactionType.EXPENSE }
                            .sumOf { it.amount }
                    }

                    // ✅ Dynamic thresholds based on the user's own spend distribution
                    val spendValuesSorted = expenseTotalByDate.values
                        .asSequence()
                        .filter { it > 0.0 }
                        .sorted()
                        .toList()

                    val thresholds = computeHeatmapThresholds(spendValuesSorted)

                    // ✅ Map date -> heat level (0..4)
                    val heatLevels = expenseTotalByDate.mapValues { (_, v) ->
                        spendingToHeatLevel(v, thresholds)
                    }

                    _state.update { old ->
                        val updated = old.copy(
                            transactionsByDate = transactionsByDate,
                            sortedDayGroups = sortedDayGroups,
                            expenseTotalByDate = expenseTotalByDate,
                            expenseHeatLevelByDate = heatLevels,
                            heatmapThresholds = thresholds,
                            isLoading = false,
                            errorMessage = null
                        )
                        updated.copy(visibleDayGroups = buildVisibleDayGroups(updated))
                    }
                }
        }
    }

    fun onDateClicked(date: LocalDate) {
        _state.update { old ->
            val newSelectedDate = if (old.selectedDate == date) null else date
            val updated = old.copy(selectedDate = newSelectedDate)
            updated.copy(visibleDayGroups = buildVisibleDayGroups(updated))
        }
    }

    fun showAllTransactions() {
        _state.update { old ->
            val updated = old.copy(selectedDate = null)
            updated.copy(visibleDayGroups = buildVisibleDayGroups(updated))
        }
    }

    private fun buildVisibleDayGroups(state: HistoryState): List<Pair<LocalDate, List<Expense>>> {
        val selected = state.selectedDate
        return if (selected == null) {
            state.sortedDayGroups
        } else {
            state.transactionsByDate[selected]?.let { listOf(selected to it) } ?: emptyList()
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Heatmap helpers                                                             */
/* -------------------------------------------------------------------------- */

private fun computeHeatmapThresholds(sortedPositiveValues: List<Double>): HeatmapThresholds {
    // If there isn’t enough data, fall back gracefully
    if (sortedPositiveValues.size < 4) {
        val max = sortedPositiveValues.maxOrNull() ?: 0.0
        return HeatmapThresholds(p50 = max, p75 = max, p90 = max)
    }

    fun percentile(p: Double): Double {
        // p is 0..1
        val n = sortedPositiveValues.size
        val idx = ((n - 1) * p).roundToInt().coerceIn(0, n - 1)
        return sortedPositiveValues[idx]
    }

    return HeatmapThresholds(
        p50 = percentile(0.50),
        p75 = percentile(0.75),
        p90 = percentile(0.90)
    )
}

private fun spendingToHeatLevel(value: Double, t: HeatmapThresholds): Int {
    if (value <= 0.0) return 0
    return when {
        value <= t.p50 -> 1
        value <= t.p75 -> 2
        value <= t.p90 -> 3
        else -> 4
    }
}
