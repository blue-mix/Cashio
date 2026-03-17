package com.bluemix.cashio.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluemix.cashio.R
import com.bluemix.cashio.core.format.CashioFormat
import com.bluemix.cashio.domain.model.Currency
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.ui.defaults.CashioCard
import com.bluemix.cashio.ui.defaults.CashioCardDefaults
import com.bluemix.cashio.ui.defaults.CashioSpacing
import com.bluemix.cashio.ui.theme.CashioSemantic
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private object TransactionItemDefaults {
    val IconSizeCompact = 40.dp
    val IconSizeDefault = 48.dp
    val EmojiSizeCompact = 18.sp
    val EmojiSizeDefault = 22.sp
    val PaddingCompact = 8.dp
    val PaddingDefault = 12.dp
}

@Composable
fun TransactionListItem(
    title: String,
    amountPaise: Long,
    type: TransactionType,
    dateTime: LocalDateTime,
    categoryIcon: String = "📦",
    categoryColor: Color = Color(0xFFE0E0E0),
    currency: Currency = Currency.INR,
    showCategoryIcon: Boolean = true,
    showChevron: Boolean = true,
    showDate: Boolean = true,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Derived display state
    val displayState = remember(amountPaise, type, dateTime, currency, compact) {
        TransactionDisplayState.from(
            amountPaise = amountPaise,
            type = type,
            dateTime = dateTime,
            currency = currency,
            compact = compact
        )
    }

    // Accessibility
    val semanticDescription = remember(title, displayState.signedAmountText, displayState.dateText) {
        "$title, ${displayState.signedAmountText}, ${displayState.dateText}"
    }

    CashioCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = semanticDescription },
        onClick = onClick,
        padding = PaddingValues(
            if (compact) TransactionItemDefaults.PaddingCompact
            else TransactionItemDefaults.PaddingDefault
        ),
        containerColor = if (compact)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        else
            MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showCategoryIcon) {
                CategoryIconChip(
                    icon = categoryIcon,
                    color = categoryColor,
                    size = if (compact) TransactionItemDefaults.IconSizeCompact
                    else TransactionItemDefaults.IconSizeDefault,
                    fontSize = if (compact) TransactionItemDefaults.EmojiSizeCompact
                    else TransactionItemDefaults.EmojiSizeDefault
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)
            ) {
                Text(
                    text = title,
                    style = if (compact)
                        MaterialTheme.typography.titleSmall
                    else
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                if (showDate) {
                    Text(
                        text = displayState.dateText,
                        style = if (compact)
                            MaterialTheme.typography.labelSmall
                        else
                            MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Text(
                text = displayState.signedAmountText,
                style = if (compact)
                    MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                else
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = displayState.amountColor
            )

            if (showChevron && !compact) {
                Icon(
                    painter = painterResource(id = R.drawable.chevron),
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@androidx.compose.runtime.Immutable
private data class TransactionDisplayState(
    val signedAmountText: String,
    val dateText: String,
    val amountColor: Color
) {
    companion object {
        fun from(
            amountPaise: Long,
            type: TransactionType,
            dateTime: LocalDateTime,
            currency: Currency,
            compact: Boolean
        ): TransactionDisplayState {
            val formattedAmount = CashioFormat.money(amountPaise, currency)
            val signedAmount = if (type == TransactionType.EXPENSE)
                "-$formattedAmount"
            else
                "+$formattedAmount"

            val pattern = if (compact) "dd MMM" else "dd MMM • hh:mm a"
            val dateStr = dateTime.format(
                DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
            )

            val color = when (type) {
                TransactionType.EXPENSE -> CashioSemantic.ExpenseRed
                TransactionType.INCOME -> CashioSemantic.IncomeGreen
            }

            return TransactionDisplayState(
                signedAmountText = signedAmount,
                dateText = dateStr,
                amountColor = color
            )
        }
    }
}

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
            fontSize = fontSize
        )
    }
}