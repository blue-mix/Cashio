package com.bluemix.cashio.presentation.history

import com.bluemix.cashio.domain.model.Currency
import com.bluemix.cashio.domain.model.Expense
import java.time.LocalDate

/**
 * State holder for the History screen.
 *
 * All monetary values are in **paise (Long)**.
 *
 * @property transactionsByDate Source of truth map grouping all loaded transactions by date.
 * @property sortedDayGroups A pre-sorted list of all transaction groups (newest first).
 * @property visibleDayGroups The derived list actually shown in the UI. If a date is selected, contains only that day.
 * @property expenseTotalByDatePaise Map of Date -> Total Spending in paise (used for dots/heatmap).
 * @property expenseHeatLevelByDate Map of Date -> Intensity Level (0-4) for calendar coloring.
 * @property selectedDate The currently active filter from the calendar.
 * @property selectedCurrency The user's selected currency for formatting.
 */
data class HistoryState(
    // Data Source
    val transactionsByDate: Map<LocalDate, List<Expense>> = emptyMap(),
    val sortedDayGroups: List<Pair<LocalDate, List<Expense>>> = emptyList(),

    // UI Render Target (Filtered)
    val visibleDayGroups: List<Pair<LocalDate, List<Expense>>> = emptyList(),

    // Visualization Data (in paise)
    val expenseTotalByDatePaise: Map<LocalDate, Long> = emptyMap(),
    val expenseHeatLevelByDate: Map<LocalDate, Int> = emptyMap(),

    // UI Status
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Currency
    val selectedCurrency: Currency = Currency.INR
)