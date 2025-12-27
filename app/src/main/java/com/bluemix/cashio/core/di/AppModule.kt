package com.bluemix.cashio.core.di

import com.bluemix.cashio.presentation.analytics.vm.AnalyticsViewModel
import com.bluemix.cashio.presentation.categories.CategoriesViewModel
import com.bluemix.cashio.presentation.history.HistoryViewModel
import com.bluemix.cashio.presentation.home.DashboardViewModel
import com.bluemix.cashio.presentation.keywordmapping.KeywordMappingViewModel
import com.bluemix.cashio.presentation.onboarding.OnboardingViewModel
import com.bluemix.cashio.presentation.settings.vm.SettingsViewModel
import com.bluemix.cashio.presentation.add.AddExpenseViewModel
import com.bluemix.cashio.presentation.splash.SplashViewModel
import com.bluemix.cashio.presentation.transaction.TransactionViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // ViewModels
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::AddExpenseViewModel)
    viewModelOf(::CategoriesViewModel)
    viewModelOf(::AnalyticsViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::KeywordMappingViewModel)
    viewModelOf(::TransactionViewModel)
    viewModelOf(::SplashViewModel)

}
