//package com.bluemix.cashio.ui.components.chart
//
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ChevronRight
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.bluemix.cashio.core.format.CashioFormat
//import com.bluemix.cashio.ui.components.defaults.CashioCard
//import com.bluemix.cashio.ui.components.defaults.CashioRadius
//import com.bluemix.cashio.ui.theme.CashioSemantic
//import com.bluemix.cashio.ui.components.defaults.CashioSpacing
//import kotlin.math.roundToInt
//
///**
// * A dashboard summary card featuring a custom Ring Chart.
// *
// * Visualizes the ratio of Expenses vs. Remaining Budget (or Income).
// * Also displays the highest spending category for quick insight.
// *
// * @param totalAmount The central figure displayed inside the ring.
// * @param periodLabel Context label (e.g., "This Month", "Today").
// * @param expenseRatio Float between 0.0 and 1.0 representing the expense percentage.
// * @param topCategory Name of the category with the highest spend.
// * @param topCategoryAmount Amount spent in the top category.
// */
//@Composable
//fun SpendingOverviewCard(
//    totalAmount: Double,
//    periodLabel: String,
//    expenseRatio: Float,
//    topCategory: String,
//    topCategoryAmount: Double,
//    topCategoryIcon: String = "💡",
//    topCategoryColor: Color = MaterialTheme.colorScheme.primary,
//    onTopCategoryClick: () -> Unit = {},
//    currencySymbol: String = "₹",
//    modifier: Modifier = Modifier
//) {
//    val expenseRatioClamped = expenseRatio.coerceIn(0f, 1f)
//    val expenseRingColor = CashioSemantic.ExpenseRed
//    val remainingRingColor = MaterialTheme.colorScheme.primary
//
//    CashioCard(modifier = modifier.fillMaxWidth()) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//
//            // ───────────────────── Ring Chart ─────────────────────
//            Box(
//                contentAlignment = Alignment.Center,
//                modifier = Modifier
//                    .size(220.dp)
//                    .padding(vertical = CashioSpacing.default)
//            ) {
//                Canvas(modifier = Modifier.size(220.dp)) {
//                    // Stroke aligns to grid (12dp)
//                    val strokeWidth = CashioSpacing.medium.toPx()
//                    val radius = (size.minDimension - strokeWidth) / 2
//                    val center = Offset(size.width / 2, size.height / 2)
//
//                    // Calculate gaps to prevent arcs from touching cleanly
//                    val gapDegrees = 4f
//
//                    // Calculate visual correction for Round Caps so arcs don't overlap
//                    val capDegrees = ((180 * strokeWidth) / (Math.PI * radius)).toFloat()
//
//                    val expenseSweep = 360f * expenseRatioClamped
//
//                    // Draw Expense Arc (Red)
//                    if (expenseSweep > 0f) {
//                        drawArc(
//                            color = expenseRingColor,
//                            startAngle = -90f + capDegrees / 2f,
//                            sweepAngle = expenseSweep - gapDegrees - capDegrees,
//                            useCenter = false,
//                            topLeft = Offset(center.x - radius, center.y - radius),
//                            size = Size(radius * 2, radius * 2),
//                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
//                        )
//                    }
//
//                    // Draw Remaining Arc (Primary Color)
//                    val remainingSweep = 360f - expenseSweep - gapDegrees - capDegrees
//                    if (remainingSweep > 0f) {
//                        drawArc(
//                            color = remainingRingColor,
//                            startAngle = -90f + expenseSweep + capDegrees / 2f,
//                            sweepAngle = remainingSweep,
//                            useCenter = false,
//                            topLeft = Offset(center.x - radius, center.y - radius),
//                            size = Size(radius * 2, radius * 2),
//                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
//                        )
//                    }
//                }
//
//                // Center Content
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text(
//                        text = periodLabel,
//                        style = MaterialTheme.typography.labelMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Spacer(modifier = Modifier.height(CashioSpacing.tiny))
//                    Text(
//                        text = CashioFormat.money(totalAmount, currencySymbol),
//                        style = MaterialTheme.typography.headlineLarge.copy(
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 32.sp
//                        ),
//                        color = MaterialTheme.colorScheme.onSurface,
//                        textAlign = TextAlign.Center
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(CashioSpacing.xl))
//            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
//            Spacer(modifier = Modifier.height(CashioSpacing.default))
//
//            // ───────────────────── Top Category Insight ─────────────────────
//            Column {
//                Text(
//                    text = "Top Spending",
//                    style = MaterialTheme.typography.titleSmall,
//                    fontWeight = FontWeight.SemiBold
//                )
//
//                Spacer(modifier = Modifier.height(CashioSpacing.medium))
//
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(CashioRadius.small))
//                        .clickable(onClick = onTopCategoryClick)
//                        .padding(vertical = CashioSpacing.small),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .size(40.dp)
//                                .clip(CircleShape)
//                                .background(topCategoryColor.copy(alpha = 0.15f)),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(text = topCategoryIcon, fontSize = 20.sp)
//                        }
//
//                        Column {
//                            Text(
//                                text = topCategory,
//                                style = MaterialTheme.typography.bodyMedium,
//                                fontWeight = FontWeight.Medium
//                            )
//                            Text(
//                                text = "${(expenseRatioClamped * 100).roundToInt()}% of total",
//                                style = MaterialTheme.typography.labelSmall,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
//                    }
//
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.small),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = CashioFormat.money(topCategoryAmount, currencySymbol),
//                            style = MaterialTheme.typography.titleSmall,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                        Icon(
//                            imageVector = Icons.Default.ChevronRight,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.outline,
//                            modifier = Modifier.size(20.dp)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

