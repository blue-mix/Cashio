package com.bluemix.cashio.core.di

import com.bluemix.cashio.domain.usecase.base.SeedDatabaseUseCase
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
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    // Common
    factoryOf(::SeedDatabaseUseCase)

    // Expense
    factoryOf(::ObserveExpensesUseCase)
    factoryOf(::ObserveExpensesByDateRangeUseCase)
    factoryOf(::GetExpensesUseCase)
    factoryOf(::GetExpensesByDateRangeUseCase)
    factoryOf(::GetExpenseByIdUseCase)
    factoryOf(::AddExpenseUseCase)
    factoryOf(::UpdateExpenseUseCase)
    factoryOf(::DeleteExpenseUseCase)
    factoryOf(::GetFinancialStatsUseCase)
    factoryOf(::RefreshExpensesFromSmsUseCase)
    factoryOf(::RecategorizeExpensesByKeywordUseCase)

    // Category
    factoryOf(::GetCategoriesUseCase)
    factoryOf(::AddCategoryUseCase)
    factoryOf(::UpdateCategoryUseCase)
    factoryOf(::DeleteCategoryUseCase)

    // Keyword
    factoryOf(::GetKeywordMappingsUseCase)
    factoryOf(::AddKeywordMappingUseCase)
    factoryOf(::UpdateKeywordMappingUseCase)
    factoryOf(::DeleteKeywordMappingUseCase)
}