package com.bluemix.cashio.components

import com.bluemix.cashio.presentation.analytics.ChartPeriod

val ChartPeriod.label: String
    get() = when (this) {
        ChartPeriod.WEEKLY -> "Weekly"
        ChartPeriod.MONTHLY -> "Monthly"
        ChartPeriod.YEARLY -> "Yearly"
    }
