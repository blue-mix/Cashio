package com.bluemix.cashio.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

/**
 * Predefined date ranges for filtering
 */
enum class DateRange {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
    CUSTOM;

    /**
     * Get start and end dates for this range
     */
    fun getDateBounds(): Pair<LocalDateTime, LocalDateTime> {
        val today = LocalDate.now()

        return when (this) {
            TODAY -> {
                today.atStartOfDay() to today.atTime(LocalTime.MAX)
            }

            THIS_WEEK -> {
                val start = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                start.atStartOfDay() to start.plusWeeks(1).minusDays(1).atTime(LocalTime.MAX)
            }

            THIS_MONTH -> {
                val start = today.with(TemporalAdjusters.firstDayOfMonth())
                val end = today.with(TemporalAdjusters.lastDayOfMonth())
                start.atStartOfDay() to end.atTime(LocalTime.MAX)
            }

            LAST_MONTH -> {
                val lastMonth = today.minusMonths(1)
                val start = lastMonth.with(TemporalAdjusters.firstDayOfMonth())
                val end = lastMonth.with(TemporalAdjusters.lastDayOfMonth())
                start.atStartOfDay() to end.atTime(LocalTime.MAX)
            }

            THIS_YEAR -> {
                val start = today.with(TemporalAdjusters.firstDayOfYear())
                val end = today.with(TemporalAdjusters.lastDayOfYear())
                start.atStartOfDay() to end.atTime(LocalTime.MAX)
            }

            CUSTOM -> {
                // Default fallback
                today.atStartOfDay() to today.atTime(LocalTime.MAX)
            }
        }
    }
}