package com.bluemix.cashio.ui.components.chart

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluemix.cashio.core.format.CashioFormat
import com.bluemix.cashio.ui.components.defaults.CashioCard
import com.bluemix.cashio.ui.components.defaults.CashioRadius
import com.bluemix.cashio.ui.components.defaults.CashioSpacing
import com.bluemix.cashio.ui.theme.CashioSemantic
import kotlin.math.PI
import kotlin.math.roundToInt

/**
 * A dashboard summary card featuring a custom Ring Chart.
 *
 * Visualizes the ratio of Expenses vs. Remaining Budget (or Income).
 * Also displays the highest spending category for quick insight.
 */
@Composable
fun SpendingOverviewCard(
    totalAmount: Double,
    periodLabel: String,
    expenseRatio: Float,
    topCategory: String,
    topCategoryAmount: Double,
    topCategoryIcon: String = "💡",
    topCategoryColor: Color = MaterialTheme.colorScheme.primary,
    onTopCategoryClick: () -> Unit = {},
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val ratio = remember(expenseRatio) { expenseRatio.coerceIn(0f, 1f) }
    val percentText = remember(ratio) { "${(ratio * 100).roundToInt()}% of total" }
    val expenseRingColor = CashioSemantic.ExpenseRed
    val remainingRingColor = cs.primary
    val totalText = remember(totalAmount, currencySymbol) {
        CashioFormat.money(totalAmount, currencySymbol)
    }
    val topAmountText = remember(topCategoryAmount, currencySymbol) {
        CashioFormat.money(topCategoryAmount, currencySymbol)
    }

    CashioCard(modifier = modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // ───────────────────── Ring Chart ─────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .padding(vertical = CashioSpacing.default)
            ) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    val strokeWidthPx = CashioSpacing.medium.toPx()
                    val radius = (size.minDimension - strokeWidthPx) / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    val gapDegrees = 4f


                    val circumference = (2f * PI.toFloat() * radius).coerceAtLeast(1f)
                    val capDegrees = (strokeWidthPx / circumference) * 360f
                    val startAngle = -90f
                    val expenseSweepRaw = 360f * ratio
                    val remainingSweepRaw = 360f - expenseSweepRaw
                    val expenseSweep = (expenseSweepRaw - gapDegrees - capDegrees).coerceAtLeast(0f)
                    val remainingSweep =
                        (remainingSweepRaw - gapDegrees - capDegrees).coerceAtLeast(0f)
                    val topLeft = Offset(center.x - radius, center.y - radius)
                    val arcSize = Size(radius * 2f, radius * 2f)
                    val stroke = Stroke(strokeWidthPx, cap = StrokeCap.Round)

                    if (ratio > 0f && expenseSweep > 0f) {
                        drawArc(
                            color = expenseRingColor,
                            startAngle = startAngle + (capDegrees / 2f),
                            sweepAngle = expenseSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = stroke
                        )
                    }
                    if (ratio < 1f && remainingSweep > 0f) {
                        drawArc(
                            color = remainingRingColor,
                            startAngle = startAngle + expenseSweepRaw + (capDegrees / 2f),
                            sweepAngle = remainingSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = stroke
                        )
                    }
                }

                // Center Content
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = cs.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(CashioSpacing.tiny))
                    Text(
                        text = totalText,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = cs.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(CashioSpacing.xl))
            HorizontalDivider(color = cs.outlineVariant)
            Spacer(modifier = Modifier.height(CashioSpacing.default))

            // ───────────────────── Top Category Insight ─────────────────────
            Column {
                Text(
                    text = "Top Spending",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(CashioSpacing.medium))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CashioRadius.small))
                        .clickable(onClick = onTopCategoryClick)
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(topCategoryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = topCategoryIcon, fontSize = 20.sp)
                        }

                        Column {
                            Text(
                                text = topCategory,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = percentText,
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = topAmountText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = cs.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}