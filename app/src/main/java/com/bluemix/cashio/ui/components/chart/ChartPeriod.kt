package com.bluemix.cashio.ui.components.chart

import com.bluemix.cashio.presentation.analytics.vm.ChartPeriod

val ChartPeriod.label: String
    get() = when (this) {
        ChartPeriod.WEEKLY -> "Weekly"
        ChartPeriod.MONTHLY -> "Monthly"
        ChartPeriod.YEARLY -> "Yearly"
    }
