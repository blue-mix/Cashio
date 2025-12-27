package com.bluemix.cashio.ui.components.chart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluemix.cashio.presentation.analytics.vm.ChartPeriod
import com.bluemix.cashio.ui.components.defaults.CashioCard
import com.bluemix.cashio.ui.theme.CashioSemantic
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorPosition
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties.Rotation
import ir.ehsannarmani.compose_charts.models.PopupProperties

@Immutable
data class FinanceChartUi(
    val incomeData: List<Double>,
    val expenseData: List<Double>,
    val labels: List<String>
) {
    companion object {
        val Empty = FinanceChartUi(emptyList(), emptyList(), emptyList())
    }
}

/**
 * Card displaying income vs expense chart with selectable period.
 * Uses semantic colors so charts remain consistent across the app.
 */
@Composable
fun FinanceStatsCard(
    chart: FinanceChartUi,
    selectedPeriod: ChartPeriod,
    onPeriodChange: (ChartPeriod) -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹"
) {
    var isPeriodMenuExpanded by remember { mutableStateOf(false) }

    val labelCount = chart.labels.size
    val hasLabels = labelCount > 0

    // Guard: avoid chart spam when all values are 0 (optional UX improvement)
    val hasAnyValue = remember(chart.incomeData, chart.expenseData) {
        (chart.incomeData.any { it != 0.0 } || chart.expenseData.any { it != 0.0 })
    }

    // Adaptive bar sizing based on data density
    val (barThickness, barSpacing, cornerRadius) = remember(labelCount) {
        Triple(
            when {
                labelCount <= 5 -> 24.dp
                labelCount <= 6 -> 12.dp
                else -> 10.dp
            },
            if (labelCount <= 6) 4.dp else 2.dp,
            when {
                labelCount <= 5 -> 12.dp
                labelCount <= 6 -> 6.dp
                else -> 5.dp
            }
        )
    }

    // Semantic gradients
    val expenseBrush = remember {
        Brush.verticalGradient(
            listOf(
                CashioSemantic.ExpenseRed.copy(alpha = 0.85f),
                CashioSemantic.ExpenseRed
            )
        )
    }

    val incomeBrush = remember {
        Brush.verticalGradient(
            listOf(
                CashioSemantic.IncomeGreen.copy(alpha = 0.85f),
                CashioSemantic.IncomeGreen
            )
        )
    }

    // Build bars safely even if lists mismatch
    val barGroups = remember(chart.labels, chart.incomeData, chart.expenseData) {
        val safeCount = chart.labels.size
        List(safeCount) { index ->
            Bars(
                label = chart.labels[index],
                values = listOf(
                    Bars.Data(
                        label = SERIES_EXPENSE,
                        value = chart.expenseData.getOrElse(index) { 0.0 },
                        color = expenseBrush
                    ),
                    Bars.Data(
                        label = SERIES_INCOME,
                        value = chart.incomeData.getOrElse(index) { 0.0 },
                        color = incomeBrush
                    )
                )
            )
        }
    }

    CashioCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            HeaderRow(
                selectedPeriod = selectedPeriod,
                expanded = isPeriodMenuExpanded,
                onExpandedChange = { isPeriodMenuExpanded = it },
                onPeriodChange = { period ->
                    isPeriodMenuExpanded = false
                    onPeriodChange(period)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!hasLabels || !hasAnyValue) {
                Text(
                    text = "No data for ${selectedPeriod.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            ColumnChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                data = barGroups,
                barProperties = BarProperties(
                    cornerRadius = Bars.Data.Radius.Rectangle(
                        topLeft = cornerRadius,
                        topRight = cornerRadius,
                        bottomLeft = cornerRadius,
                        bottomRight = cornerRadius
                    ),
                    spacing = barSpacing,
                    thickness = barThickness
                ),
                labelHelperProperties = LabelHelperProperties(enabled = false),
                indicatorProperties = HorizontalIndicatorProperties(
                    enabled = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = MaterialTheme.typography.labelSmall.fontFamily
                    ),
                    padding = 8.dp,
                    position = IndicatorPosition.Horizontal.Start,
                    contentBuilder = { "$currencySymbol${it.toInt()}" }
                ),
                labelProperties = LabelProperties(
                    enabled = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontFamily = MaterialTheme.typography.labelSmall.fontFamily
                    ),
                    padding = 12.dp,
                    rotation = Rotation(degree = 0f)
                ),
                dividerProperties = DividerProperties(enabled = false),
                gridProperties = GridProperties(
                    enabled = true,
                    yAxisProperties = GridProperties.AxisProperties(false)
                ),
                popupProperties = PopupProperties(
                    enabled = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    cornerRadius = 6.dp,
                    contentBuilder = { "$currencySymbol${it.toInt()}" }
                )
            )
        }
    }
}

@Composable
private fun HeaderRow(
    selectedPeriod: ChartPeriod,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPeriodChange: (ChartPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PeriodDropdown(
            selected = selectedPeriod,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            onSelect = onPeriodChange
        )

        Legend(
            incomeColor = CashioSemantic.IncomeGreen,
            expenseColor = CashioSemantic.ExpenseRed
        )
    }
}

@Composable
private fun PeriodDropdown(
    selected: ChartPeriod,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (ChartPeriod) -> Unit
) {
    Box {
        OutlinedButton(
            onClick = { onExpandedChange(true) },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.height(40.dp)
        ) {
            Text(
                text = selected.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select period",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            ChartPeriod.entries.forEach { period ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = period.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (period == selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    },
                    onClick = { onSelect(period) },
                    leadingIcon = {
                        if (period == selected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun Legend(
    incomeColor: Color,
    expenseColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = incomeColor, label = SERIES_INCOME)
        LegendItem(color = expenseColor, label = SERIES_EXPENSE)
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private const val SERIES_INCOME = "Income"
private const val SERIES_EXPENSE = "Expense"
