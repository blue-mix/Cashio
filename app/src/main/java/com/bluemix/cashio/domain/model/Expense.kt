package com.bluemix.cashio.domain.model

import java.time.LocalDateTime

/**
 * Domain model representing a single financial transaction (expense or income).
 *
 * ## Money representation
 * [amountPaise] stores value in the **smallest currency unit** (paise for INR, cents for USD, etc.)
 * as a [Long] to avoid floating-point accumulation errors over aggregations.
 * Use [amountAsDouble] for display only — never for arithmetic.
 *
 * ## Privacy
 * [rawSmsBody] may contain sensitive bank data (account numbers, balances). It is
 * stored for debugging/re-parse purposes only. Consider clearing it after a
 * successful parse round-trip, and never log or display it in the UI.
 */
data class Expense(
    val id: String,

    /**
     * Transaction value in the smallest currency unit (e.g. paise, cents).
     * Always positive — [transactionType] determines the direction.
     */
    val amountPaise: Long,

    val title: String,
    val category: Category,
    val date: LocalDateTime,
    val note: String = "",
    val source: ExpenseSource = ExpenseSource.MANUAL,

    /** Raw SMS body — sensitive, see class-level privacy note. */
    val rawSmsBody: String? = null,

    val merchantName: String? = null,
    val transactionType: TransactionType
) {
    /**
     * Convenience accessor for display formatting.
     * Do **not** use for arithmetic; use [amountPaise] directly.
     */
    val amountAsDouble: Double get() = amountPaise / 100.0

    companion object {
        /** Convert a user-entered [Double] to paise, rounding half-up. */
        fun paise(amount: Double): Long = (amount * 100).toLong()
    }
}

/**
 * Source of the expense entry.
 */
enum class ExpenseSource {
    MANUAL,
    SMS,
    NOTIFICATION
}