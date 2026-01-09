//package com.bluemix.cashio.presentation.history
//
//import androidx.compose.animation.core.Spring
//import androidx.compose.animation.core.animateDpAsState
//import androidx.compose.animation.core.spring
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.aspectRatio
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.lazy.LazyListState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.derivedStateOf
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.bluemix.cashio.ui.components.defaults.CashioPadding
//import com.bluemix.cashio.ui.theme.CashioSemantic
//import com.bluemix.cashio.ui.components.defaults.CashioSpacing
//import com.kizitonwose.calendar.compose.HorizontalCalendar
//import com.kizitonwose.calendar.compose.rememberCalendarState
//import com.kizitonwose.calendar.core.CalendarDay
//import com.kizitonwose.calendar.core.DayPosition
//import com.kizitonwose.calendar.core.daysOfWeek
//import java.time.DayOfWeek
//import java.time.LocalDate
//import java.time.YearMonth
//import java.time.format.TextStyle
//import java.util.Locale
//
///**
// * A collapsible/sticky calendar header for the History screen.
// *
// * Features:
// * 1. **Scroll Awareness:** Monitors [listState] to animate elevation/shadow when the user
// * scrolls the transaction list beneath it, mimicking a sticky header.
// * 2. **Heat Map:** Visualizes daily spending intensity using [expenseHeatLevelByDate].
// *
// * @param listState State of the transaction list below to trigger elevation changes.
// * @param expenseTotalByDate Map of Date -> Total Amount (used for dot indicators).
// * @param expenseHeatLevelByDate Map of Date -> Intensity (0-4) for background coloring.
// * @param selectedDate The currently active filter date.
// */
//
//@Composable
//fun CalendarStickyHeader(
//    listState: LazyListState,
//    expenseTotalByDate: Map<LocalDate, Double>,
//    expenseHeatLevelByDate: Map<LocalDate, Int>,
//    today: LocalDate,
//    selectedDate: LocalDate?,
//    onDayClick: (LocalDate) -> Unit
//) {
//    val (calendarState, weekDays) = rememberHistoryCalendarStateAndWeekDays()
//
//    // Determine if the content below has scrolled, requiring the header to "lift" visually.
//    val isScrolled by remember(listState) {
//        derivedStateOf {
//            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
//        }
//    }
//
//    val elevation by animateDpAsState(
//        targetValue = if (isScrolled) 6.dp else 0.dp,
//        animationSpec = spring(
//            dampingRatio = Spring.DampingRatioNoBouncy,
//            stiffness = Spring.StiffnessMedium
//        ),
//        label = "CalendarHeaderElevation"
//    )
//
//    val containerColor = if (isScrolled) {
//        MaterialTheme.colorScheme.surface
//    } else {
//        MaterialTheme.colorScheme.background
//    }
//
//    Surface(
//        modifier = Modifier.fillMaxWidth(),
//        color = containerColor,
//        tonalElevation = elevation,
//        shadowElevation = elevation
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = CashioPadding.screen)
//        ) {
//            HorizontalCalendar(
//                state = calendarState,
//                monthHeader = { MonthHeader(weekDays) },
//                dayContent = { day ->
//                    val spending = expenseTotalByDate[day.date] ?: 0.0
//                    val heat = expenseHeatLevelByDate[day.date] ?: 0
//
//                    SpendingDay(
//                        day = day,
//                        spending = spending,
//                        heatLevel = heat,
//                        isSelected = selectedDate == day.date,
//                        isToday = day.date == today,
//                        onClick = { onDayClick(day.date) }
//                    )
//                }
//            )
//
//            Spacer(modifier = Modifier.height(CashioSpacing.medium))
//        }
//    }
//}
//@Composable
//private fun MonthHeader(daysOfWeek: List<DayOfWeek>) {
//    val locale = remember { Locale.getDefault() }
//    val labels = remember(daysOfWeek, locale) {
//        daysOfWeek.map { it.getDisplayName(TextStyle.NARROW, locale) }
//    }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = CashioSpacing.medium, horizontal = CashioSpacing.xs)
//    ) {
//        labels.forEach { label ->
//            Text(
//                text = label,
//                modifier = Modifier.weight(1f),
//                style = MaterialTheme.typography.labelMedium,
//                fontSize = 12.sp,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                fontWeight = FontWeight.Bold,
//                textAlign = TextAlign.Center
//            )
//        }
//    }
//}
//
///**
// * Renders a single day cell in the calendar.
// *
// * Visual Hierarchy (Priority High to Low):
// * 1. **Selected:** Solid Primary color.
// * 2. **Today:** Primary Container color (if not selected).
// * 3. **Heat Level:** Background tint indicating spending intensity (Level 1-4).
// * 4. **Default:** Transparent.
// *
// * Additionally, a small dot indicator appears if there is any spending on that day
// * but the day is not currently selected.
// */
//@Composable
//private fun SpendingDay(
//    day: CalendarDay,
//    spending: Double,
//    heatLevel: Int,
//    isSelected: Boolean,
//    isToday: Boolean,
//    onClick: () -> Unit
//) {
//    val isCurrentMonth = day.position == DayPosition.MonthDate
//    val hasSpending = spending > 0
//    val cs = MaterialTheme.colorScheme
//
//    // Determine background color based on spending intensity (Heat Map)
//    val heatBg: Color? = when {
//        !isCurrentMonth || heatLevel == 0 -> null
//        heatLevel == 4 -> cs.errorContainer     // High Spend
//        heatLevel == 3 -> cs.tertiaryContainer  // Med-High
//        heatLevel == 2 -> cs.primaryContainer   // Medium
//        else -> cs.secondaryContainer           // Low
//    }
//
//    val background = when {
//        isSelected -> cs.primary
//        isToday && isCurrentMonth -> cs.primaryContainer
//        else -> heatBg ?: Color.Transparent
//    }
//
//    val textColor = when {
//        isSelected -> cs.onPrimary
//        isToday && isCurrentMonth -> cs.onPrimaryContainer
//        heatBg != null -> cs.onSurface
//        !isCurrentMonth -> cs.onSurfaceVariant.copy(alpha = 0.4f)
//        else -> cs.onSurfaceVariant
//    }
//
//    Box(
//        modifier = Modifier
//            .aspectRatio(1.2f)
//            .padding(CashioSpacing.xxs)
//            .clip(CircleShape)
//            .background(background)
//            .clickable(enabled = isCurrentMonth, onClick = onClick),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Text(
//                text = day.date.dayOfMonth.toString(),
//                style = MaterialTheme.typography.bodyMedium,
//                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
//                fontSize = 13.sp,
//                color = textColor
//            )
//
//            // Small dot indicator for spending presence
//            if (hasSpending && !isSelected && isCurrentMonth) {
//                val dotColor = when {
//                    spending > 1000 -> CashioSemantic.ExpenseRed
//                    spending > 500 -> cs.tertiary
//                    else -> cs.primary
//                }
//
//                Box(
//                    modifier = Modifier
//                        .padding(top = CashioSpacing.xxs)
//                        .size(CashioSpacing.xs)
//                        .background(dotColor, CircleShape)
//                )
//            }
//        }
//    }
//}
//
///**
// * Configures the Calendar state with a range of -12 months to +1 month from today.
// */
//@Composable
//private fun rememberHistoryCalendarStateAndWeekDays():
//        Pair<com.kizitonwose.calendar.compose.CalendarState, List<DayOfWeek>> {
//    val currentMonth = remember { YearMonth.now() }
//    val startMonth = remember(currentMonth) { currentMonth.minusMonths(12) }
//    val endMonth = remember(currentMonth) { currentMonth.plusMonths(1) }
//    val weekDays = remember { daysOfWeek() }
//
//    val calendarState = rememberCalendarState(
//        startMonth = startMonth,
//        endMonth = endMonth,
//        firstVisibleMonth = currentMonth,
//        firstDayOfWeek = weekDays.first()
//    )
//
//    return calendarState to weekDays
//}

