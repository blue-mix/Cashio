
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
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.R
import com.bluemix.cashio.core.format.CashioFormat
import com.bluemix.cashio.domain.model.Currency
import com.bluemix.cashio.ui.defaults.CashioCard
import com.bluemix.cashio.ui.defaults.CashioIcon
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioSpacing
import com.bluemix.cashio.ui.theme.CashioSemantic
import kotlin.math.abs

// Animation constants
private object FinancialSummaryAnimations {
    const val PressScale = 0.985f
    const val NormalScale = 1f
    val SpringSpec = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy)
}

@Composable
fun FinancialSummaryCard(
    totalIncomePaise: Long,
    totalExpensesPaise: Long,
    incomeDeltaPaise: Long,
    expenseDeltaPaise: Long,
    currency: Currency = Currency.INR,
    comparisonLabel: String = "last month",
    showChevron: Boolean = true,
    onIncomeClick: () -> Unit = {},
    onExpenseClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    CashioCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xs)) {
            MetricRow(
                leadingIcon = CashioIcon.Drawable(R.drawable.uptrend),
                label = "Income",
                amountPaise = totalIncomePaise,
                deltaPaise = incomeDeltaPaise,
                currency = currency,
                comparisonLabel = comparisonLabel,
                isIncomeRow = true,
                showChevron = showChevron,
                onClick = onIncomeClick
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = CashioSpacing.xs),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            MetricRow(
                leadingIcon = CashioIcon.Drawable(R.drawable.downtrend),
                label = "Expenses",
                amountPaise = totalExpensesPaise,
                deltaPaise = expenseDeltaPaise,
                currency = currency,
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
    amountPaise: Long,
    deltaPaise: Long,
    currency: Currency,
    comparisonLabel: String,
    isIncomeRow: Boolean,
    showChevron: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    // Scale animation
    val scale by animateFloatAsState(
        targetValue = if (pressed) FinancialSummaryAnimations.PressScale
        else FinancialSummaryAnimations.NormalScale,
        animationSpec = FinancialSummaryAnimations.SpringSpec,
        label = "MetricRowScale"
    )

    // Consolidate all display state into a single remembered object
    val displayState = remember(
        amountPaise,
        deltaPaise,
        currency,
        comparisonLabel,
        isIncomeRow
    ) {
        MetricDisplayState.from(
            amountPaise = amountPaise,
            deltaPaise = deltaPaise,
            currency = currency,
            comparisonLabel = comparisonLabel,
            isIncomeRow = isIncomeRow
        )
    }

    // Accessibility
    val semanticDesc = remember(label, displayState.amountText, displayState.deltaText) {
        "$label: ${displayState.amountText}, ${displayState.deltaText}"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(CashioRadius.small))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(vertical = CashioSpacing.xs)
            .semantics { contentDescription = semanticDesc },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Bubble
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(displayState.rowTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                CashioIcon(
                    icon = leadingIcon,
                    tint = displayState.rowTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Delta with animation
                AnimatedContent(
                    targetState = displayState.deltaText,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn())
                            .togetherWith(slideOutVertically { -it } + fadeOut())
                    },
                    label = "MetricRowDelta"
                ) { targetText ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.xxs)
                    ) {
                        displayState.deltaIcon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = displayState.deltaColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = targetText,
                            style = MaterialTheme.typography.bodySmall,
                            color = displayState.deltaColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(CashioSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Amount with animation
            AnimatedContent(
                targetState = displayState.amountText,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn())
                        .togetherWith(slideOutVertically { -it } + fadeOut())
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
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Immutable state holder - all display logic in one place
@androidx.compose.runtime.Immutable
private data class MetricDisplayState(
    val amountText: String,
    val deltaText: String,
    val deltaColor: Color,
    val deltaIcon: ImageVector?,
    val rowTint: Color
) {
    companion object {
        fun from(
            amountPaise: Long,
            deltaPaise: Long,
            currency: Currency,
            comparisonLabel: String,
            isIncomeRow: Boolean
        ): MetricDisplayState {
            val amountText = CashioFormat.compactAmount(amountPaise, currency)

            val deltaText = when {
                deltaPaise == 0L -> "Same as $comparisonLabel"
                else -> {
                    val amount = CashioFormat.compactAmount(abs(deltaPaise), currency)
                    val direction = if (deltaPaise > 0L) "more" else "less"
                    "$amount $direction than $comparisonLabel"
                }
            }

            // Calculate delta color based on context
            val deltaColor = when {
                deltaPaise == 0L -> Color.Unspecified // Will use onSurfaceVariant
                deltaPaise > 0L -> if (isIncomeRow)
                    CashioSemantic.IncomeGreen
                else
                    CashioSemantic.ExpenseRed
                else -> if (isIncomeRow)
                    CashioSemantic.ExpenseRed
                else
                    CashioSemantic.IncomeGreen
            }

            val deltaIcon = when {
                deltaPaise == 0L -> null
                deltaPaise > 0L -> Icons.Default.NorthEast
                else -> Icons.Default.SouthWest
            }

            val rowTint = if (isIncomeRow)
                CashioSemantic.IncomeGreen
            else
                CashioSemantic.ExpenseRed

            return MetricDisplayState(
                amountText = amountText,
                deltaText = deltaText,
                deltaColor = deltaColor,
                deltaIcon = deltaIcon,
                rowTint = rowTint
            )
        }
    }
}