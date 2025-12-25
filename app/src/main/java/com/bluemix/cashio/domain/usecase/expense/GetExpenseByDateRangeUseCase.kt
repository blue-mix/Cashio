package com.bluemix.cashio.domain.usecase.expense

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.usecase.base.UseCase
import java.time.LocalDateTime

/**
 * Get expenses by date range
 */
class GetExpensesByDateRangeUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<GetExpensesByDateRangeUseCase.Params, List<Expense>>() {

    data class Params(
        val startDate: LocalDateTime,
        val endDate: LocalDateTime
    )

    override suspend fun execute(params: Params): Result<List<Expense>> {
        return expenseRepository.getExpensesByDateRange(
            params.startDate,
            params.endDate
        )
    }
}
