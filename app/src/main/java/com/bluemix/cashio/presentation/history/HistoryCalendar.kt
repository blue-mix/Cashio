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
import androidx.compose.material3.ColorScheme
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
import com.bluemix.cashio.ui.defaults.CashioPadding
import com.bluemix.cashio.ui.defaults.CashioSpacing
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

// Constants
private object CalendarDefaults {
    const val ScrollElevation = 6f
    const val NoScrollElevation = 0f
}

private object SpendingThresholds {
    const val HighSpending = 1000.0 // Rupees
    const val MediumSpending = 500.0 // Rupees
}

/**
 * Calendar sticky header with spending heatmap.
 *
 * All monetary values in **paise (Long)**.
 *
 * @param expenseTotalByDatePaise Map of dates to total spending (paise)
 */
@Composable
fun CalendarStickyHeader(
    listState: LazyListState,
    expenseTotalByDatePaise: Map<LocalDate, Long>,
    expenseHeatLevelByDate: Map<LocalDate, Int>,
    today: LocalDate,
    selectedDate: LocalDate?,
    onDayClick: (LocalDate) -> Unit
) {
    val (calendarState, weekDays) = rememberHistoryCalendarStateAndWeekDays(today)

    // Scroll detection
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                    listState.firstVisibleItemScrollOffset > 0
        }
    }

    // Animated elevation
    val elevation by animateDpAsState(
        targetValue = if (isScrolled) CalendarDefaults.ScrollElevation.dp
        else CalendarDefaults.NoScrollElevation.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "CalendarHeaderElevation"
    )

    val containerColor = if (isScrolled)
        MaterialTheme.colorScheme.surface
    else
        MaterialTheme.colorScheme.background

    // Precompute heat palette
    val heatPalette = rememberHeatPalette()

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
                    SpendingDay(
                        day = day,
                        spendingPaise = expenseTotalByDatePaise[day.date] ?: 0L,
                        heatLevel = (expenseHeatLevelByDate[day.date] ?: 0).coerceIn(0, 4),
                        isSelected = selectedDate == day.date,
                        isToday = day.date == today,
                        heatPalette = heatPalette,
                        onClick = { onDayClick(day.date) }
                    )
                }
            )

            Spacer(modifier = Modifier.height(CashioSpacing.sm))
        }
    }
}

@Composable
private fun rememberHeatPalette(): Array<Color> {
    val cs = MaterialTheme.colorScheme
    return remember(cs) {
        arrayOf(
            Color.Transparent,       // 0 (unused)
            cs.secondaryContainer,   // 1 low
            cs.primaryContainer,     // 2 medium
            cs.tertiaryContainer,    // 3 med-high
            cs.errorContainer        // 4 high
        )
    }
}

@Composable
private fun MonthHeader(daysOfWeek: List<DayOfWeek>) {
    val locale = remember { Locale.getDefault() }
    val labels = remember(daysOfWeek, locale) {
        daysOfWeek.map { it.getDisplayName(TextStyle.NARROW, locale) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = CashioSpacing.sm,
                horizontal = CashioSpacing.xs
            )
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

/**
 * Individual calendar day cell with spending indicator.
 *
 * @param spendingPaise Total spending on this day (paise)
 */
@Composable
private fun SpendingDay(
    day: CalendarDay,
    spendingPaise: Long,
    heatLevel: Int,
    isSelected: Boolean,
    isToday: Boolean,
    heatPalette: Array<Color>,
    onClick: () -> Unit
) {
    val isCurrentMonth = day.position == DayPosition.MonthDate
    val hasSpending = spendingPaise > 0L
    val cs = MaterialTheme.colorScheme

    // Determine background color
    val heatBg = if (!isCurrentMonth || heatLevel == 0) null
    else heatPalette[heatLevel]

    val background = when {
        isSelected -> cs.primary
        isToday && isCurrentMonth -> cs.primaryContainer
        else -> heatBg ?: Color.Transparent
    }

    // Determine text color
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
                fontWeight = if (isSelected) FontWeight.ExtraBold
                else FontWeight.SemiBold,
                fontSize = 13.sp,
                color = textColor
            )

            // Spending dot indicator
            if (hasSpending && !isSelected && isCurrentMonth) {
                SpendingDotIndicator(
                    spendingPaise = spendingPaise,
                    colorScheme = cs
                )
            }
        }
    }
}

@Composable
private fun SpendingDotIndicator(
    spendingPaise: Long,
    colorScheme: ColorScheme
) {
    val spendingRupees = spendingPaise / 100.0

    val dotColor = when {
        spendingRupees > SpendingThresholds.HighSpending -> CashioSemantic.ExpenseRed
        spendingRupees > SpendingThresholds.MediumSpending -> colorScheme.tertiary
        else -> colorScheme.primary
    }

    Box(
        modifier = Modifier
            .padding(top = CashioSpacing.xxs)
            .size(CashioSpacing.xs)
            .background(dotColor, CircleShape)
    )
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
