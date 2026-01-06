//package com.bluemix.cashio.domain.model
//
//import java.time.LocalDate
//import java.time.LocalDateTime
//import java.time.temporal.TemporalAdjusters
//
///**
// * Predefined date ranges for filtering
// */
//enum class DateRange {
//    TODAY,
//    THIS_WEEK,
//    THIS_MONTH,
//    LAST_MONTH,
//    THIS_YEAR,
//    CUSTOM;
//
//    /**
//     * Get start and end dates for this range
//     */
//    fun getDateBounds(): Pair<LocalDateTime, LocalDateTime> {
//        LocalDateTime.now()
//        val today = LocalDate.now()
//
//        return when (this) {
//            TODAY -> {
//                val start = today.atStartOfDay()
//                val end = start.plusDays(1).minusSeconds(1)
//                start to end
//            }
//
//            THIS_WEEK -> {
//                val start = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
//                    .atStartOfDay()
//                val end = start.plusWeeks(1).minusSeconds(1)
//                start to end
//            }
//
//            THIS_MONTH -> {
//                val start = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay()
//                val end = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59)
//                start to end
//            }
//
//            LAST_MONTH -> {
//                val lastMonth = today.minusMonths(1)
//                val start = lastMonth.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay()
//                val end = lastMonth.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59)
//                start to end
//            }
//
//            THIS_YEAR -> {
//                val start = today.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay()
//                val end = today.with(TemporalAdjusters.lastDayOfYear()).atTime(23, 59, 59)
//                start to end
//            }
//
//            CUSTOM -> {
//                // Return current day as default, actual dates provided separately
//                val start = today.atStartOfDay()
//                val end = start.plusDays(1).minusSeconds(1)
//                start to end
//            }
//        }
//    }
//}
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