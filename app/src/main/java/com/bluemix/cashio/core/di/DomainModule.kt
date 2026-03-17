package com.bluemix.cashio.core.di

import com.bluemix.cashio.domain.usecase.base.SeedDatabaseUseCase
import com.bluemix.cashio.domain.usecase.category.AddCategoryUseCase
import com.bluemix.cashio.domain.usecase.category.DeleteCategoryUseCase
import com.bluemix.cashio.domain.usecase.category.GetCategoriesUseCase
import com.bluemix.cashio.domain.usecase.category.ObserveCategoriesUseCase
import com.bluemix.cashio.domain.usecase.category.UpdateCategoryUseCase
import com.bluemix.cashio.domain.usecase.expense.AddExpenseUseCase
import com.bluemix.cashio.domain.usecase.expense.DeleteExpenseUseCase
import com.bluemix.cashio.domain.usecase.expense.DeleteExpensesUseCase
import com.bluemix.cashio.domain.usecase.expense.GetAllExpensesUseCase
import com.bluemix.cashio.domain.usecase.expense.GetExpenseByIdUseCase
import com.bluemix.cashio.domain.usecase.expense.GetExpensesByCategoryUseCase
import com.bluemix.cashio.domain.usecase.expense.GetExpensesByDateRangeUseCase
import com.bluemix.cashio.domain.usecase.expense.GetExpensesByTypeUseCase
import com.bluemix.cashio.domain.usecase.expense.GetFinancialStatsUseCase
import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesByDateRangeUseCase
import com.bluemix.cashio.domain.usecase.expense.ObserveExpensesUseCase
import com.bluemix.cashio.domain.usecase.expense.RecategorizeExpensesByKeywordUseCase
import com.bluemix.cashio.domain.usecase.expense.RefreshExpensesFromSmsUseCase
import com.bluemix.cashio.domain.usecase.expense.UpdateExpenseUseCase
import com.bluemix.cashio.domain.usecase.keywordmapping.AddKeywordMappingUseCase
import com.bluemix.cashio.domain.usecase.keywordmapping.DeleteKeywordMappingUseCase
import com.bluemix.cashio.domain.usecase.keywordmapping.DeleteKeywordMappingsByCategoryUseCase
import com.bluemix.cashio.domain.usecase.keywordmapping.GetKeywordMappingsByCategoryUseCase
import com.bluemix.cashio.domain.usecase.keywordmapping.GetKeywordMappingsUseCase
import com.bluemix.cashio.domain.usecase.keywordmapping.ObserveKeywordMappingsUseCase
import com.bluemix.cashio.domain.usecase.keywordmapping.UpdateKeywordMappingUseCase
import com.bluemix.cashio.domain.usecase.preferences.ClearAllDataUseCase
import com.bluemix.cashio.domain.usecase.preferences.GetSelectedCurrencyUseCase
import com.bluemix.cashio.domain.usecase.preferences.ObserveDarkModeUseCase
import com.bluemix.cashio.domain.usecase.preferences.ObserveOnboardingCompletedUseCase
import com.bluemix.cashio.domain.usecase.preferences.ObserveSelectedCurrencyUseCase
import com.bluemix.cashio.domain.usecase.preferences.SetDarkModeUseCase
import com.bluemix.cashio.domain.usecase.preferences.SetOnboardingCompletedUseCase
import com.bluemix.cashio.domain.usecase.preferences.SetSelectedCurrencyUseCase
import org.koin.dsl.module

val domainModule = module {
    // Common
    // ── Base use cases ─────────────────────────────────────────────────────

    single { SeedDatabaseUseCase(get()) }

    // ── Category use cases ─────────────────────────────────────────────────

    single { GetCategoriesUseCase(get()) }
    single { ObserveCategoriesUseCase(get()) }
    single { AddCategoryUseCase(get()) }
    single { UpdateCategoryUseCase(get()) }
    single { DeleteCategoryUseCase(get()) }

    // ── Expense use cases ──────────────────────────────────────────────────

    single { ObserveExpensesUseCase(get()) }
    single { ObserveExpensesByDateRangeUseCase(get()) }
    single { GetAllExpensesUseCase(get()) }
    single { GetExpenseByIdUseCase(get()) }
    single { GetExpensesByDateRangeUseCase(get()) }
    single { GetExpensesByCategoryUseCase(get()) }
    single { GetExpensesByTypeUseCase(get()) }
    single { AddExpenseUseCase(get()) }
    single { UpdateExpenseUseCase(get()) }
    single { DeleteExpenseUseCase(get()) }
    single { DeleteExpensesUseCase(get()) }
    single { GetFinancialStatsUseCase(get()) }
    single { RefreshExpensesFromSmsUseCase(get()) }
    single { RecategorizeExpensesByKeywordUseCase(get()) }

    // ── Keyword mapping use cases ──────────────────────────────────────────

    single { ObserveKeywordMappingsUseCase(get()) }
    single { GetKeywordMappingsUseCase(get()) }
    single { GetKeywordMappingsByCategoryUseCase(get()) }
    single { AddKeywordMappingUseCase(get(), get()) }
    single { UpdateKeywordMappingUseCase(get(), get()) }
    single { DeleteKeywordMappingUseCase(get()) }
    single { DeleteKeywordMappingsByCategoryUseCase(get()) }

    // ── Preferences use cases ──────────────────────────────────────────────

    single { ObserveOnboardingCompletedUseCase(get()) }
    single { SetOnboardingCompletedUseCase(get()) }
    single { ObserveDarkModeUseCase(get()) }
    single { SetDarkModeUseCase(get()) }
    single { ObserveSelectedCurrencyUseCase(get()) }
    single { GetSelectedCurrencyUseCase(get()) }
    single { SetSelectedCurrencyUseCase(get()) }

    single { ClearAllDataUseCase(get(), get(), get()) }
}