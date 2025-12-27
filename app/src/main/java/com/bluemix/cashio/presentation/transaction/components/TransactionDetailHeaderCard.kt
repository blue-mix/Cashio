package com.bluemix.cashio.presentation.transaction.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.ui.components.defaults.CashioCard
import com.bluemix.cashio.ui.theme.CashioSemantic.ExpenseRed
import com.bluemix.cashio.ui.theme.CashioSemantic.IncomeGreen

@Composable
fun TransactionDetailsHeaderCard(tx: Expense) {
    val color = if (tx.transactionType == TransactionType.EXPENSE) ExpenseRed else IncomeGreen

    CashioCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(16.dp),
        cornerRadius = 16.dp,
        showBorder = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = tx.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "₹${String.format("%,.2f", tx.amount)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = "${tx.category.icon}  ${tx.category.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
