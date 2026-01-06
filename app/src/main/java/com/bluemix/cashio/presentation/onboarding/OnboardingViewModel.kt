package com.bluemix.cashio.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.data.local.preferences.UserPreferencesDataStore
import com.bluemix.cashio.domain.usecase.base.SeedDatabaseUseCase
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val userPrefs: UserPreferencesDataStore,
    private val seedDatabaseUseCase: SeedDatabaseUseCase
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            // 1. Seed the database now (so it's ready when we hit Dashboard)
            seedDatabaseUseCase()

            // 2. Mark onboarding as done
            userPrefs.setOnboardingCompleted(true)
        }
    }
}