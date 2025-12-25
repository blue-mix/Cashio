package com.bluemix.cashio.domain.model

import java.time.LocalDateTime

/**
 * Intermediate model for parsed SMS data
 * Before converting to Expense
 */
data class ParsedSmsTransaction(
    val amount: Double,
    val transactionType: TransactionType,
    val merchantName: String?,
    val accountNumber: String?,
    val timestamp: LocalDateTime,
    val rawSmsBody: String,
    val bankName: String?
) {
    /**
     * Convert parsed SMS to Expense
     */
    fun toExpense(
        category: Category,
        id: String = generateExpenseId()
    ): Expense {
        return Expense(
            id = id,
            amount = amount,
            title = merchantName ?: "Transaction",
            category = category,
            date = timestamp,
            transactionType = transactionType,
            source = ExpenseSource.SMS,
            rawSmsBody = rawSmsBody,
            merchantName = merchantName,
            note = "Auto-detected from SMS"
        )
    }

    private fun generateExpenseId(): String {
        return "exp_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}
