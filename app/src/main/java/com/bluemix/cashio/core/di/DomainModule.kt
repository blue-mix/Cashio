package com.bluemix.cashio.core.di

import com.bluemix.cashio.domain.usecase.category.AddCategoryUseCase
import com.bluemix.cashio.domain.usecase.category.DeleteCategoryUseCase
import com.bluemix.cashio.domain.usecase.category.GetCategoriesUseCase
import com.bluemix.cashio.domain.usecase.category.UpdateCategoryUseCase
import com.bluemix.cashio.domain.usecase.expense.AddExpenseUseCase
import com.bluemix.cashio.domain.usecase.expense.DeleteExpenseUseCase
import com.bluemix.cashio.domain.usecase.expense.GetExpenseByIdUseCase
import com.bluemix.cashio.domain.usecase.expense.GetExpensesByDateRangeUseCase
import com.bluemix.cashio.domain.usecase.expense.GetExpensesUseCase
import com.bluemix.cashio.domain.usecase.expense.GetFinancialStatsUseCase
import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesByDateRangeUseCase
import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesUseCase
import com.bluemix.cashio.domain.usecase.expense.RecategorizeExpensesByKeywordUseCase
import com.bluemix.cashio.domain.usecase.expense.RefreshExpensesFromSmsUseCase
import com.bluemix.cashio.domain.usecase.expense.UpdateExpenseUseCase
import com.bluemix.cashio.domain.usecase.keyword.AddKeywordMappingUseCase
import com.bluemix.cashio.domain.usecase.keyword.DeleteKeywordMappingUseCase
import com.bluemix.cashio.domain.usecase.keyword.GetKeywordMappingsUseCase
import com.bluemix.cashio.domain.usecase.keyword.UpdateKeywordMappingUseCase
import org.koin.dsl.module

val domainModule = module {
    // Expense Use Cases
    factory { GetExpensesUseCase(get()) }
    factory { GetExpenseByIdUseCase(get()) }
    factory { GetExpensesByDateRangeUseCase(get()) }
    factory { AddExpenseUseCase(get()) }
    factory { UpdateExpenseUseCase(get()) }
    factory { DeleteExpenseUseCase(get()) }
    factory { GetFinancialStatsUseCase(get()) }
    factory { RefreshExpensesFromSmsUseCase(get()) }
    // DomainModule.kt
    factory { ObserveExpensesUseCase(get()) }
    factory { ObserveExpensesByDateRangeUseCase(get()) }


    // Category Use Cases
    factory { GetCategoriesUseCase(get()) }
    factory { AddCategoryUseCase(get()) }
    factory { UpdateCategoryUseCase(get()) }
    factory { DeleteCategoryUseCase(get()) }

    // Keyword Use Cases (minimal)
    factory { GetKeywordMappingsUseCase(get()) }
    factory { AddKeywordMappingUseCase(get()) }
    factory { DeleteKeywordMappingUseCase(get()) }
    factory { UpdateKeywordMappingUseCase(get()) }
    factory { RecategorizeExpensesByKeywordUseCase(get()) }


}
