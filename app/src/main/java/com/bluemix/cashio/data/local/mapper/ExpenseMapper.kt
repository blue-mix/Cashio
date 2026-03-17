package com.bluemix.cashio.data.local.mapper

import com.bluemix.cashio.data.local.entity.ExpenseEntity
import com.bluemix.cashio.data.local.entity.ExpenseSourceValues
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.ExpenseSource
import com.bluemix.cashio.domain.model.TransactionType
import java.time.ZoneId

/**
 * Bi-directional mapper between [Expense] domain model and [ExpenseEntity].
 *
 * [zoneId] is accepted as a parameter (defaulting to system) so callers can
 * cache and reuse a single instance across bulk mapping operations, avoiding
 * repeated [ZoneId.systemDefault] lookups per row.
 */

fun ExpenseEntity.toDomain(
    category: Category,
    zoneId: ZoneId = ZoneId.systemDefault()
): Expense = Expense(
    id = id,
    amountPaise = amountPaise,
    title = title,
    category = category,
    date = java.time.LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(dateMillis), zoneId
    ),
    note = note,
    source = when (source) {
        ExpenseSourceValues.SMS -> ExpenseSource.SMS
        ExpenseSourceValues.NOTIFICATION -> ExpenseSource.NOTIFICATION
        else -> ExpenseSource.MANUAL
    },
    rawSmsBody = rawSmsBody,
    merchantName = merchantName,
    transactionType = TransactionType.fromString(transactionType)
)

fun Expense.toEntity(zoneId: ZoneId = ZoneId.systemDefault()): ExpenseEntity =
    ExpenseEntity().apply {
        id = this@toEntity.id
        amountPaise = this@toEntity.amountPaise
        title = this@toEntity.title
        categoryId = this@toEntity.category.id
        // Set dateMillis directly — do NOT also call the computed `date` setter,
        // which would run a second ZoneId lookup and overwrite the value redundantly.
        dateMillis = this@toEntity.date.atZone(zoneId).toInstant().toEpochMilli()
        note = this@toEntity.note
        source = when (this@toEntity.source) {
            ExpenseSource.SMS -> ExpenseSourceValues.SMS
            ExpenseSource.NOTIFICATION -> ExpenseSourceValues.NOTIFICATION
            ExpenseSource.MANUAL -> ExpenseSourceValues.MANUAL
        }
        rawSmsBody = this@toEntity.rawSmsBody
        merchantName = this@toEntity.merchantName
        transactionType = this@toEntity.transactionType.name
    }