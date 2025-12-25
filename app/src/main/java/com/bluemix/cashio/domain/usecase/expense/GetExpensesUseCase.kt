package com.bluemix.cashio.domain.usecase.expense

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.usecase.base.NoParamsUseCase

/**
 * Get all expenses
 */
class GetExpensesUseCase(
    private val expenseRepository: ExpenseRepository
) : NoParamsUseCase<List<Expense>>() {

    override suspend fun execute(): Result<List<Expense>> {
        return expenseRepository.getAllExpenses()
    }
}
