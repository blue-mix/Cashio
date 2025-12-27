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
import com.bluemix.cashio.ui.theme.CashioSemantic
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

private val ScreenPadding = 16.dp

@Composable
fun CalendarStickyHeader(
    listState: LazyListState,
    expenseTotalByDate: Map<LocalDate, Double>,
    expenseHeatLevelByDate: Map<LocalDate, Int>,
    today: LocalDate,
    selectedDate: LocalDate?,
    onDayClick: (LocalDate) -> Unit
) {
    val (calendarState, weekDays) = rememberHistoryCalendarStateAndWeekDays()

    val isScrolled by remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    // Finance UX: tight + controlled (no bouncy elevation)
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = containerColor,
        tonalElevation = elevation,
        shadowElevation = elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenPadding)
        ) {
            HorizontalCalendar(
                state = calendarState,
                monthHeader = { MonthHeader(weekDays) },
                dayContent = { day ->
                    val spending = expenseTotalByDate[day.date] ?: 0.0
                    val heat = expenseHeatLevelByDate[day.date] ?: 0

                    SpendingDay(
                        day = day,
                        spending = spending,
                        heatLevel = heat,
                        isSelected = selectedDate == day.date,
                        isToday = day.date == today,
                        onClick = { onDayClick(day.date) }
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun rememberHistoryCalendarStateAndWeekDays():
        Pair<com.kizitonwose.calendar.compose.CalendarState, List<DayOfWeek>> {
    val currentMonth = remember { YearMonth.now() }
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

@Composable
private fun MonthHeader(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        val locale = Locale.getDefault()
        daysOfWeek.forEach { dow ->
            Text(
                text = dow.getDisplayName(TextStyle.NARROW, locale), // better than take(1)
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
    onClick: () -> Unit
) {
    val isCurrentMonth = day.position == DayPosition.MonthDate
    val hasSpending = spending > 0
    val cs = MaterialTheme.colorScheme

    // ✅ heatLevel-based background (no magic numbers)
    val heatBg: Color? = when {
        !isCurrentMonth || heatLevel == 0 -> null
        heatLevel == 4 -> cs.errorContainer
        heatLevel == 3 -> cs.tertiaryContainer
        heatLevel == 2 -> cs.primaryContainer
        else -> cs.secondaryContainer // heatLevel == 1
    }

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
            .padding(2.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = isCurrentMonth, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = when {
                    isSelected -> FontWeight.ExtraBold
                    isToday && isCurrentMonth -> FontWeight.Bold
                    else -> FontWeight.SemiBold
                },
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
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .background(dotColor, CircleShape)
                )
            }
        }
    }
}
