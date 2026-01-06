package com.bluemix.cashio.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.domain.usecase.base.SeedDatabaseUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(
    private val seedDatabaseUseCase: SeedDatabaseUseCase
) : ViewModel() {

    fun initApp(onFinished: () -> Unit) {
        viewModelScope.launch {
            // 1. Seed Database (Safe to call every time; it checks counts internally)
            seedDatabaseUseCase()

            // 2. Artificial delay (optional) to show your logo animation
            // Remove this if you want instant startup
            delay(1000)

            // 3. Navigate to Dashboard
            onFinished()
        }
    }
}