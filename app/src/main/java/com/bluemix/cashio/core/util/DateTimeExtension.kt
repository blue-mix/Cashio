package com.bluemix.cashio.ui.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Converts LocalDateTime to epoch milliseconds for Material 3 DatePicker
 */
fun LocalDateTime.toEpochMillis(): Long {
    return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

/**
 * Converts epoch milliseconds to LocalDate
 */
fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

/**
 * Formats LocalDateTime as a readable date string (e.g., "19 Feb 2026")
 */
fun LocalDateTime.toDateLabel(): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    return this.format(formatter)
}

/**
 * Formats LocalDateTime as a readable time string (e.g., "02:30 PM")
 */
fun LocalDateTime.toTimeLabel(): String {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
    return this.format(formatter)
}