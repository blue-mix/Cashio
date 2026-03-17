package com.bluemix.cashio.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Utility for converting between monetary representations.
 *
 * The canonical internal representation is **paise** ([Long]) — the smallest
 * unit for INR (and analogous units for other currencies). This avoids all
 * floating-point accumulation errors when summing many transactions.
 *
 * Usage:
 * ```
 * val paise = Money.toPaise(100.50)   // → 10050L
 * val display = Money.toDouble(10050) // → 100.50
 * ```
 */
object Money {

    /**
     * Converts a user-entered [Double] (e.g. "100.50") to paise.
     * Uses [BigDecimal] to avoid floating-point precision loss during conversion.
     */
    fun toPaise(amount: Double): Long {
        return BigDecimal.valueOf(amount)
            .setScale(2, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100))
            .toLong()
    }

    /**
     * Converts paise back to a [Double] for display formatting only.
     * Never use the result for further arithmetic.
     */
    fun toDouble(paise: Long): Double = paise / 100.0

    /**
     * Parses a user input string to paise.
     * Returns `null` if the string is not a valid positive monetary amount.
     */
    fun parseInput(input: String): Long? {
        val value = input.trim().toDoubleOrNull() ?: return null
        if (value <= 0.0) return null
        return toPaise(value)
    }

    /**
     * Formats paise as a human-readable string with 2 decimal places.
     * For locale-aware symbol formatting, use [com.bluemix.cashio.core.format.CashioFormat.money].
     */
    fun format(paise: Long): String = "%.2f".format(toDouble(paise))

    /** Sums a collection of paise values safely. */
    fun Iterable<Long>.sumPaise(): Long = fold(0L, Long::plus)
}