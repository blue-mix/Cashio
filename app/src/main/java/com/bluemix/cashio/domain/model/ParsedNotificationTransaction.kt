package com.bluemix.cashio.domain.model

import java.time.LocalDateTime

/**
 * Intermediate model holding a parsed UPI app notification transaction,
 * before it is persisted as an [Expense].
 *
 * [notifId] is a stable content-fingerprint built from package name, amount,
 * merchant, and a bucketed timestamp. It becomes [Expense.id] to guarantee
 * idempotent inserts across notification re-deliveries.
 */
data class ParsedNotificationTransaction(
    /** Stable deduplication key — becomes [Expense.id]. */
    val notifId: String,

    val packageName: String,
    val postTimeMillis: Long,
    val title: String?,
    val text: String?,

    /** Transaction value in paise (smallest currency unit). Always positive. */
    val amountPaise: Long,

    val transactionType: TransactionType,
    val merchantName: String?,
    val timestamp: LocalDateTime,

    /**
     * Concatenated notification title + body.
     * Sensitive if it contains account references — do not log or display.
     */
    val rawBody: String,

    val appName: String?
) {
    /** Converts to [Expense], using [notifId] as the stable primary key. */
    fun toExpense(category: Category): Expense = Expense(
        id = notifId,
        amountPaise = amountPaise,
        title = merchantName ?: appName ?: "UPI Payment",
        category = category,
        date = timestamp,
        transactionType = transactionType,
        source = ExpenseSource.NOTIFICATION,
        rawSmsBody = rawBody,
        merchantName = merchantName,
        note = ""
    )
}