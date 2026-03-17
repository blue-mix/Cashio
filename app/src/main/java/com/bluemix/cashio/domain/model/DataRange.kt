package com.bluemix.cashio.domain.model

import com.bluemix.cashio.domain.model.DateRange.CUSTOM
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

/**
 * Predefined date ranges used for filtering and statistics.
 *
 * Use [getDateBounds] to obtain concrete [LocalDateTime] boundaries.
 * [CUSTOM] does **not** provide bounds on its own — callers must supply
 * explicit start/end dates when using custom ranges.
 */
enum class DateRange {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
    CUSTOM;

    /**
     * Returns an inclusive [start, end] pair of [LocalDateTime] for this range.
     *
     * [weekStartDay] controls which day a week begins on — defaults to [DayOfWeek.MONDAY]
     * but should be sourced from the user's locale or preference in calling code.
     *
     * @throws UnsupportedOperationException if called on [CUSTOM]. Custom ranges must
     * supply explicit dates to the repository/use-case directly.
     */
    fun getDateBounds(weekStartDay: DayOfWeek = DayOfWeek.MONDAY): Pair<LocalDateTime, LocalDateTime> {
        val today = LocalDate.now()

        return when (this) {
            TODAY -> today.atStartOfDay() to today.atTime(LocalTime.MAX)

            THIS_WEEK -> {
                val start = today.with(TemporalAdjusters.previousOrSame(weekStartDay))
                val end = start.plusWeeks(1).minusDays(1)
                start.atStartOfDay() to end.atTime(LocalTime.MAX)
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

            CUSTOM -> throw UnsupportedOperationException(
                "DateRange.CUSTOM does not have fixed bounds. " +
                        "Pass explicit startDate/endDate to the repository or use-case instead."
            )
        }
    }

    /**
     * Returns the [DateRange] that immediately precedes this one — used for
     * period-over-period delta calculations in analytics.
     *
     * [CUSTOM] and [THIS_YEAR] return null because a generic "previous" period
     * is not well-defined.
     */
    fun previousRange(): DateRange? = when (this) {
        TODAY -> null          // "yesterday" needs explicit date; not a named range
        THIS_WEEK -> null
        THIS_MONTH -> LAST_MONTH
        LAST_MONTH -> null
        THIS_YEAR -> null
        CUSTOM -> null
    }
}