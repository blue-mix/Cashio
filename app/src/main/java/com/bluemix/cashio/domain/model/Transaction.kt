package com.bluemix.cashio.domain.model

/**
 * Type of financial transaction
 */
enum class TransactionType {
    INCOME,
    EXPENSE;

    companion object {
        fun fromString(value: String): TransactionType {
            return when (value.uppercase()) {
                "INCOME" -> INCOME
                "EXPENSE" -> EXPENSE
                else -> EXPENSE // Default
            }
        }
    }
}
