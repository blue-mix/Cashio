package com.bluemix.cashio.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluemix.cashio.R
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.ui.components.defaults.CashioCard
import com.bluemix.cashio.ui.theme.CashioSemantic
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

/**
 * A reusable transaction row card used across screens (history, dashboard, lists).
 *
 * Design rules:
 * - Uses [CashioCard] as the ONE consistent container.
 * - Optional compact styling is applied inside the card (subtle surfaceVariant tint).
 * - Amount color is semantic: income = green, expense = red.
 */
@Composable
fun TransactionListItem(
    title: String,
    amount: Double,
    type: TransactionType,
    dateTime: LocalDateTime,
    categoryIcon: String = "📦",
    categoryColor: Color = Color(0xFFE0E0E0),
    showCategoryIcon: Boolean = true,
    showChevron: Boolean = true,
    showDate: Boolean = true,
    compact: Boolean = false,
    currencySymbol: String = "₹",
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // UI sizing tokens
    val iconSize = if (compact) 40.dp else 48.dp
    val emojiFontSize = if (compact) 20.sp else 24.sp

    val amountColor = when (type) {
        TransactionType.EXPENSE -> CashioSemantic.ExpenseRed
        TransactionType.INCOME -> CashioSemantic.IncomeGreen
    }

    val normalizedAmount = amount.absoluteValue

    // Keep formatting stable and cheap
    val formattedAmount = remember(normalizedAmount, currencySymbol) {
        "%s%,.2f".format(Locale.ENGLISH, currencySymbol, normalizedAmount)
    }

    val signedAmountText = remember(type, formattedAmount) {
        when (type) {
            TransactionType.EXPENSE -> "-$formattedAmount"
            TransactionType.INCOME -> "+$formattedAmount"
        }
    }

    val now = remember { LocalDateTime.now() }
    val dateText = remember(dateTime, now) { formatTransactionDate(dateTime, now) }


    CashioCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = if (compact)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        else
            MaterialTheme.colorScheme.surface
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showCategoryIcon) {
                CategoryIconChip(
                    icon = categoryIcon,
                    color = categoryColor,
                    size = iconSize,
                    fontSize = emojiFontSize
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = if (compact) MaterialTheme.typography.titleSmall
                    else MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1

                )

                if (showDate) {
                    Text(
                        text = dateText,
                        style = if (compact) MaterialTheme.typography.labelSmall
                        else MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Text(
                text = signedAmountText,
                style = if (compact) {
                    MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                } else {
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                },
                color = amountColor
            )

            if (showChevron) {
                Icon(
                    painter = painterResource(R.drawable.chevron),
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Icon chip used in transaction rows.
 * If you move from emoji to vector icons later, update only this function.
 */
@Composable
private fun CategoryIconChip(
    icon: String,
    color: Color,
    size: Dp,
    fontSize: TextUnit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = fontSize)
        )
    }
}

/**
 * Human readable timestamps:
 * - Today, 13:20
 * - Yesterday, 09:12
 * - 12 Dec, 18:40 (same year)
 * - 12 Dec 2024 (different year)
 */
private fun formatTransactionDate(
    dateTime: LocalDateTime,
    now: LocalDateTime = LocalDateTime.now()
): String {
    val today = now.toLocalDate()
    val yesterday = today.minusDays(1)
    val txDate = dateTime.toLocalDate()

    return when {
        txDate == today ->
            "Today, ${dateTime.format(TIME_FMT)}"

        txDate == yesterday ->
            "Yesterday, ${dateTime.format(TIME_FMT)}"

        txDate.year == today.year ->
            dateTime.format(DATE_TIME_SAME_YEAR_FMT)

        else ->
            dateTime.format(DATE_FULL_YEAR_FMT)
    }
}

private val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
private val DATE_TIME_SAME_YEAR_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.ENGLISH)
private val DATE_FULL_YEAR_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
