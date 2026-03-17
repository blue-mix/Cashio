
package com.bluemix.cashio.ui.components.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluemix.cashio.core.format.CashioFormat
import com.bluemix.cashio.domain.model.Currency
import com.bluemix.cashio.ui.defaults.CashioCard
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioSpacing
import com.bluemix.cashio.ui.theme.CashioSemantic
import kotlin.math.PI
import kotlin.math.roundToInt

// Constants for ring chart
private object RingChartDefaults {
    val StrokeWidth = 14.dp
    val Size = 200.dp
    val ContainerSize = 220.dp
    const val GapDegrees = 6f
    const val MinRatioForSegment = 0.02f
    const val MaxRatioForSegment = 0.98f
}

@Composable
fun SpendingOverviewCard(
    totalAmountPaise: Long,
    periodLabel: String,
    expenseRatio: Float,
    topCategory: String,
    topCategoryAmountPaise: Long,
    topCategoryIcon: String = "💡",
    topCategoryColor: Color = MaterialTheme.colorScheme.primary,
    onTopCategoryClick: () -> Unit = {},
    currency: Currency = Currency.INR,
    modifier: Modifier = Modifier
) {
    // Derived state - memoized only when dependencies change
    val displayState = remember(
        totalAmountPaise,
        expenseRatio,
        topCategoryAmountPaise,
        currency
    ) {
        SpendingDisplayState(
            ratio = expenseRatio.coerceIn(0f, 1f),
            percentText = "${(expenseRatio * 100).roundToInt()}% of total",
            totalText = CashioFormat.money(totalAmountPaise, currency),
            topAmountText = CashioFormat.money(topCategoryAmountPaise, currency)
        )
    }

    CashioCard(modifier = modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Ring Chart Section
            RingChartSection(
                ratio = displayState.ratio,
                periodLabel = periodLabel,
                totalText = displayState.totalText
            )

            Spacer(modifier = Modifier.height(CashioSpacing.lg))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(CashioSpacing.md))

            // Top Spending Section
            TopSpendingSection(
                category = topCategory,
                percentText = displayState.percentText,
                amountText = displayState.topAmountText,
                icon = topCategoryIcon,
                color = topCategoryColor,
                onClick = onTopCategoryClick
            )
        }
    }
}

// Immutable state holder
@androidx.compose.runtime.Immutable
private data class SpendingDisplayState(
    val ratio: Float,
    val percentText: String,
    val totalText: String,
    val topAmountText: String
)

@Composable
private fun RingChartSection(
    ratio: Float,
    periodLabel: String,
    totalText: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(RingChartDefaults.ContainerSize)
            .padding(vertical = CashioSpacing.md)
    ) {
        val cs = MaterialTheme.colorScheme

        Canvas(modifier = Modifier.size(RingChartDefaults.Size)) {
            drawRingChart(
                ratio = ratio,
                expenseColor = CashioSemantic.ExpenseRed,
                remainingColor = cs.primary
            )
        }

        // Center Text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.labelMedium,
                color = cs.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(CashioSpacing.xxs))
            Text(
                text = totalText,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = cs.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Extracted ring chart drawing logic for reusability
private fun DrawScope.drawRingChart(
    ratio: Float,
    expenseColor: Color,
    remainingColor: Color
) {
    val strokeWidthPx = RingChartDefaults.StrokeWidth.toPx()
    val radius = (size.minDimension - strokeWidthPx) / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    val circumference = (2f * PI.toFloat() * radius).coerceAtLeast(1f)

    // Calculate gap and cap angles
    val capDegrees = (strokeWidthPx / circumference) * 360f
    val gapDegrees = RingChartDefaults.GapDegrees
    val startAngle = -90f

    val expenseSweepRaw = 360f * ratio
    val remainingSweepRaw = 360f - expenseSweepRaw

    val expenseSweep = (expenseSweepRaw - gapDegrees - capDegrees).coerceAtLeast(0f)
    val remainingSweep = (remainingSweepRaw - gapDegrees - capDegrees).coerceAtLeast(0f)

    val topLeft = Offset(center.x - radius, center.y - radius)
    val arcSize = Size(radius * 2f, radius * 2f)
    val stroke = Stroke(strokeWidthPx, cap = StrokeCap.Round)

    // Draw Expense Segment
    if (ratio > RingChartDefaults.MinRatioForSegment && expenseSweep > 0f) {
        drawArc(
            color = expenseColor,
            startAngle = startAngle + (gapDegrees / 2f) + (capDegrees / 2f),
            sweepAngle = expenseSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke
        )
    }

    // Draw Remaining/Budget Segment
    if (ratio < RingChartDefaults.MaxRatioForSegment && remainingSweep > 0f) {
        drawArc(
            color = remainingColor,
            startAngle = startAngle + expenseSweepRaw + (gapDegrees / 2f) + (capDegrees / 2f),
            sweepAngle = remainingSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke
        )
    }
}

@Composable
private fun TopSpendingSection(
    category: String,
    percentText: String,
    amountText: String,
    icon: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Top Spending",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(CashioSpacing.xs))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CashioRadius.small))
                .clickable(onClick = onClick)
                .padding(vertical = CashioSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon Bubble
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 22.sp)
                }

                Column {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = percentText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View category details",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}