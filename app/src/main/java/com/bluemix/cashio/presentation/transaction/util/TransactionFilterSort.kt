package com.bluemix.cashio.presentation.transaction.util

import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.presentation.transaction.TransactionSort
import com.bluemix.cashio.presentation.transaction.TransactionTypeFilter

fun filterAndSortTransactions(
    list: List<Expense>,
    query: String,
    typeFilter: TransactionTypeFilter,
    sort: TransactionSort
): List<Expense> {
    val q = query.trim()

    val filtered = list.asSequence()
        .filter { tx ->
            when (typeFilter) {
                TransactionTypeFilter.ALL -> true
                TransactionTypeFilter.EXPENSE -> tx.transactionType == TransactionType.EXPENSE
                TransactionTypeFilter.INCOME -> tx.transactionType == TransactionType.INCOME
            }
        }
        .filter { tx ->
            if (q.isBlank()) true else {
                tx.title.contains(q, ignoreCase = true) ||
                        tx.category.name.contains(q, ignoreCase = true) ||
                        (tx.merchantName?.contains(q, ignoreCase = true) == true) ||
                        tx.amount.toString().contains(q)
            }
        }
        .toList()

    return when (sort) {
        TransactionSort.DATE_DESC -> filtered.sortedByDescending { it.date }
        TransactionSort.DATE_ASC -> filtered.sortedBy { it.date }
        TransactionSort.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
        TransactionSort.AMOUNT_ASC -> filtered.sortedBy { it.amount }
        TransactionSort.TITLE_ASC -> filtered.sortedBy { it.title }
        TransactionSort.TITLE_DESC -> filtered.sortedByDescending { it.title }
    }
}
