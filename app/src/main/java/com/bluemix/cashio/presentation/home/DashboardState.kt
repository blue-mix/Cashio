package com.bluemix.cashio.presentation.home

import com.bluemix.cashio.domain.model.DateRange
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.presentation.common.UiState

data class DashboardState(
    val totalExpenses: Double = 0.0,
    val walletBalance: Double = 0.0,
    val percentageChange: Float = 0f,
    val isIncrease: Boolean = false,

    val recentExpenses: UiState<List<Expense>> = UiState.Idle,

    val isRefreshingSms: Boolean = false,
    val smsRefreshMessage: String? = null,

    val selectedDateRange: DateRange = DateRange.THIS_MONTH
)
