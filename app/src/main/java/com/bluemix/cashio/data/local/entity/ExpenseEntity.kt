package com.bluemix.cashio.data.local.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Realm persistence entity for [com.bluemix.cashio.domain.model.Expense].
 *
 * ## Money
 * [amountPaise] stores the transaction value in paise (smallest currency unit)
 * as a [Long] to avoid floating-point accumulation errors. Never store Double.
 *
 * ## Date
 * [dateMillis] stores epoch-milliseconds. The [date] computed property is
 * annotated [@Ignore] so Realm does not attempt to persist it.
 * Zone resolution uses a cached [ZoneId] passed via the mapper — not
 * [ZoneId.systemDefault] on every read.
 */
class ExpenseEntity : RealmObject {

    @PrimaryKey
    var id: String = ""

    /** Transaction value in paise. Always positive; [transactionType] gives direction. */
    var amountPaise: Long = 0L

    var title: String = ""

    /** FK reference to [CategoryEntity.id]. */
    var categoryId: String = ""

    @Index
    var dateMillis: Long = 0L

    var note: String = ""

    /**
     * Persisted as its [com.bluemix.cashio.domain.model.ExpenseSource.name] string.
     * Use [ExpenseSourceValues] constants to avoid magic strings.
     */
    var source: String = ExpenseSourceValues.MANUAL

    /**
     * Raw SMS/notification body — sensitive, may contain account numbers.
     * Stored for re-parse debugging only. Do not display or log.
     */
    var rawSmsBody: String? = null

    var merchantName: String? = null

    /**
     * Persisted as its [com.bluemix.cashio.domain.model.TransactionType.name] string.
     * Use [TransactionTypeValues] constants to avoid magic strings.
     */
    var transactionType: String = TransactionTypeValues.EXPENSE

    // ── Computed helpers (Realm ignores these) ─────────────────────────────

    /**
     * Convenience accessor. Zone must match what was used during write.
     * Prefer using [dateMillis] directly in queries.
     */
    @Ignore
    var date: LocalDateTime
        get() = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(dateMillis),
            ZoneId.systemDefault()
        )
        set(value) {
            dateMillis = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
}

/**
 * String constants for [ExpenseEntity.source].
 * Single source of truth — changes here propagate everywhere automatically.
 */
object ExpenseSourceValues {
    const val MANUAL = "MANUAL"
    const val SMS = "SMS"
    const val NOTIFICATION = "NOTIFICATION"
}

/**
 * String constants for [ExpenseEntity.transactionType].
 */
object TransactionTypeValues {
    const val EXPENSE = "EXPENSE"
    const val INCOME = "INCOME"
}