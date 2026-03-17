package com.bluemix.cashio.ui.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.R
import com.bluemix.cashio.core.format.CashioFormat
import com.bluemix.cashio.domain.model.Currency
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.ui.defaults.CashioCard
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioSpacing
import com.bluemix.cashio.ui.theme.CashioSemantic
import com.bluemix.cashio.ui.theme.toComposeColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

// Animation constants
private object DayCardAnimations {
    const val FadeInDuration = 140
    const val ExpandDuration = 200
    const val FadeOutDuration = 120
    const val ShrinkDuration = 180
}

@Composable
fun DayTransactionCard(
    date: LocalDate,
    transactions: List<Expense>,
    currency: Currency = Currency.INR,
    onTransactionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable(date.toString()) { mutableStateOf(false) }

    // Optimized calculations - memoized by dependencies
    val dateDisplay = remember(date) {
        DateDisplay.from(date)
    }

    val summary = remember(transactions, currency) {
        DaySummaryState.from(transactions, currency)
    }

    // Accessibility
    val semanticDesc = remember(dateDisplay, summary, transactions.size) {
        "${dateDisplay.fullDate}, ${transactions.size} transactions, ${summary.semanticDescription}"
    }

    CashioCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = semanticDesc },
        onClick = null
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.sm)) {
            DayHeaderRow(
                dateDisplay = dateDisplay,
                transactionCount = transactions.size,
                summary = summary,
                isExpanded = isExpanded,
                onToggleExpand = { isExpanded = !isExpanded }
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(DayCardAnimations.FadeInDuration)) +
                        expandVertically(animationSpec = tween(DayCardAnimations.ExpandDuration)),
                exit = fadeOut(animationSpec = tween(DayCardAnimations.FadeOutDuration)) +
                        shrinkVertically(animationSpec = tween(DayCardAnimations.ShrinkDuration))
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = CashioSpacing.xs),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(CashioSpacing.xs))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(CashioSpacing.xs)
                    ) {
                        transactions.forEach { tx ->
                            key(tx.id) { // Optimize recompositions
                                TransactionListItem(
                                    title = tx.title,
                                    amountPaise = tx.amountPaise,
                                    type = tx.transactionType,
                                    dateTime = tx.date,
                                    categoryIcon = tx.category.icon,
                                    categoryColor = tx.category.colorHex.toComposeColor(),
                                    currency = currency,
                                    compact = true,
                                    onClick = { onTransactionClick(tx.id) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(CashioSpacing.xs))
                }
            }
        }
    }
}

@Composable
private fun DayHeaderRow(
    dateDisplay: DateDisplay,
    transactionCount: Int,
    summary: DaySummaryState,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val countLabel = remember(transactionCount) {
        if (transactionCount == 1) "1 transaction" else "$transactionCount transactions"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CashioRadius.small))
            .clickable(onClick = onToggleExpand)
            .padding(horizontal = CashioSpacing.xs, vertical = CashioSpacing.xxs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)) {
            Text(
                text = dateDisplay.dayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = dateDisplay.fullDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = countLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)
            ) {
                summary.incomeText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleSmall,
                        color = CashioSemantic.IncomeGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                summary.expenseText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleSmall,
                        color = CashioSemantic.ExpenseRed,
                        fontWeight = FontWeight.Bold
                    )
                }
                summary.netText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (summary.isNetPositive)
                            CashioSemantic.IncomeGreen
                        else
                            CashioSemantic.ExpenseRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Icon(
                painter = painterResource(
                    id = if (isExpanded) R.drawable.chevronup else R.drawable.chevrondown
                ),
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/* --- Immutable State Holders --- */

@androidx.compose.runtime.Immutable
private data class DateDisplay(
    val dayName: String,
    val fullDate: String
) {
    companion object {
        fun from(date: LocalDate): DateDisplay {
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val fullDate = date.format(
                DateTimeFormatter.ofPattern("dd MMMM, yyyy", Locale.getDefault())
            )
            return DateDisplay(dayName, fullDate)
        }
    }
}

@androidx.compose.runtime.Immutable
private data class DaySummaryState(
    val incomeText: String?,
    val expenseText: String?,
    val netText: String?,
    val isNetPositive: Boolean,
    val semanticDescription: String
) {
    companion object {
        fun from(transactions: List<Expense>, currency: Currency): DaySummaryState {
            var income = 0L
            var expense = 0L

            for (tx in transactions) {
                when (tx.transactionType) {
                    TransactionType.INCOME -> income += tx.amountPaise
                    TransactionType.EXPENSE -> expense += tx.amountPaise
                }
            }

            val net = income - expense
            val isPositive = net >= 0L

            val incText = income.takeIf { it > 0L }?.let {
                "+ ${CashioFormat.money(it, currency)}"
            }
            val expText = expense.takeIf { it > 0L }?.let {
                "- ${CashioFormat.money(it, currency)}"
            }
            val netText = if (income > 0L || expense > 0L) {
                val sign = if (net >= 0L) "+" else "-"
                "Net: $sign${CashioFormat.money(abs(net), currency)}"
            } else null

            val semantic = buildString {
                incText?.let { append("Income $it, ") }
                expText?.let { append("Expense $it, ") }
                netText?.let { append(it) }
            }

            return DaySummaryState(
                incomeText = incText,
                expenseText = expText,
                netText = netText,
                isNetPositive = isPositive,
                semanticDescription = semantic
            )
        }
    }
}