package com.bluemix.cashio.presentation.history

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluemix.cashio.ui.components.defaults.CashioPadding
import com.bluemix.cashio.ui.components.defaults.CashioSpacing
import com.bluemix.cashio.ui.theme.CashioSemantic
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarStickyHeader(
    listState: LazyListState,
    expenseTotalByDate: Map<LocalDate, Double>,
    expenseHeatLevelByDate: Map<LocalDate, Int>,
    today: LocalDate,
    selectedDate: LocalDate?,
    onDayClick: (LocalDate) -> Unit
) {
    val (calendarState, weekDays) = rememberHistoryCalendarStateAndWeekDays(today)

    // ✅ don't key derivedStateOf with listState (stable object)
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    val elevation by animateDpAsState(
        targetValue = if (isScrolled) 6.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "CalendarHeaderElevation"
    )

    val containerColor = if (isScrolled) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.background
    }

    // ✅ Precompute heat palette once per colorScheme change
    val cs = MaterialTheme.colorScheme
    val heatPalette = remember(cs) {
        arrayOf(
            Color.Transparent,          // 0 (unused)
            cs.secondaryContainer,      // 1 low
            cs.primaryContainer,        // 2 medium
            cs.tertiaryContainer,       // 3 med-high
            cs.errorContainer           // 4 high
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = containerColor,
        tonalElevation = elevation,
        shadowElevation = elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CashioPadding.screen)
        ) {
            HorizontalCalendar(
                state = calendarState,
                monthHeader = { MonthHeader(weekDays) },
                dayContent = { day ->
                    val spending = expenseTotalByDate[day.date] ?: 0.0
                    val heat = (expenseHeatLevelByDate[day.date] ?: 0).coerceIn(0, 4)

                    SpendingDay(
                        day = day,
                        spending = spending,
                        heatLevel = heat,
                        isSelected = selectedDate == day.date,
                        isToday = day.date == today,
                        heatPalette = heatPalette,
                        onClick = { onDayClick(day.date) }
                    )
                }
            )

            Spacer(modifier = Modifier.height(CashioSpacing.medium))
        }
    }
}

