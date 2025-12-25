package com.bluemix.cashio.domain.usecase.expense

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.usecase.base.NoParamsUseCase

/**
 * Refresh expenses from SMS messages
 */
class RefreshExpensesFromSmsUseCase(
    private val expenseRepository: ExpenseRepository
) : NoParamsUseCase<Int>() {

    override suspend fun execute(): Result<Int> {
        return expenseRepository.refreshExpensesFromSms()
    }
}
