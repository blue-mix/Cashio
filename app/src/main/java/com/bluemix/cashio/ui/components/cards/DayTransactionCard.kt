package com.bluemix.cashio.ui.components.cards

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.R
import com.bluemix.cashio.core.format.CashioFormat
import com.bluemix.cashio.core.format.CashioFormat.toDayName
import com.bluemix.cashio.core.format.CashioFormat.toFullDate
import com.bluemix.cashio.core.format.CashioFormat.toTimeLabel
import com.bluemix.cashio.core.format.CashioFormat.toTransactionCountLabel
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.ui.components.defaults.CashioCard
import com.bluemix.cashio.ui.theme.CashioRadius
import com.bluemix.cashio.ui.theme.CashioSemantic.ExpenseRed
import com.bluemix.cashio.ui.theme.CashioSemantic.IncomeGreen
import com.bluemix.cashio.ui.theme.CashioSpacing
import java.time.LocalDate
import kotlin.math.abs

/**
 * A collapsible card that groups all transactions for a specific date.
 *
 * Header displays:
 * - Date and Day name.
 * - Daily summary (Total Income, Total Expense).
 *
 * Expanded state displays:
 * - A list of individual transaction rows.
 *
 * @param date The specific date for this group.
 * @param transactions List of expenses/incomes occurring on this date.
 * @param onTransactionClick Callback when a specific transaction row is clicked.
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

    val dayName = remember(date) { date.toDayName() }
    val fullDate = remember(date) { date.toFullDate() }

    // Efficiently calculate totals only when the transaction list changes
    val summary by remember(transactions) {
        derivedStateOf { transactions.toDaySummary() }
    }

    val headerUi = remember(summary, currencySymbol) {
        summary.toHeaderUi(currencySymbol)
    }

    CashioCard(
        modifier = modifier.fillMaxWidth(),
        onClick = null // Click handled by the header row specifically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.compact)) {

            DayHeaderRow(
                dayName = dayName,
                fullDate = fullDate,
                transactionCount = transactions.size,
                headerUi = headerUi,
                isExpanded = isExpanded,
                onToggleExpand = { isExpanded = !isExpanded }
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

/**
 * The always-visible top section of the card.
 * Clicking this toggles the expansion state.
 */
@Composable
private fun DayHeaderRow(
    dayName: String,
    fullDate: String,
    transactionCount: Int,
    headerUi: DayHeaderUi,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CashioRadius.mediumSmall))
            .clickable(onClick = onToggleExpand)
            .padding(horizontal = CashioSpacing.xs, vertical = CashioSpacing.xxs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side: Date Info
        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)) {
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

        // Right Side: Financial Summary
        Row(
            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)
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

/**
 * The content shown when the card is expanded.
 * Includes a divider and a constrained list of transactions.
 */
@Composable
private fun DayExpandedContent(
    transactions: List<Expense>,
    currencySymbol: String,
    onTransactionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(top = CashioSpacing.small),
        verticalArrangement = Arrangement.spacedBy(CashioSpacing.small)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Spacer(modifier = Modifier.height(CashioSpacing.xs))

        // Constrain height to prevent massive cards on heavy transaction days
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(CashioSpacing.small)
        ) {
            items(items = transactions, key = { it.id }) { tx ->
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
 * A dense row representing a single transaction inside the expanded view.
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

    val amountText = remember(transaction.amount, currencySymbol) {
        "$sign${CashioFormat.money(abs(transaction.amount), currencySymbol)}"
    }

    val timeText = remember(transaction.date) {
        transaction.date.toTimeLabel()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CashioRadius.small))
            .clickable(onClick = onClick)
            .padding(vertical = CashioSpacing.tiny),
        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIconBubble(
            icon = transaction.category.icon,
            tintBackground = transaction.category.color
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun CategoryIconBubble(icon: String, tintBackground: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(tintBackground.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, style = MaterialTheme.typography.titleMedium)
    }
}

/* -------------------------------------------------------------------------- */
/* Data Helpers                                                               */
/* -------------------------------------------------------------------------- */

/**
 * Aggregates a list of expenses into a summary of Income vs Expense.
 */
private fun List<Expense>.toDaySummary(): DaySummary {
    val (inc, exp) = partition { it.transactionType == TransactionType.INCOME }
    val incomeSum = inc.sumOf { it.amount }
    val expenseSum = exp.sumOf { it.amount }
    return DaySummary(incomeSum, expenseSum, incomeSum - expenseSum)
}

/**
 * Formats the raw summary data into displayable strings for the UI header.
 * Only non-zero values are converted to text.
 */
private fun DaySummary.toHeaderUi(currencySymbol: String): DayHeaderUi {
    val incText = income.takeIf { it > 0 }?.let { "+${CashioFormat.money(it, currencySymbol)}" }
    val expText = expense.takeIf { it > 0 }?.let { "-${CashioFormat.money(it, currencySymbol)}" }

    // Only show "Net" if there is activity
    val netValueText = if (income > 0 || expense > 0) {
        val sign = if (net >= 0) "+" else "-"
        "Net: $sign${CashioFormat.money(abs(net), currencySymbol)}"
    } else null

    return DayHeaderUi(incText, expText, netValueText, net >= 0)
}

private data class DaySummary(val income: Double, val expense: Double, val net: Double)

private data class DayHeaderUi(
    val incomeText: String?,
    val expenseText: String?,
    val netText: String?,
    val isNetPositive: Boolean
)