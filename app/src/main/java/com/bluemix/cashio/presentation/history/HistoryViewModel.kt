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

/**
 * State holder for the History screen.
 *
 * @property transactionsByDate Source of truth map grouping all loaded transactions by date.
 * @property sortedDayGroups A pre-sorted list of all transaction groups (newest first).
 * @property visibleDayGroups The derived list actually shown in the UI. If a date is selected, contains only that day.
 * @property expenseTotalByDate Map of Date -> Total Spending (used for dots/heatmap).
 * @property expenseHeatLevelByDate Map of Date -> Intensity Level (0-4) for calendar coloring.
 * @property selectedDate The currently active filter from the calendar.
 */
data class HistoryState(
    // Data Source
    val transactionsByDate: Map<LocalDate, List<Expense>> = emptyMap(),
    val sortedDayGroups: List<Pair<LocalDate, List<Expense>>> = emptyList(),

    // UI Render Target (Filtered)
    val visibleDayGroups: List<Pair<LocalDate, List<Expense>>> = emptyList(),

    // Visualization Data
    val expenseTotalByDate: Map<LocalDate, Double> = emptyMap(),
    val expenseHeatLevelByDate: Map<LocalDate, Int> = emptyMap(),

    // UI Status
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel responsible for the Transaction History and Calendar Heatmap.
 *
 * Key Responsibilities:
 * 1. Observes the full transaction stream.
 * 2. Processes raw data into daily groups for the list.
 * 3. Calculates "Spending Heat" dynamically based on the user's spending percentiles.
 * 4. Manages filtering when a user taps a specific day on the calendar.
 */
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
                    updateState {
                        it.copy(
                            isLoading = false,
                            errorMessage = t.message ?: "Failed to load transactions"
                        )
                    }
                }
                .collectLatest { transactions ->
                    // Heavy calculation offloaded to background via coroutine context
                    val processed = processData(transactions)

                    updateState { old ->
                        val newState = old.copy(
                            transactionsByDate = processed.byDate,
                            sortedDayGroups = processed.sortedGroups,
                            expenseTotalByDate = processed.totals,
                            expenseHeatLevelByDate = processed.heatLevels,
                            isLoading = false,
                            errorMessage = null
                        )
                        // Re-apply current selection filter to the new data
                        newState.copy(visibleDayGroups = resolveVisibleGroups(newState))
                    }
                }
        }
    }

    fun onDateClicked(date: LocalDate) {
        updateState { old ->
            // Toggle selection: click same date to deselect
            val newSelection = if (old.selectedDate == date) null else date
            val newState = old.copy(selectedDate = newSelection)
            newState.copy(visibleDayGroups = resolveVisibleGroups(newState))
        }
    }

    fun showAllTransactions() {
        updateState { old ->
            val newState = old.copy(selectedDate = null)
            newState.copy(visibleDayGroups = resolveVisibleGroups(newState))
        }
    }

    /* -------------------------------------------------------------------------- */
    /* Data Processing Logic                                                      */
    /* -------------------------------------------------------------------------- */

    private data class ProcessedData(
        val byDate: Map<LocalDate, List<Expense>>,
        val sortedGroups: List<Pair<LocalDate, List<Expense>>>,
        val totals: Map<LocalDate, Double>,
        val heatLevels: Map<LocalDate, Int>
    )

    /**
     * Transforms raw flat transactions into grouped, sorted, and analyzed data structures.
     */
    private fun processData(transactions: List<Expense>): ProcessedData {
        // 1. Group by Date & Sort Descending
        val byDate = transactions
            .groupBy { it.date.toLocalDate() }
            .mapValues { (_, list) -> list.sortedByDescending { it.date } }

        val sortedGroups = byDate.toList().sortedByDescending { (date, _) -> date }

        // 2. Calculate Daily Totals (Expenses only for heatmap)
        val totals = byDate.mapValues { (_, items) ->
            items.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amount }
        }

        // 3. Compute Dynamic Heatmap Thresholds
        // We calculate percentiles (median, 75th, 90th) of *this specific user's* spending habits.
        // This ensures the colors are meaningful regardless of whether they spend $10 or $1000 daily.
        val positiveSpends = totals.values.filter { it > 0.0 }.sorted()
        val thresholds = computeThresholds(positiveSpends)

        // 4. Map Totals to Heat Levels (0-4)
        val heatLevels = totals.mapValues { (_, amount) ->
            getHeatLevel(amount, thresholds)
        }

        return ProcessedData(byDate, sortedGroups, totals, heatLevels)
    }

    /**
     * Filters the visible list based on the currently selected date.
     * If no date is selected, returns the full history.
     */
    private fun resolveVisibleGroups(state: HistoryState): List<Pair<LocalDate, List<Expense>>> {
        return state.selectedDate?.let { date ->
            // If date selected, return list containing ONLY that day (if it has data)
            state.transactionsByDate[date]?.let { listOf(date to it) }
        } ?: state.sortedDayGroups // Otherwise return everything
    }

    /* -------------------------------------------------------------------------- */
    /* Heatmap Math Helpers                                                       */
    /* -------------------------------------------------------------------------- */

    private data class Thresholds(val p50: Double, val p75: Double, val p90: Double)

    private fun computeThresholds(sortedValues: List<Double>): Thresholds {
        if (sortedValues.size < 4) {
            val max = sortedValues.maxOrNull() ?: 0.0
            return Thresholds(max, max, max)
        }

        fun getPercentile(p: Double): Double {
            val index = ((sortedValues.size - 1) * p).roundToInt()
            return sortedValues[index.coerceIn(sortedValues.indices)]
        }

        return Thresholds(
            p50 = getPercentile(0.50), // Median spend
            p75 = getPercentile(0.75), // High spend
            p90 = getPercentile(0.90)  // Very high spend
        )
    }

    private fun getHeatLevel(amount: Double, t: Thresholds): Int {
        if (amount <= 0.0) return 0
        return when {
            amount <= t.p50 -> 1 // Low intensity
            amount <= t.p75 -> 2 // Medium intensity
            amount <= t.p90 -> 3 // High intensity
            else -> 4            // Max intensity
        }
    }

    private fun updateState(transform: (HistoryState) -> HistoryState) {
        _state.update(transform)
    }
}