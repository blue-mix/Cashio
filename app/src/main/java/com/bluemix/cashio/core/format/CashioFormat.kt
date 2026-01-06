package com.bluemix.cashio.core.format

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

/**
 * Central place for formatting + lightweight conversions.
 * Keep it JVM-only (no Compose / Android classes).
 */
object CashioFormat {

    /* ---------------------------------------------------------------------- */
    /* Locale / Zone                                                          */
    /* ---------------------------------------------------------------------- */

    fun locale(): Locale = Locale.getDefault()
    fun zoneId(): ZoneId = ZoneId.systemDefault()

    /* ---------------------------------------------------------------------- */
    /* Date / Time patterns                                                    */
    /* ---------------------------------------------------------------------- */

    private fun dayNameFormatter(locale: Locale) =
        DateTimeFormatter.ofPattern("EEEE", locale)

    private fun fullDateFormatter(locale: Locale) =
        DateTimeFormatter.ofPattern("dd MMM yyyy", locale)

    private fun dateLabelFormatter(locale: Locale) =
        DateTimeFormatter.ofPattern("dd MMM, yyyy", locale)

    private fun timeFormatter(locale: Locale) =
        DateTimeFormatter.ofPattern("HH:mm", locale)

    private fun monthLabelFormatter(locale: Locale) =
        DateTimeFormatter.ofPattern("MMMM", locale)

    private fun topBarDateFormatter(locale: Locale) =
        DateTimeFormatter.ofPattern("EEE, d MMM", locale)

    /* ---------------------------------------------------------------------- */
    /* Date helpers                                                            */
    /* ---------------------------------------------------------------------- */

    fun LocalDate.currentMonthLabel(locale: Locale = locale()): String =
        this.format(monthLabelFormatter(locale))

    fun LocalDate.toTopBarLabel(locale: Locale = Locale.ENGLISH): String =
        this.format(topBarDateFormatter(locale))

    fun LocalDate.toFullDate(locale: Locale = locale()): String =
        this.format(fullDateFormatter(locale))

    fun LocalDate.toDayName(locale: Locale = locale()): String =
        this.format(dayNameFormatter(locale))

    fun LocalDateTime.toDateLabel(locale: Locale = locale()): String =
        this.toLocalDate().format(dateLabelFormatter(locale))

    fun LocalDateTime.toTimeLabel(locale: Locale = locale()): String =
        this.toLocalTime().format(timeFormatter(locale))

    fun LocalDate.toPrettyMonthDay(locale: Locale = Locale.ENGLISH): String {
        val month = this.month.getDisplayName(TextStyle.SHORT, locale)
        return "$month $dayOfMonth${dayOfMonth.ordinalSuffix(locale)}"
    }

    private fun Int.ordinalSuffix(locale: Locale): String {
        if (!locale.language.equals(Locale.ENGLISH.language, ignoreCase = true)) return ""
        return when {
            this in 11..13 -> "th"
            this % 10 == 1 -> "st"
            this % 10 == 2 -> "nd"
            this % 10 == 3 -> "rd"
            else -> "th"
        }
    }

    /* ---------------------------------------------------------------------- */
    /* Epoch conversions                                                       */
    /* ---------------------------------------------------------------------- */

    fun LocalDateTime.toEpochMillis(zoneId: ZoneId = zoneId()): Long =
        this.atZone(zoneId).toInstant().toEpochMilli()

    fun Long.toLocalDate(zoneId: ZoneId = zoneId()): LocalDate =
        Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()

    /* ---------------------------------------------------------------------- */
    /* Text helpers                                                            */
    /* ---------------------------------------------------------------------- */

    fun Int.toTransactionCountLabel(): String =
        "$this transaction${if (this == 1) "" else "s"}"

    /* ---------------------------------------------------------------------- */
    /* Money formatting                                                        */
    /* ---------------------------------------------------------------------- */

    /**
     * Best default: locale-aware grouping + 2 decimals.
     * If you want always-English formatting, pass Locale.ENGLISH.
     */
    fun money(
        value: Double,
        currencySymbol: String = "₹",
        locale: Locale = locale()
    ): String {
        val nf = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return currencySymbol + nf.format(value)
    }

    /** Compact like 1.2K / 3.4M (good for chips, stats). */
    fun compactAmount(value: Double): String {
        val v = abs(value)
        return when {
            v >= 1_000_000 -> {
                val x = v / 1_000_000.0
                if (x < 10) String.format(Locale.US, "%.1fM", x) else String.format(
                    Locale.US,
                    "%.0fM",
                    x
                )
            }

            v >= 1_000 -> {
                val x = v / 1_000.0
                if (x < 10) String.format(Locale.US, "%.1fK", x) else String.format(
                    Locale.US,
                    "%.0fK",
                    x
                )
            }

            else -> String.format(Locale.US, "%.0f", v)
        }
    }

}
