package com.bluemix.cashio.presentation.transaction.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.presentation.transaction.TransactionTypeFilter

@Composable
fun TransactionTypeFilterRow(
    filter: TransactionTypeFilter,
    onChange: (TransactionTypeFilter) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FilterChip(
            selected = filter == TransactionTypeFilter.ALL,
            onClick = { onChange(TransactionTypeFilter.ALL) },
            label = { Text("All") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                selectedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        FilterChip(
            selected = filter == TransactionTypeFilter.EXPENSE,
            onClick = { onChange(TransactionTypeFilter.EXPENSE) },
            label = { Text("Expense") }
        )
        FilterChip(
            selected = filter == TransactionTypeFilter.INCOME,
            onClick = { onChange(TransactionTypeFilter.INCOME) },
            label = { Text("Income") }
        )
    }
}
