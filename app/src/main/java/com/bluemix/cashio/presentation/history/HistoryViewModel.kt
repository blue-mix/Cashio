package com.bluemix.cashio.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesUseCase
import com.bluemix.cashio.domain.usecase.preferences.ObserveSelectedCurrencyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * ViewModel responsible for the Transaction History and Calendar Heatmap.
 *
 * Key Responsibilities:
 * 1. Observes the full transaction stream.
 * 2. Processes raw data into daily groups for the list.
 * 3. Calculates "Spending Heat" dynamically based on the user's spending percentiles.
 * 4. Manages filtering when a user taps a specific day on the calendar.
 *
 * **All monetary values use paise (Long) internally.**
 */
class HistoryViewModel(
    private val observeExpensesUseCase: ObserveExpensesUseCase,
    private val observeSelectedCurrencyUseCase: ObserveSelectedCurrencyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState(isLoading = true))
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                observeExpensesUseCase(),
                observeSelectedCurrencyUseCase()
            ) { transactions, currency ->
                transactions to currency
            }
                .distinctUntilChanged()
                .catch { t ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            errorMessage = t.message ?: "Failed to load transactions"
                        )
                    }
                }
                .collectLatest { (transactions, currency) ->
                    // Heavy calculation offloaded to background via coroutine context
                    val processed = processData(transactions)

                    updateState { old ->
                        val newState = old.copy(
                            transactionsByDate = processed.byDate,
                            sortedDayGroups = processed.sortedGroups,
                            expenseTotalByDatePaise = processed.totalsPaise,
                            expenseHeatLevelByDate = processed.heatLevels,
                            selectedCurrency = currency,
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
        val totalsPaise: Map<LocalDate, Long>,
        val heatLevels: Map<LocalDate, Int>
    )

    /**
     * Transforms raw flat transactions into grouped, sorted, and analyzed data structures.
     * All monetary values in **paise (Long)**.
     */
    private fun processData(transactions: List<Expense>): ProcessedData {
        // 1. Group by Date & Sort Descending
        val byDate = transactions
            .groupBy { it.date.toLocalDate() }
            .mapValues { (_, list) -> list.sortedByDescending { it.date } }

        val sortedGroups = byDate.toList().sortedByDescending { (date, _) -> date }

        // 2. Calculate Daily Totals (Expenses only for heatmap) in paise
        val totalsPaise = byDate.mapValues { (_, items) ->
            items
                .filter { it.transactionType == TransactionType.EXPENSE }
                .sumOf { it.amountPaise }
        }

        // 3. Compute Dynamic Heatmap Thresholds
        // We calculate percentiles (median, 75th, 90th) of *this specific user's* spending habits.
        // This ensures the colors are meaningful regardless of whether they spend $10 or $1000 daily.
        val positiveSpends = totalsPaise.values.filter { it > 0L }.sorted()
        val thresholds = computeThresholds(positiveSpends)

        // 4. Map Totals to Heat Levels (0-4)
        val heatLevels = totalsPaise.mapValues { (_, amountPaise) ->
            getHeatLevel(amountPaise, thresholds)
        }

        return ProcessedData(byDate, sortedGroups, totalsPaise, heatLevels)
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

    private data class Thresholds(val p50: Long, val p75: Long, val p90: Long)

    private fun computeThresholds(sortedValues: List<Long>): Thresholds {
        if (sortedValues.size < 4) {
            val max = sortedValues.maxOrNull() ?: 0L
            return Thresholds(max, max, max)
        }

        fun getPercentile(p: Double): Long {
            val index = ((sortedValues.size - 1) * p).roundToInt()
            return sortedValues[index.coerceIn(sortedValues.indices)]
        }

        return Thresholds(
            p50 = getPercentile(0.50), // Median spend
            p75 = getPercentile(0.75), // High spend
            p90 = getPercentile(0.90)  // Very high spend
        )
    }

    private fun getHeatLevel(amountPaise: Long, t: Thresholds): Int {
        if (amountPaise <= 0L) return 0
        return when {
            amountPaise <= t.p50 -> 1 // Low intensity
            amountPaise <= t.p75 -> 2 // Medium intensity
            amountPaise <= t.p90 -> 3 // High intensity
            else -> 4                 // Max intensity
        }
    }

    private fun updateState(transform: (HistoryState) -> HistoryState) {
        _state.update(transform)
    }
}