
package com.bluemix.cashio.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.R
import com.bluemix.cashio.ui.theme.CashioSemantic
import kotlin.math.abs

/**
 * A compact, tappable card showing top-level Income + Expense metrics.
 *
 * Notes:
 * - "Delta" values should be positive/negative based on comparison vs previous period.
 * - The row tint stays consistent (Income row uses green, Expense row uses red),
 *   while delta color communicates direction (good/bad) based on context.
 */
@Composable
fun FinancialSummaryCard(
    totalIncome: Double,
    totalExpenses: Double,
    incomeDelta: Double,
    expenseDelta: Double,
    currencySymbol: String = "₹",
    showChevron: Boolean = true,
    onIncomeClick: () -> Unit = {},
    onExpenseClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    CashioCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            MetricRow(
                leadingIcon = ICON_INCOME,
                label = LABEL_INCOME,
                amount = totalIncome,
                delta = incomeDelta,
                currencySymbol = currencySymbol,
                isIncomeRow = true,
                showChevron = showChevron,
                onClick = onIncomeClick
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            MetricRow(
                leadingIcon = ICON_EXPENSE,
                label = LABEL_EXPENSE,
                amount = totalExpenses,
                delta = expenseDelta,
                currencySymbol = currencySymbol,
                isIncomeRow = false,
                showChevron = showChevron,
                onClick = onExpenseClick
            )
        }
    }
}

@Composable
private fun MetricRow(
    leadingIcon: CashioIcon,
    label: String,
    amount: Double,
    delta: Double,
    currencySymbol: String,
    isIncomeRow: Boolean,
    showChevron: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "MetricRowScale"
    )

    val rowTint = if (isIncomeRow) CashioSemantic.IncomeGreen else CashioSemantic.ExpenseRed

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Icon + label + delta
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(rowTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                CashioIcon(
                    icon = leadingIcon,
                    tint = rowTint,
                    modifier = Modifier.size(24.dp)
                )

            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                AnimatedContent(
                    targetState = delta,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()).togetherWith(
                            slideOutVertically { -it } + fadeOut()
                        )
                    },
                    label = "MetricRowDelta"
                ) { target ->
                    val deltaColor = deltaColor(
                        delta = target,
                        isIncomeRow = isIncomeRow,
                        neutral = MaterialTheme.colorScheme.onSurfaceVariant,
                        income = CashioSemantic.IncomeGreen,
                        expense = CashioSemantic.ExpenseRed
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (target != 0.0) {
                            Icon(
                                imageVector = if (target > 0) Icons.Default.NorthEast else Icons.Default.SouthWest,
                                contentDescription = null,
                                tint = deltaColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = deltaText(target, currencySymbol),
                            style = MaterialTheme.typography.bodySmall,
                            color = deltaColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Right side: amount + optional chevron
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = amount,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()).togetherWith(
                        slideOutVertically { -it } + fadeOut()
                    )
                },
                label = "MetricRowAmount"
            ) { target ->
                Text(
                    text = "$currencySymbol${formatCompactAmount(target)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (showChevron) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Delta text. You can replace "last month" with a passed-in label later (e.g., "vs last week").
 */
private fun deltaText(delta: Double, currencySymbol: String): String {
    val amount = "$currencySymbol${formatCompactAmount(abs(delta))}"
    return when {
        delta > 0 -> "$amount more than last month"
        delta < 0 -> "$amount less than last month"
        else -> "Same as last month"
    }
}

/**
 * Context-aware delta coloring:
 * - For Income: positive is good (green), negative is bad (red).
 * - For Expense: positive is bad (red), negative is good (green).
 */
private fun deltaColor(
    delta: Double,
    isIncomeRow: Boolean,
    neutral: Color,
    income: Color,
    expense: Color
): Color = when {
    delta > 0 -> if (isIncomeRow) income else expense
    delta < 0 -> if (isIncomeRow) expense else income
    else -> neutral
}

private fun formatCompactAmount(value: Double): String {
    val v = abs(value)
    return when {
        v >= 1_000_000 -> {
            val x = v / 1_000_000.0
            if (x < 10) String.format("%.1fM", x) else String.format("%.0fM", x)
        }
        v >= 1_000 -> {
            val x = v / 1_000.0
            if (x < 10) String.format("%.1fK", x) else String.format("%.0fK", x)
        }
        else -> String.format("%.0f", v)
    }
}

private const val LABEL_INCOME = "Income"
private const val LABEL_EXPENSE = "Expense"
private val ICON_INCOME = CashioIcon.Drawable(R.drawable.uptrend)
private val ICON_EXPENSE = CashioIcon.Drawable(R.drawable.downtrend)
