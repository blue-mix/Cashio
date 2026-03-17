package com.bluemix.cashio.domain.usecase.expense

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.DateRange
import com.bluemix.cashio.domain.model.FinancialStats
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.usecase.base.UseCase
import java.time.LocalDateTime

/**
 * Returns aggregated [FinancialStats] for either a named [DateRange] or an
 * explicit custom window.
 *
 * Exactly one of [dateRange] or ([startDate] + [endDate]) must be provided:
 * - Pass [DateRange.CUSTOM] with explicit dates for user-defined windows.
 * - Pass any other [DateRange] value and leave dates null for named periods.
 */
class GetFinancialStatsUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<GetFinancialStatsUseCase.Params, FinancialStats>() {

    data class Params(
        val dateRange: DateRange,
        /** Required when [dateRange] is [DateRange.CUSTOM]. */
        val startDate: LocalDateTime? = null,
        /** Required when [dateRange] is [DateRange.CUSTOM]. */
        val endDate: LocalDateTime? = null
    )

    override suspend fun execute(params: Params): Result<FinancialStats> {
        return if (params.dateRange == DateRange.CUSTOM) {
            val start = params.startDate ?: return Result.Error(
                IllegalArgumentException("startDate required for CUSTOM range"),
                "Please select a start date"
            )
            val end = params.endDate ?: return Result.Error(
                IllegalArgumentException("endDate required for CUSTOM range"),
                "Please select an end date"
            )
            if (end.isBefore(start)) {
                return Result.Error(
                    IllegalArgumentException("endDate must be after startDate"),
                    "End date must be after start date"
                )
            }
            expenseRepository.getFinancialStatsByDates(start, end)
        } else {
            expenseRepository.getFinancialStats(params.dateRange)
        }
    }
}