
package com.bluemix.cashio.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.ui.theme.CashioSemantic.ExpenseRed
import com.bluemix.cashio.ui.theme.CashioSemantic.IncomeGreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import com.bluemix.cashio.R

/**
 * A single day group card in History:
 * - Header shows date + summary (income/expense/net)
 * - Expand reveals individual transactions for that day
 */
@Composable
fun DayTransactionCard(
    date: LocalDate,
    transactions: List<Expense>,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹"
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Date formatters (stable, created once)
    val dayName = remember(date) { date.format(DAY_NAME_FORMATTER) }
    val fullDate = remember(date) { date.format(FULL_DATE_FORMATTER) }

    // Summary is derived from the list; recomputes only when list changes.
    val summary by remember(transactions) {
        derivedStateOf { transactions.toDaySummary() }
    }

    val headerUi = remember(summary, currencySymbol) {
        summary.toHeaderUi(currencySymbol)
    }

    CashioCard(
        modifier = modifier.fillMaxWidth(),
        onClick = { isExpanded = !isExpanded } // click anywhere on card toggles expand
    ) {
        Column(
            modifier = Modifier.animateContentSize()
        ) {
            DayHeaderRow(
                dayName = dayName,
                fullDate = fullDate,
                transactionCount = transactions.size,
                headerUi = headerUi,
                isExpanded = isExpanded
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                DayExpandedContent(
                    transactions = transactions,
                    currencySymbol = currencySymbol,
                    onTransactionClick = onTransactionClick
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Header                                                                      */
/* -------------------------------------------------------------------------- */

@Composable
private fun DayHeaderRow(
    dayName: String,
    fullDate: String,
    transactionCount: Int,
    headerUi: DayHeaderUi,
    isExpanded: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: date labels
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = dayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = fullDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = transactionCount.toTransactionCountLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Right: totals + expand icon
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                headerUi.incomeText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = IncomeGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                headerUi.expenseText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = ExpenseRed,
                        fontWeight = FontWeight.Bold
                    )
                }
                headerUi.netText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (headerUi.isNetPositive) IncomeGreen else ExpenseRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                painter = painterResource(if (isExpanded) R.drawable.chevronup else R.drawable.chevrondown),
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Expanded content                                                            */
/* -------------------------------------------------------------------------- */

@Composable
private fun DayExpandedContent(
    transactions: List<Expense>,
    currencySymbol: String,
    onTransactionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Use LazyColumn only if list can be long; otherwise Column is fine.
        // Here we keep it lightweight with Column + items for stable keys.
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            transactions.forEach { tx ->
                DayTransactionRow(
                    transaction = tx,
                    currencySymbol = currencySymbol,
                    onClick = { onTransactionClick(tx.id) }
                )
            }
        }
    }
}

/**
 * A single transaction row inside an expanded day.
 * Click goes to transaction details.
 */
@Composable
fun DayTransactionRow(
    transaction: Expense,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹"
) {
    val isExpense = transaction.transactionType == TransactionType.EXPENSE
    val amountColor = if (isExpense) ExpenseRed else IncomeGreen
    val sign = if (isExpense) "-" else "+"

    val amountText = remember(transaction.amount, transaction.transactionType, currencySymbol) {
        "$sign$currencySymbol${format0(transaction.amount)}"
    }

    val timeText = remember(transaction.date) {
        transaction.date.toLocalTime().format(TIME_FORMATTER)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIconBubble(
            icon = transaction.category.icon,
            tintBackground = transaction.category.color
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Text(
            text = amountText,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = amountColor
        )
    }
}

@Composable
private fun CategoryIconBubble(
    icon: String,
    tintBackground: androidx.compose.ui.graphics.Color,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(tintBackground.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/* -------------------------------------------------------------------------- */
/* UI models + helpers                                                         */
/* -------------------------------------------------------------------------- */

private data class DaySummary(
    val income: Double,
    val expense: Double,
    val net: Double
)

private data class DayHeaderUi(
    val incomeText: String?,
    val expenseText: String?,
    val netText: String?,
    val isNetPositive: Boolean
)

private fun List<Expense>.toDaySummary(): DaySummary {
    var income = 0.0
    var expense = 0.0

    for (t in this) {
        when (t.transactionType) {
            TransactionType.INCOME -> income += t.amount
            TransactionType.EXPENSE -> expense += t.amount
        }
    }

    return DaySummary(
        income = income,
        expense = expense,
        net = income - expense
    )
}

private fun DaySummary.toHeaderUi(currencySymbol: String): DayHeaderUi {
    val incomeText = income.takeIf { it > 0 }?.let { "+$currencySymbol${format0(it)}" }
    val expenseText = expense.takeIf { it > 0 }?.let { "-$currencySymbol${format0(it)}" }

    val netText = if (income > 0 || expense > 0) {
        val sign = if (net >= 0) "+" else "-"
        "Net: $sign$currencySymbol${format0(abs(net))}"
    } else null

    return DayHeaderUi(
        incomeText = incomeText,
        expenseText = expenseText,
        netText = netText,
        isNetPositive = net >= 0
    )
}

private fun Int.toTransactionCountLabel(): String =
    "$this transaction${if (this == 1) "" else "s"}"

private fun format0(value: Double): String =
    String.format(Locale.ENGLISH, "%.0f", value)

/* -------------------------------------------------------------------------- */
/* Formatters (keep stable & centralized)                                      */
/* -------------------------------------------------------------------------- */

private val DAY_NAME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)

private val FULL_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

private val TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