@Composable
private fun MonthHeader(daysOfWeek: List<DayOfWeek>) {
    val locale = remember { Locale.getDefault() }

    // ✅ Precompute weekday labels once
    val labels = remember(daysOfWeek, locale) {
        daysOfWeek.map { it.getDisplayName(TextStyle.NARROW, locale) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CashioSpacing.medium, horizontal = CashioSpacing.xs)
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SpendingDay(
    day: CalendarDay,
    spending: Double,
    heatLevel: Int,
    isSelected: Boolean,
    isToday: Boolean,
    heatPalette: Array<Color>,
    onClick: () -> Unit
) {
    val isCurrentMonth = day.position == DayPosition.MonthDate
    val hasSpending = spending > 0
    val cs = MaterialTheme.colorScheme

    val heatBg: Color? = if (!isCurrentMonth || heatLevel == 0) null else heatPalette[heatLevel]

    val background = when {
        isSelected -> cs.primary
        isToday && isCurrentMonth -> cs.primaryContainer
        else -> heatBg ?: Color.Transparent
    }

    val textColor = when {
        isSelected -> cs.onPrimary
        isToday && isCurrentMonth -> cs.onPrimaryContainer
        heatBg != null -> cs.onSurface
        !isCurrentMonth -> cs.onSurfaceVariant.copy(alpha = 0.4f)
        else -> cs.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .aspectRatio(1.2f)
            .padding(CashioSpacing.xxs)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = isCurrentMonth, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                fontSize = 13.sp,
                color = textColor
            )

            if (hasSpending && !isSelected && isCurrentMonth) {
                val dotColor = when {
                    spending > 1000 -> CashioSemantic.ExpenseRed
                    spending > 500 -> cs.tertiary
                    else -> cs.primary
                }

                Box(
                    modifier = Modifier
                        .padding(top = CashioSpacing.xxs)
                        .size(CashioSpacing.xs)
                        .background(dotColor, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun rememberHistoryCalendarStateAndWeekDays(
    today: LocalDate
): Pair<CalendarState, List<DayOfWeek>> {
    val currentMonth = remember(today) { YearMonth.from(today) }
    val startMonth = remember(currentMonth) { currentMonth.minusMonths(12) }
    val endMonth = remember(currentMonth) { currentMonth.plusMonths(1) }
    val weekDays = remember { daysOfWeek() }

    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = weekDays.first()
    )

    return calendarState to weekDays
}
