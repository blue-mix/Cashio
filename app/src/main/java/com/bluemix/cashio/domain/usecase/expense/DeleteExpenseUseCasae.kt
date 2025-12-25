package com.bluemix.cashio.domain.usecase.expense

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.usecase.base.UseCase

/**
 * Delete an expense
 */
class DeleteExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<String, Unit>() {

    override suspend fun execute(params: String): Result<Unit> {
        return expenseRepository.deleteExpense(params)
    }
}
