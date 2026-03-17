package com.bluemix.cashio.domain.usecase.expense

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.usecase.base.NoParamsUseCase
import com.bluemix.cashio.domain.usecase.base.UseCase

/**
 * Triggers a fresh SMS inbox scan and persists any new bank transactions.
 * Returns the count of newly added expenses.
 */
class RefreshExpensesFromSmsUseCase(
    private val expenseRepository: ExpenseRepository
) : NoParamsUseCase<Int>() {
    override suspend fun execute(): Result<Int> =
        expenseRepository.refreshExpensesFromSms()
}

/**
 * Re-applies keyword mapping rules to all non-manual expenses whose merchant
 * or title contains [keyword]. Returns the count of updated expenses.
 *
 * Called automatically after a keyword mapping is saved or updated so that
 * historical transactions are retroactively recategorised.
 */
class RecategorizeExpensesByKeywordUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<String, Int>() {

    override suspend fun execute(params: String): Result<Int> {
        if (params.isBlank()) {
            return Result.Error(
                IllegalArgumentException("Keyword cannot be blank"),
                "Keyword is required"
            )
        }
        return expenseRepository.recategorizeExpensesByKeyword(params.trim())
    }
}