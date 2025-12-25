package com.bluemix.cashio.domain.usecase.expense

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.repository.ExpenseRepository

/**
 * Use case to get a single expense by its ID
 */
class GetExpenseByIdUseCase(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(expenseId: String): Result<Expense?> {
        return expenseRepository.getExpenseById(expenseId)
    }
}
