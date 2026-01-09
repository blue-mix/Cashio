//package com.bluemix.cashio.ui.components.chart
//
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowDropDown
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.DropdownMenu
//import androidx.compose.material3.DropdownMenuItem
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.Immutable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.bluemix.cashio.presentation.analytics.vm.ChartPeriod
//import com.bluemix.cashio.ui.components.defaults.CashioCard
//import com.bluemix.cashio.ui.components.defaults.CashioRadius
//import com.bluemix.cashio.ui.theme.CashioSemantic
//import com.bluemix.cashio.ui.components.defaults.CashioSpacing
//import ir.ehsannarmani.compose_charts.ColumnChart
//import ir.ehsannarmani.compose_charts.models.BarProperties
//import ir.ehsannarmani.compose_charts.models.Bars
//import ir.ehsannarmani.compose_charts.models.DividerProperties
//import ir.ehsannarmani.compose_charts.models.GridProperties
//import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
//import ir.ehsannarmani.compose_charts.models.IndicatorPosition
//import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
//import ir.ehsannarmani.compose_charts.models.LabelProperties
//import ir.ehsannarmani.compose_charts.models.PopupProperties
//
///**
// * Immutable state holder for the Finance Chart.
// *
// * @property incomeData List of income values corresponding to each label.
// * @property expenseData List of expense values corresponding to each label.
// * @property labels X-axis labels (e.g., "Mon", "Tue" or "Week 1", "Week 2").
// */
//@Immutable
//data class FinanceChartUi(
//    val incomeData: List<Double>,
//    val expenseData: List<Double>,
//    val labels: List<String>
//) {
//    companion object {
//        val Empty = FinanceChartUi(emptyList(), emptyList(), emptyList())
//    }
//}
//
///**
// * A card component displaying a comparative Column Chart (Income vs Expense).
// *
// * Features:
// * - Period selection dropdown (controlled by [selectedPeriod]).
// * - Adaptive bar sizing: Bar thickness and spacing adjust based on the number of data points.
// * - Gradient styling for Income (Green) and Expense (Red) bars.
// *
// * @param chart The data to populate the chart.
// * @param selectedPeriod The currently active time period filter.
// * @param onPeriodChange Callback triggered when the user selects a different period.
// * @param currencySymbol Symbol used in tooltips and axis labels (default: "₹").
// */
//@Composable
//fun FinanceStatsCard(
//    chart: FinanceChartUi,
//    selectedPeriod: ChartPeriod,
//    onPeriodChange: (ChartPeriod) -> Unit,
//    modifier: Modifier = Modifier,
//    currencySymbol: String = "₹"
//) {
//    var isPeriodMenuExpanded by remember { mutableStateOf(false) }
//
//    val labelCount = chart.labels.size
//    val hasLabels = labelCount > 0
//    val hasAnyValue = remember(chart.incomeData, chart.expenseData) {
//        (chart.incomeData.any { it != 0.0 } || chart.expenseData.any { it != 0.0 })
//    }
//
//    // Adaptive sizing logic:
//    // Fewer items = thicker bars for better aesthetics.
//    // More items = thinner bars to fit within the viewport.
//    val (barThickness, barSpacing, cornerRadius) = remember(labelCount) {
//        Triple(
//            when {
//                labelCount <= 5 -> 24.dp
//                labelCount <= 6 -> 12.dp
//                else -> 10.dp
//            },
//            if (labelCount <= 6) CashioSpacing.xs else CashioSpacing.xxs,
//            when {
//                labelCount <= 5 -> CashioRadius.small
//                labelCount <= 6 -> CashioRadius.xs
//                else -> 5.dp
//            }
//        )
//    }
//
//    val expenseBrush = remember {
//        Brush.verticalGradient(
//            listOf(
//                CashioSemantic.ExpenseRed.copy(alpha = 0.85f),
//                CashioSemantic.ExpenseRed
//            )
//        )
//    }
//    val incomeBrush = remember {
//        Brush.verticalGradient(
//            listOf(
//                CashioSemantic.IncomeGreen.copy(alpha = 0.85f),
//                CashioSemantic.IncomeGreen
//            )
//        )
//    }
//
//    val barGroups = remember(chart.labels, chart.incomeData, chart.expenseData) {
//        val safeCount = chart.labels.size
//        List(safeCount) { index ->
//            Bars(
//                label = chart.labels[index],
//                values = listOf(
//                    Bars.Data(
//                        label = SERIES_EXPENSE,
//                        value = chart.expenseData.getOrElse(index) { 0.0 },
//                        color = expenseBrush
//                    ),
//                    Bars.Data(
//                        label = SERIES_INCOME,
//                        value = chart.incomeData.getOrElse(index) { 0.0 },
//                        color = incomeBrush
//                    )
//                )
//            )
//        }
//    }
//
//    CashioCard(modifier = modifier.fillMaxWidth()) {
//        Column {
//            HeaderRow(
//                selectedPeriod = selectedPeriod,
//                expanded = isPeriodMenuExpanded,
//                onExpandedChange = { isPeriodMenuExpanded = it },
//                onPeriodChange = { period ->
//                    isPeriodMenuExpanded = false
//                    onPeriodChange(period)
//                }
//            )
//
//            Spacer(modifier = Modifier.height(CashioSpacing.huge))
//
//            if (!hasLabels || !hasAnyValue) {
//                Text(
//                    text = "No data for ${selectedPeriod.label}",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//                return@Column
//            }
//
//            ColumnChart(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp),
//                data = barGroups,
//                barProperties = BarProperties(
//                    cornerRadius = Bars.Data.Radius.Rectangle(
//                        cornerRadius,
//                        cornerRadius,
//                        cornerRadius,
//                        cornerRadius
//                    ),
//                    spacing = barSpacing,
//                    thickness = barThickness
//                ),
//                labelHelperProperties = LabelHelperProperties(enabled = false),
//                indicatorProperties = HorizontalIndicatorProperties(
//                    enabled = true,
//                    textStyle = TextStyle(
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        fontSize = 11.sp,
//                        fontFamily = MaterialTheme.typography.labelSmall.fontFamily
//                    ),
//                    padding = CashioSpacing.small,
//                    position = IndicatorPosition.Horizontal.Start,
//                    contentBuilder = { "$currencySymbol${it.toInt()}" }
//                ),
//                labelProperties = LabelProperties(
//                    enabled = true,
//                    textStyle = TextStyle(
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        fontSize = 11.sp,
//                        fontFamily = MaterialTheme.typography.labelSmall.fontFamily
//                    ),
//                    padding = CashioSpacing.medium,
//                    rotation = LabelProperties.Rotation(degree = 0f)
//                ),
//                dividerProperties = DividerProperties(enabled = false),
//                gridProperties = GridProperties(
//                    enabled = true,
//                    yAxisProperties = GridProperties.AxisProperties(false)
//                ),
//                popupProperties = PopupProperties(
//                    enabled = true,
//                    textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
//                    containerColor = MaterialTheme.colorScheme.inverseSurface,
//                    cornerRadius = CashioRadius.xs,
//                    contentBuilder = { "$currencySymbol${it.toInt()}" }
//                )
//            )
//        }
//    }
//}
//
//@Composable
//private fun HeaderRow(
//    selectedPeriod: ChartPeriod,
//    expanded: Boolean,
//    onExpandedChange: (Boolean) -> Unit,
//    onPeriodChange: (ChartPeriod) -> Unit
//) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        PeriodDropdown(
//            selected = selectedPeriod,
//            expanded = expanded,
//            onExpandedChange = onExpandedChange,
//            onSelect = onPeriodChange
//        )
//
//        Legend(
//            incomeColor = CashioSemantic.IncomeGreen,
//            expenseColor = CashioSemantic.ExpenseRed
//        )
//    }
//}
//
//@Composable
//private fun PeriodDropdown(
//    selected: ChartPeriod,
//    expanded: Boolean,
//    onExpandedChange: (Boolean) -> Unit,
//    onSelect: (ChartPeriod) -> Unit
//) {
//    Box {
//        OutlinedButton(
//            onClick = { onExpandedChange(true) },
//            shape = RoundedCornerShape(CashioRadius.pill),
//            colors = ButtonDefaults.outlinedButtonColors(
//                containerColor = Color.Transparent,
//                contentColor = MaterialTheme.colorScheme.onSurface
//            ),
//            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
//            contentPadding = PaddingValues(
//                horizontal = CashioSpacing.default,
//                vertical = CashioSpacing.small
//            ),
//            modifier = Modifier.height(40.dp)
//        ) {
//            Text(text = selected.label, style = MaterialTheme.typography.labelLarge)
//            Spacer(modifier = Modifier.width(CashioSpacing.xs))
//            Icon(
//                imageVector = Icons.Default.ArrowDropDown,
//                contentDescription = null,
//                modifier = Modifier.size(20.dp)
//            )
//        }
//
//        DropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { onExpandedChange(false) },
//            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
//        ) {
//            ChartPeriod.entries.forEach { period ->
//                DropdownMenuItem(
//                    text = {
//                        Text(
//                            text = period.label,
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = if (period == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
//                        )
//                    },
//                    onClick = { onSelect(period) },
//                    leadingIcon = {
//                        if (period == selected) {
//                            Icon(
//                                imageVector = Icons.Default.Check,
//                                contentDescription = null,
//                                tint = MaterialTheme.colorScheme.primary,
//                                modifier = Modifier.size(20.dp)
//                            )
//                        }
//                    }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun Legend(incomeColor: Color, expenseColor: Color) {
//    Row(
//        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.default),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        LegendItem(color = incomeColor, label = SERIES_INCOME)
//        LegendItem(color = expenseColor, label = SERIES_EXPENSE)
//    }
//}
//
//@Composable
//private fun LegendItem(color: Color, label: String) {
//    Row(
//        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.tiny),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Box(
//            modifier = Modifier
//                .size(8.dp)
//                .background(color, CircleShape)
//        )
//        Text(
//            text = label,
//            style = MaterialTheme.typography.labelSmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//    }
//}
//
//private const val SERIES_INCOME = "Income"
//private const val SERIES_EXPENSE = "Expense"

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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.bluemix.cashio.ui.components.defaults.CashioRadius
import com.bluemix.cashio.ui.components.defaults.CashioSpacing
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
import ir.ehsannarmani.compose_charts.models.PopupProperties
import kotlin.math.min

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
 * Card displaying Income vs Expense bar chart with period selector + legend.
 *
 * Optimizations:
 * - clamps data to min(labels, income, expense) to prevent mismatch crashes/blank visuals
 * - stable brush + text styles
 * - rememberSaveable for dropdown state (better inside Lazy lists)
 */
@Composable
fun FinanceStatsCard(
    chart: FinanceChartUi,
    selectedPeriod: ChartPeriod,
    onPeriodChange: (ChartPeriod) -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹"
) {
    var isPeriodMenuExpanded by rememberSaveable { mutableStateOf(false) }

    val safeCount = remember(chart.labels, chart.incomeData, chart.expenseData) {
        min(chart.labels.size, min(chart.incomeData.size, chart.expenseData.size))
    }

    val hasLabels = safeCount > 0

    val hasAnyValue = remember(chart.incomeData, chart.expenseData, safeCount) {
        if (safeCount == 0) false
        else {
            var any = false
            for (i in 0 until safeCount) {
                if (chart.incomeData[i] != 0.0 || chart.expenseData[i] != 0.0) {
                    any = true
                    break
                }
            }
            any
        }
    }

    val (barThickness, barSpacing, cornerRadius) = remember(safeCount) {
        val thickness = when {
            safeCount <= 5 -> 24.dp
            safeCount <= 6 -> 12.dp
            else -> 10.dp
        }
        val spacing = if (safeCount <= 6) CashioSpacing.xs else CashioSpacing.xxs
        val radius = when {
            safeCount <= 5 -> CashioRadius.small
            safeCount <= 6 -> CashioRadius.xs
            else -> 5.dp
        }
        Triple(thickness, spacing, radius)
    }

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

    val axisTextStyle = remember {
        TextStyle(
            fontSize = 11.sp
        )
    }

    val barGroups = remember(chart.labels, chart.incomeData, chart.expenseData, safeCount) {
        if (safeCount == 0) emptyList()
        else {
            List(safeCount) { index ->
                Bars(
                    label = chart.labels[index],
                    values = listOf(
                        Bars.Data(
                            label = SERIES_EXPENSE,
                            value = chart.expenseData[index],
                            color = expenseBrush
                        ),
                        Bars.Data(
                            label = SERIES_INCOME,
                            value = chart.incomeData[index],
                            color = incomeBrush
                        )
                    )
                )
            }
        }
    }

    CashioCard(modifier = modifier.fillMaxWidth()) {
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

            Spacer(modifier = Modifier.height(CashioSpacing.huge))

            if (!hasLabels || !hasAnyValue) {
                Text(
                    text = "No data for ${selectedPeriod.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@CashioCard
            }

            ColumnChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                data = barGroups,
                barProperties = BarProperties(
                    cornerRadius = Bars.Data.Radius.Rectangle(
                        cornerRadius,
                        cornerRadius,
                        cornerRadius,
                        cornerRadius
                    ),
                    spacing = barSpacing,
                    thickness = barThickness
                ),
                labelHelperProperties = LabelHelperProperties(enabled = false),
                indicatorProperties = HorizontalIndicatorProperties(
                    enabled = true,
                    textStyle = axisTextStyle.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = MaterialTheme.typography.labelSmall.fontFamily
                    ),
                    padding = CashioSpacing.small,
                    position = IndicatorPosition.Horizontal.Start,
                    contentBuilder = { "$currencySymbol${it.toInt()}" }
                ),
                labelProperties = LabelProperties(
                    enabled = true,
                    textStyle = axisTextStyle.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = MaterialTheme.typography.labelSmall.fontFamily
                    ),
                    padding = CashioSpacing.medium,
                    rotation = LabelProperties.Rotation(degree = 0f)
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
                    cornerRadius = CashioRadius.xs,
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
            shape = RoundedCornerShape(CashioRadius.pill),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            contentPadding = PaddingValues(
                horizontal = CashioSpacing.default,
                vertical = CashioSpacing.small
            ),
            modifier = Modifier.height(40.dp)
        ) {
            Text(text = selected.label, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.width(CashioSpacing.xs))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
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
                            color = if (period == selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
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
private fun Legend(incomeColor: Color, expenseColor: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.default),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = incomeColor, label = SERIES_INCOME)
        LegendItem(color = expenseColor, label = SERIES_EXPENSE)
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.tiny),
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
