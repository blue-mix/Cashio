package com.bluemix.cashio.domain.usecase.expense

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.usecase.base.UseCase

/**
 * Add a new expense
 */
class AddExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<Expense, Unit>() {

    override suspend fun execute(params: Expense): Result<Unit> {
        return expenseRepository.addExpense(params)
    }
}
