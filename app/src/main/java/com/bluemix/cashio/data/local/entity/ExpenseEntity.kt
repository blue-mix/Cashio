package com.bluemix.cashio.data.local.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Realm entity for Expense
 * Optimized: date stored as Long (epoch millis), not String
 */
class ExpenseEntity : RealmObject {
    @PrimaryKey
    var id: String = ""

    var amount: Double = 0.0
    var title: String = ""
    var categoryId: String = ""  // Reference to CategoryEntity

    @Index  // Index for faster date queries
    var dateMillis: Long = 0L  //  Optimized: Long instead of String

    var note: String = ""
    var source: String = "MANUAL"  // "MANUAL" or "SMS"
    var rawSmsBody: String? = null
    var merchantName: String? = null
    var transactionType: String = "EXPENSE"

    // Helper to convert to/from LocalDateTime
    var date: LocalDateTime
        get() = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(dateMillis),
            ZoneId.systemDefault()
        )
        set(value) {
            dateMillis = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
}
