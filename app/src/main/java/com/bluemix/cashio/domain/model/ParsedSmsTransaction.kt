package com.bluemix.cashio.domain.model

import java.time.LocalDateTime

/**
 * Intermediate model holding a successfully parsed SMS transaction,
 * before it is persisted as an [Expense].
 *
 * [smsId] is a stable content-fingerprint (SHA-256 of amount + merchant + bucketed timestamp)
 * used as the [Expense.id] so that re-parsing the same SMS never creates duplicates.
 */
data class ParsedSmsTransaction(
    /** Stable deduplication key — becomes [Expense.id]. */
    val smsId: String,

    /** Epoch millis of the SMS row in the device inbox. */
    val smsDateMillis: Long,

    val smsAddress: String?,

    /** Transaction value in paise (smallest currency unit). Always positive. */
    val amountPaise: Long,

    val transactionType: TransactionType,
    val merchantName: String?,
    val accountNumber: String?,
    val timestamp: LocalDateTime,

    /**
     * Raw SMS body — sensitive (may contain account numbers, OTPs).
     * Retained only for re-parse debugging; should not be displayed or logged.
     */
    val rawSmsBody: String,

    val bankName: String?
) {
    /** Converts to [Expense], using [smsId] as the stable primary key. */
    fun toExpense(category: Category): Expense = Expense(
        id = smsId,
        amountPaise = amountPaise,
        title = merchantName ?: bankName ?: "Bank Transaction",
        category = category,
        date = timestamp,
        transactionType = transactionType,
        source = ExpenseSource.SMS,
        rawSmsBody = rawSmsBody,
        merchantName = merchantName,
        note = ""   // Note left blank; user can annotate manually
    )
}