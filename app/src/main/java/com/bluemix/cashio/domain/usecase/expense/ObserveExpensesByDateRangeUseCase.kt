package com.bluemix.cashio.domain.usecase.expense

import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class ObserveExpensesByDateRangeUseCase(
    private val expenseRepository: ExpenseRepository
) {
    data class Params(
        val startDate: LocalDateTime,
        val endDate: LocalDateTime
    )

    operator fun invoke(params: Params): Flow<List<Expense>> =
        expenseRepository.observeExpensesByDateRange(params.startDate, params.endDate)
}
