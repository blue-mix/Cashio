package com.bluemix.cashio.presentation.transaction.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.ui.components.defaults.CashioCard

@Composable
fun TransactionDetailsInfoCard(
    title: String,
    rows: List<Pair<String, String>>
) {
    CashioCard(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        padding = PaddingValues(16.dp),
        cornerRadius = 16.dp,
        showBorder = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            rows.forEach { (k, v) ->
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(k, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(v, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
