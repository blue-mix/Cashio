package com.bluemix.cashio.domain.model

import java.time.LocalDateTime

/**
 * Domain model for an expense
 * Clean business logic representation - no database dependencies
 */
data class Expense(
    val id: String,
    val amount: Double,
    val title: String,
    val category: Category,
    val date: LocalDateTime,
    val note: String = "",
    val source: ExpenseSource = ExpenseSource.MANUAL,
    val rawSmsBody: String? = null,  // Original SMS text if from SMS
    val merchantName: String? = null,
    val transactionType: TransactionType
)

/**
 * Source of the expense entry
 */
enum class ExpenseSource {
    MANUAL,  // User added manually
    SMS,      // Parsed from bank SMS
}
