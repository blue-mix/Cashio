package com.bluemix.cashio.domain.usecase.expense

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.usecase.base.UseCase

class RecategorizeExpensesByKeywordUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<String, Int>() {

    override suspend fun execute(params: String): Result<Int> {
        val keyword = params.trim()
        if (keyword.isBlank()) return Result.Success(0)

        return expenseRepository.recategorizeExpensesByKeyword(keyword)
    }
}
