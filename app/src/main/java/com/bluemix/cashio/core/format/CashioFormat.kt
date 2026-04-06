package com.bluemix.cashio.core.format

import com.bluemix.cashio.core.format.CashioFormat.compactAmount
import com.bluemix.cashio.core.format.CashioFormat.money
import com.bluemix.cashio.domain.model.Currency
import com.bluemix.cashio.domain.model.Money
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Formatting utilities for monetary amounts and dates.
 *
 * ## Performance
 * [DateTimeFormatter] instances are thread-safe and expensive to construct —
 * they are cached as `companion object` properties and created at most once per
 * locale change. [NumberFormat] is NOT thread-safe; a fresh instance is obtained
 * per call via the pooling inside [NumberFormat.getInstance], which is fast.
 *
 * ## Money sign convention
 * Negative amounts (e.g. credits displayed as deltas) retain their sign through
 * all formatters. Use [money] for full symbol+sign formatting and [compactAmount]
 * for abbreviated display (e.g. "₹1.2K").
 */
object CashioFormat {

    // ── Cached formatters ──────────────────────────────────────────────────
    // DateTimeFormatter is thread-safe; safe to share.

    var _locale: Locale = Locale.getDefault()
    private var _dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy", _locale)
    private var _timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", _locale)
    private var _dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", _locale)
    private var _shortDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM", _locale)
    private var _monthYearFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMMM yyyy", _locale)
    private var _dayFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE, dd MMM", _locale)

    /**
     * Call this when the user's locale changes (e.g. in [android.app.Application.onConfigurationChanged])
     * to refresh cached formatters. Under normal usage the locale is stable for the
     * process lifetime, so this is rarely needed.
     */
    fun invalidateLocale() {
        _locale = Locale.getDefault()
        _dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", _locale)
        _timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", _locale)
        _dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", _locale)
        _shortDateFormatter = DateTimeFormatter.ofPattern("dd MMM", _locale)
        _monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", _locale)
        _dayFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM", _locale)
    }

    // ── Date formatting ────────────────────────────────────────────────────

    fun date(dateTime: LocalDateTime): String = _dateFormatter.format(dateTime)
    fun date(date: LocalDate): String = _dateFormatter.format(date)
    fun time(dateTime: LocalDateTime): String = _timeFormatter.format(dateTime)
    fun dateTime(dateTime: LocalDateTime): String = _dateTimeFormatter.format(dateTime)
    fun shortDate(dateTime: LocalDateTime): String = _shortDateFormatter.format(dateTime)
    fun shortDate(date: LocalDate): String = _shortDateFormatter.format(date)
    fun monthYear(dateTime: LocalDateTime): String = _monthYearFormatter.format(dateTime)
    fun dayOfWeek(dateTime: LocalDateTime): String = _dayFormatter.format(dateTime)

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

    // ── Money formatting ───────────────────────────────────────────────────

    /**
     * Formats [amountPaise] as a locale-aware string with currency symbol.
     * e.g. `money(150075, Currency.INR)` → "₹1,500.75"
     *
     * Sign is preserved — negative values produce e.g. "-₹1,500.75".
     */
    fun money(amountPaise: Long, currency: Currency = Currency.INR): String {
        val value = amountPaise / 100.0
        val absValue = kotlin.math.abs(value)
        val sign = if (value < 0) "-" else ""
        val formatter = NumberFormat.getNumberInstance(_locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "$sign${currency.symbol}${formatter.format(absValue)}"
    }

    /**
     * Abbreviated money display for tight spaces (charts, heatmap cells).
     * e.g. `compactAmount(150075, Currency.INR)` → "₹1.5K"
     *
     * Sign is preserved — negative values produce e.g. "-₹1.5K".
     */
    fun compactAmount(amountPaise: Long, currency: Currency = Currency.INR): String {
        val value = amountPaise / 100.0
        val sign = if (value < 0) "-" else ""
        val abs = kotlin.math.abs(value)

        val (formatted, suffix) = when {
            abs >= 10_000_000.0 -> Pair("%.1f".format(abs / 10_000_000.0), "Cr")
            abs >= 100_000.0 -> Pair("%.1f".format(abs / 100_000.0), "L")
            abs >= 1_000.0 -> Pair("%.1f".format(abs / 1_000.0), "K")
            else -> Pair("%.0f".format(abs), "")
        }

        // Strip redundant ".0" suffix (e.g. "1.0K" → "1K")
        val trimmed = if (suffix.isNotEmpty() && formatted.endsWith(".0")) {
            formatted.dropLast(2)
        } else {
            formatted
        }

        return "$sign${currency.symbol}$trimmed$suffix"
    }

    /**
     * Formats a raw [Double] input value (from user entry) as a money string.
     * Use [money] for stored paise values wherever possible.
     */
    fun moneyFromDouble(amount: Double, currency: Currency = Currency.INR): String =
        money(Money.toPaise(amount), currency)

    /**
     * Returns a percentage string, e.g. 0.456 → "45.6%"
     */
    fun percentage(ratio: Double): String = "%.1f%%".format(ratio * 100.0)
}