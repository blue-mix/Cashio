
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluemix.cashio.domain.model.Currency
import com.bluemix.cashio.presentation.analytics.vm.ChartPeriod
import com.bluemix.cashio.ui.defaults.CashioCard
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioSpacing
import com.bluemix.cashio.ui.theme.CashioSemantic
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import kotlin.math.min

@Immutable
data class FinanceChartUi(
    val incomeDataPaise: List<Long>,
    val expenseDataPaise: List<Long>,
    val labels: List<String>
) {
    companion object {
        val Empty = FinanceChartUi(emptyList(), emptyList(), emptyList())
    }

    // Pre-calculate to avoid recomputation
    val isEmpty: Boolean get() = labels.isEmpty()
    val safeSize: Int get() = min(labels.size, min(incomeDataPaise.size, expenseDataPaise.size))
}

// Define brushes at top level to avoid recreation
private val ExpenseBrush = Brush.verticalGradient(
    listOf(
        CashioSemantic.ExpenseRed.copy(alpha = 0.85f),
        CashioSemantic.ExpenseRed
    )
)

private val IncomeBrush = Brush.verticalGradient(
    listOf(
        CashioSemantic.IncomeGreen.copy(alpha = 0.85f),
        CashioSemantic.IncomeGreen
    )
)

@Composable
fun FinanceStatsCard(
    chart: FinanceChartUi,
    selectedPeriod: ChartPeriod,
    onPeriodChange: (ChartPeriod) -> Unit,
    modifier: Modifier = Modifier,
    currency: Currency = Currency.INR
) {
    var isPeriodMenuExpanded by rememberSaveable { mutableStateOf(false) }

    // Single derived state calculation
    val chartState = remember(chart) {
        ChartDisplayState.from(chart)
    }

    CashioCard(modifier = modifier.fillMaxWidth()) {
        Column {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PeriodDropdown(
                    selected = selectedPeriod,
                    expanded = isPeriodMenuExpanded,
                    onExpandedChange = { isPeriodMenuExpanded = it },
                    onSelect = {
                        isPeriodMenuExpanded = false
                        onPeriodChange(it)
                    }
                )
                ChartLegend()
            }

            Spacer(modifier = Modifier.height(CashioSpacing.lg))

            // Content based on state
            when {
                chartState.isEmpty -> EmptyChartPlaceholder(selectedPeriod.label)
                else -> FinanceColumnChart(
                    state = chartState,
                    currency = currency
                )
            }
        }
    }
}

// Separate state holder for better performance
@Immutable
private data class ChartDisplayState(
    val barGroups: List<Bars>,
    val barThickness: androidx.compose.ui.unit.Dp,
    val barSpacing: androidx.compose.ui.unit.Dp,
    val cornerRadius: androidx.compose.ui.unit.Dp,
    val isEmpty: Boolean
) {
    companion object {
        fun from(chart: FinanceChartUi): ChartDisplayState {
            val safeCount = chart.safeSize

            if (safeCount == 0) {
                return ChartDisplayState(
                    barGroups = emptyList(),
                    barThickness = 24.dp,
                    barSpacing = CashioSpacing.xs,
                    cornerRadius = CashioRadius.small,
                    isEmpty = true
                )
            }

            // Check if there's any actual data
            val hasAnyValue = (0 until safeCount).any { i ->
                chart.incomeDataPaise[i] != 0L || chart.expenseDataPaise[i] != 0L
            }

            if (!hasAnyValue) {
                return ChartDisplayState(
                    barGroups = emptyList(),
                    barThickness = 24.dp,
                    barSpacing = CashioSpacing.xs,
                    cornerRadius = CashioRadius.small,
                    isEmpty = true
                )
            }

            // Dynamic UI scaling based on data count
            val (thickness, spacing, radius) = when {
                safeCount <= 5 -> Triple(24.dp, CashioSpacing.xs, CashioRadius.small)
                safeCount <= 7 -> Triple(14.dp, CashioSpacing.xs, CashioRadius.small)
                else -> Triple(10.dp, 4.dp, 4.dp)
            }

            // Convert data for chart library
            val bars = List(safeCount) { i ->
                Bars(
                    label = chart.labels[i],
                    values = listOf(
                        Bars.Data(
                            label = "Expense",
                            value = chart.expenseDataPaise[i] / 100.0,
                            color = ExpenseBrush
                        ),
                        Bars.Data(
                            label = "Income",
                            value = chart.incomeDataPaise[i] / 100.0,
                            color = IncomeBrush
                        )
                    )
                )
            }

            return ChartDisplayState(
                barGroups = bars,
                barThickness = thickness,
                barSpacing = spacing,
                cornerRadius = radius,
                isEmpty = false
            )
        }
    }
}

@Composable
private fun FinanceColumnChart(
    state: ChartDisplayState,
    currency: Currency
) {
    ColumnChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        data = state.barGroups,
        barProperties = BarProperties(
            cornerRadius = Bars.Data.Radius.Rectangle(
                topLeft = state.cornerRadius,
                topRight = state.cornerRadius,
                bottomLeft = 0.dp,
                bottomRight = 0.dp
            ),
            spacing = state.barSpacing,
            thickness = state.barThickness
        ),
        labelHelperProperties = LabelHelperProperties(enabled = false),
        indicatorProperties = HorizontalIndicatorProperties(
            enabled = true,
            contentBuilder = { "${currency.symbol}${it.toInt()}" },
            textStyle = TextStyle.Default.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = MaterialTheme.typography.labelSmall.fontFamily,
                fontSize = 10.sp
            )
        ),
        labelProperties = LabelProperties(
            enabled = true,
            textStyle = TextStyle.Default.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = MaterialTheme.typography.labelSmall.fontFamily,
                fontSize = 10.sp
            )
        ),
        gridProperties = GridProperties(
            enabled = true,
            yAxisProperties = GridProperties.AxisProperties(enabled = false)
        ),
        dividerProperties = DividerProperties(enabled = false)
    )
}

@Composable
private fun EmptyChartPlaceholder(periodLabel: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CashioSpacing.xs)
        ) {
            Text(
                text = "No data for $periodLabel",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Start tracking your finances to see insights",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
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
            shape = RoundedCornerShape(CashioRadius.pill),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            ),
            modifier = Modifier.height(36.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
        ) {
            Text(
                text = selected.label,
                style = MaterialTheme.typography.labelLarge
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select period"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            ChartPeriod.entries.forEach { period ->
                DropdownMenuItem(
                    text = { Text(period.label) },
                    onClick = { onSelect(period) },
                    leadingIcon = {
                        if (period == selected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ChartLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(CashioSpacing.md)) {
        LegendItem(color = CashioSemantic.IncomeGreen, label = "Income")
        LegendItem(color = CashioSemantic.ExpenseRed, label = "Expense")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
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