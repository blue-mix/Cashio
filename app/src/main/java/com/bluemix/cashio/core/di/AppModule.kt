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
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::DashboardViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::TransactionViewModel)
    viewModelOf(::AddExpenseViewModel)
    viewModelOf(::CategoriesViewModel)
    viewModelOf(::AnalyticsViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::KeywordMappingViewModel)
    viewModelOf(::OnboardingViewModel)
}