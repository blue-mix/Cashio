package com.bluemix.cashio.ui.components.cards

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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.R
import com.bluemix.cashio.core.format.CashioFormat
import com.bluemix.cashio.ui.components.defaults.CashioCard
import com.bluemix.cashio.ui.components.defaults.CashioIcon
import com.bluemix.cashio.ui.components.defaults.CashioRadius
import com.bluemix.cashio.ui.components.defaults.CashioSpacing
import com.bluemix.cashio.ui.theme.CashioSemantic
import kotlin.math.abs

/**
 * A dashboard card that displays the high-level financial summary:
 * Total Income vs Total Expenses, along with comparative deltas.
 *
 * @param totalIncome Current period's total income.
 * @param totalExpenses Current period's total expenses.
 * @param incomeDelta Difference in income compared to the previous period.
 * @param expenseDelta Difference in expenses compared to the previous period.
 * @param comparisonLabel Label explaining the delta (e.g., "last month").
 * @param showChevron Whether to show navigation indicators.
 */
@Composable
fun FinancialSummaryCard(
    totalIncome: Double,
    totalExpenses: Double,
    incomeDelta: Double,
    expenseDelta: Double,
    currencySymbol: String = "₹",
    comparisonLabel: String = "last month",
    showChevron: Boolean = true,
    onIncomeClick: () -> Unit = {},
    onExpenseClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    CashioCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xs)) {
            MetricRow(
                leadingIcon = ICON_INCOME,
                label = LABEL_INCOME,
                amount = totalIncome,
                delta = incomeDelta,
                currencySymbol = currencySymbol,
                comparisonLabel = comparisonLabel,
                isIncomeRow = true,
                showChevron = showChevron,
                onClick = onIncomeClick
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = CashioSpacing.small),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            MetricRow(
                leadingIcon = ICON_EXPENSE,
                label = LABEL_EXPENSE,
                amount = totalExpenses,
                delta = expenseDelta,
                currencySymbol = currencySymbol,
                comparisonLabel = comparisonLabel,
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
    comparisonLabel: String,
    isIncomeRow: Boolean,
    showChevron: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    // Subtle press animation for better tactile feel
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "MetricRowScale"
    )

    val rowTint = if (isIncomeRow) CashioSemantic.IncomeGreen else CashioSemantic.ExpenseRed

    val amountText = remember(amount, currencySymbol) {
        "$currencySymbol${CashioFormat.compactAmount(amount)}"
    }
    val deltaText = remember(delta, currencySymbol, comparisonLabel) {
        buildDeltaText(delta, currencySymbol, comparisonLabel)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(CashioRadius.small))
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom ripple handled by parent if needed, or disable for cleaner look
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(vertical = CashioSpacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
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

            Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Animate text changes for delta
                AnimatedContent(
                    targetState = deltaText,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()).togetherWith(
                            slideOutVertically { -it } + fadeOut()
                        )
                    },
                    label = "MetricRowDelta"
                ) { targetText ->
                    val deltaColor = deltaColor(
                        delta = delta,
                        isIncomeRow = isIncomeRow,
                        neutral = MaterialTheme.colorScheme.onSurfaceVariant,
                        income = CashioSemantic.IncomeGreen,
                        expense = CashioSemantic.ExpenseRed
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.tiny)
                    ) {
                        if (delta != 0.0) {
                            Icon(
                                imageVector = if (delta > 0) Icons.Default.NorthEast else Icons.Default.SouthWest,
                                contentDescription = null,
                                tint = deltaColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = targetText,
                            style = MaterialTheme.typography.bodySmall,
                            color = deltaColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animate text changes for amount
            AnimatedContent(
                targetState = amountText,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()).togetherWith(
                        slideOutVertically { -it } + fadeOut()
                    )
                },
                label = "MetricRowAmount"
            ) { targetText ->
                Text(
                    text = targetText,
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
 * Builds a readable string describing the change (e.g., "$500 less than last month").
 */
private fun buildDeltaText(delta: Double, currencySymbol: String, comparisonLabel: String): String {
    if (delta == 0.0) return "Same as $comparisonLabel"
    val amount = "$currencySymbol${CashioFormat.compactAmount(abs(delta))}"
    return if (delta > 0) "$amount more than $comparisonLabel" else "$amount less than $comparisonLabel"
}

/**
 * Determines the color of the delta text based on context.
 * - Income increasing is good (Green).
 * - Expense increasing is bad (Red).
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

private const val LABEL_INCOME = "Income"
private const val LABEL_EXPENSE = "Expense"
private val ICON_INCOME = CashioIcon.Drawable(R.drawable.uptrend)
private val ICON_EXPENSE = CashioIcon.Drawable(R.drawable.downtrend)