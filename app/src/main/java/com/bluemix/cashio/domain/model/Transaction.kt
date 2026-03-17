package com.bluemix.cashio.domain.model

/**
 * Direction of a financial transaction.
 */
enum class TransactionType {
    INCOME,
    EXPENSE;

    companion object {
        /**
         * Returns the [TransactionType] for [value] (case-insensitive),
         * or [EXPENSE] as a safe fallback if the value is unrecognised.
         *
         * Only [IllegalArgumentException] from [valueOf] is caught — other
         * exceptions are allowed to propagate so genuine programming errors
         * are not silently swallowed.
         */
        fun fromString(value: String): TransactionType {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                EXPENSE
            }
        }
    }
}