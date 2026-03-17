package com.bluemix.cashio.core.di

import com.bluemix.cashio.presentation.add.AddExpenseViewModel
import com.bluemix.cashio.presentation.analytics.vm.AnalyticsViewModel
import com.bluemix.cashio.presentation.categories.CategoriesViewModel
import com.bluemix.cashio.presentation.history.HistoryViewModel
import com.bluemix.cashio.presentation.home.DashboardViewModel
import com.bluemix.cashio.presentation.keyword.KeywordMappingViewModel
import com.bluemix.cashio.presentation.onboarding.OnboardingViewModel
import com.bluemix.cashio.presentation.settings.vm.SettingsViewModel
import com.bluemix.cashio.presentation.transaction.TransactionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    viewModel { OnboardingViewModel(get(), get()) }
    viewModel {
        SettingsViewModel(
            observeDarkModeUseCase = get(),
            setDarkModeUseCase = get(),
            getKeywordMappingsUseCase = get(),
            clearAllDataUseCase = get()
        )
    }

    viewModel {
        DashboardViewModel(
            observeExpensesByDateRangeUseCase = get(),
            observeSelectedCurrencyUseCase = get(),
            refreshExpensesFromSmsUseCase = get()
        )
    }

    viewModel {
        AddExpenseViewModel(
            getCategoriesUseCase = get(),
            addExpenseUseCase = get(),
            updateExpenseUseCase = get(),
            getExpenseByIdUseCase = get()
        )
    }

    viewModel {
        AnalyticsViewModel(
            getFinancialStatsUseCase = get(),
            getExpensesByDateRangeUseCase = get(),
            observeSelectedCurrencyUseCase = get()
        )
    }

    viewModel {
        HistoryViewModel(
            observeExpensesUseCase = get(),
            observeSelectedCurrencyUseCase = get()
        )
    }

    viewModel {
        CategoriesViewModel(
            observeCategoriesUseCase = get(),
            addCategoryUseCase = get(),
            updateCategoryUseCase = get(),
            deleteCategoryUseCase = get()
        )
    }

    viewModel {
        KeywordMappingViewModel(
            getKeywordMappings = get(),
            addKeywordMapping = get(),
            updateKeywordMapping = get(),
            deleteKeywordMapping = get(),
            recategorizeExpensesByKeyword = get(),
            getCategories = get()
        )
    }
    viewModel {
        TransactionViewModel(
            observeExpensesUseCase = get(),
            getExpenseByIdUseCase = get(),
            deleteExpenseUseCase = get(),
            observeSelectedCurrencyUseCase = get()
        )
    }
}