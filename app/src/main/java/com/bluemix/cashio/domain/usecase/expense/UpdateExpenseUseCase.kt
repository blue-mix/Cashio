package com.bluemix.cashio.domain.usecase.expense

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.usecase.base.UseCase

/**
 * Update an existing expense
 */
class UpdateExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<Expense, Unit>() {

    override suspend fun execute(params: Expense): Result<Unit> {
        return expenseRepository.updateExpense(params)
    }
}
