package com.bluemix.cashio.data.local.mapper

import com.bluemix.cashio.data.local.entity.ExpenseEntity
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.ExpenseSource
import com.bluemix.cashio.domain.model.TransactionType

/**
 * Convert between Expense domain model and ExpenseEntity
 */
fun ExpenseEntity.toDomain(category: Category): Expense {
    return Expense(
        id = id,
        amount = amount,
        title = title,
        category = category,
        date = date,
        note = note,
        source = when (source) {
            "SMS" -> ExpenseSource.SMS
            else -> ExpenseSource.MANUAL
        },
        rawSmsBody = rawSmsBody,
        merchantName = merchantName,
        transactionType = TransactionType.fromString(transactionType)
    )
}

fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity().apply {
        id = this@toEntity.id
        amount = this@toEntity.amount
        title = this@toEntity.title
        categoryId = this@toEntity.category.id
        date = this@toEntity.date
        note = this@toEntity.note
        source = when (this@toEntity.source) {
            ExpenseSource.SMS -> "SMS"
            ExpenseSource.MANUAL -> "MANUAL"

        }
        rawSmsBody = this@toEntity.rawSmsBody
        merchantName = this@toEntity.merchantName
        transactionType = this@toEntity.transactionType.name
    }
}
