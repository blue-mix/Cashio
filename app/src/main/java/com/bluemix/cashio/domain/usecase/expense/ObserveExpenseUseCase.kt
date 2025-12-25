package com.bluemix.cashio.domain.usecase.expense

import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class ObserveExpensesUseCase(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<Expense>> = expenseRepository.observeExpenses()
}
