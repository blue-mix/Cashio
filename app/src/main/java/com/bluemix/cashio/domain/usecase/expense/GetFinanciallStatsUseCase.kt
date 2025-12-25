package com.bluemix.cashio.domain.usecase.expense

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.DateRange
import com.bluemix.cashio.domain.model.FinancialStats
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.usecase.base.UseCase

/**
 * Get financial statistics for a date range
 */
class GetFinancialStatsUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<DateRange, FinancialStats>() {

    override suspend fun execute(params: DateRange): Result<FinancialStats> {
        return expenseRepository.getFinancialStats(params)
    }
}
