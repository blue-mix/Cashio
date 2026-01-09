package com.bluemix.cashio.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluemix.cashio.R
import com.bluemix.cashio.core.format.CashioFormat
import com.bluemix.cashio.core.format.CashioFormat.toDateLabel
import com.bluemix.cashio.core.format.CashioFormat.toTimeLabel
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.ui.components.defaults.CashioCard
import com.bluemix.cashio.ui.components.defaults.CashioSpacing
import com.bluemix.cashio.ui.theme.CashioSemantic
import java.time.LocalDateTime
import kotlin.math.absoluteValue

/**
 * A standard list item representing a single financial transaction.
 *
 * This component automatically handles:
 * - Formatting the amount with the currency symbol.
 * - Applying Semantic colors (Red for Expense, Green for Income).
 * - Formatting the date and time strings.
 * - Adjusting layout density via the [compact] parameter.
 *
 * @param title The primary text (Merchant name or Description).
 * @param amount The raw transaction amount.
 * @param type Determines if this is [TransactionType.INCOME] or [TransactionType.EXPENSE].
 * @param dateTime The timestamp of the transaction.
 * @param categoryIcon The emoji or character representing the category.
 * @param categoryColor The background tint color for the category icon.
 * @param showCategoryIcon Whether to display the leading circular icon.
 * @param showChevron Whether to display the trailing arrow icon indicating navigation.
 * @param showDate Whether to display the secondary text line with date/time.
 * @param compact If true, reduces padding and font sizes for dense lists (e.g., Dashboard).
 * @param currencySymbol The symbol to prefix the amount with (default "₹").
 * @param onClick Callback when the item is tapped.
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
    val iconSize = if (compact) 40.dp else 48.dp
    val emojiFontSize = if (compact) 20.sp else 24.sp

    val amountColor = when (type) {
        TransactionType.EXPENSE -> CashioSemantic.ExpenseRed
        TransactionType.INCOME -> CashioSemantic.IncomeGreen
    }

    val formattedAmount = remember(amount, currencySymbol) {
        CashioFormat.money(amount.absoluteValue, currencySymbol)
    }

    val signedAmountText = remember(type, formattedAmount) {
        if (type == TransactionType.EXPENSE) "-$formattedAmount" else "+$formattedAmount"
    }

    val dateText = remember(dateTime) {
        val datePart = dateTime.toDateLabel()
        val timePart = dateTime.toTimeLabel()
        "$datePart • $timePart"
    }

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
            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
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
                verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)
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
 * A private helper composable to render the circular category icon.
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