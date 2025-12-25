package com.bluemix.cashio.presentation.history

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.components.DayTransactionCard
import com.bluemix.cashio.ui.theme.CashioSemantic
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import com.bluemix.cashio.R

private val ScreenPadding = 16.dp
private val ItemVerticalPadding = 4.dp
private val EmptyStateCornerRadius = 16.dp

@Composable
fun HistoryScreen(
    onTransactionClick: (String) -> Unit,
    viewModel: HistoryViewModel = koinViewModel()
) {
    // Lifecycle-aware state collection (prevents work when screen is not visible)
    val state by viewModel.state.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val today = remember { LocalDate.now() }
    val selectedDate = state.selectedDate

    // Calendar bounds
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

    val listState = rememberLazyListState()

    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        HistoryTopBar(
            selectedDate = selectedDate,
            onTodayClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                // Toggles off if already selected (based on your VM behavior)
                viewModel.onDateClicked(LocalDate.now())

                // Optional: scroll list to top for quick access
                scope.launch { listState.animateScrollToItem(0) }
            },
            modifier = Modifier.padding(horizontal = ScreenPadding)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = bottomInset + ScreenPadding
            )
        ) {
            // Sticky calendar header (stays visible while scrolling list)
            stickyHeader {
                CalendarStickyHeader(
                    listState = listState,
                    daysOfWeek = weekDays,
                    calendarState = calendarState,
                    state = state,
                    today = today,
                    selectedDate = selectedDate,
                    onDayClick = { date ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onDateClicked(date)
                    }
                )
            }

            val transactionsByDate = state.filteredTransactions

            when {
                state.isLoading -> {
                    item {
                        CenterMessage(
                            text = "Loading...",
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }

                state.errorMessage != null -> {
                    item {
                        CenterMessage(
                            text = state.errorMessage ?: "Something went wrong",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }

                transactionsByDate.isEmpty() -> {
                    item {
                        EmptyTransactionsState(
                            selectedDate = selectedDate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = ScreenPadding, vertical = 24.dp)
                        )
                    }
                }

                else -> {
                    itemsIndexed(
                        items = transactionsByDate,
                        key = { _, (date, _) -> date.toString() }
                    ) { _, (date, dayTransactions) ->
                        DayTransactionCard(
                            date = date,
                            transactions = dayTransactions,
                            onTransactionClick = { id ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTransactionClick(id)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = ScreenPadding, vertical = ItemVerticalPadding)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sticky calendar header with subtle scroll-based elevation:
 * - Elevation increases after user scrolls the list.
 * - Background shifts from background -> surface for separation.
 */
@Composable
private fun CalendarStickyHeader(
    listState: LazyListState,
    daysOfWeek: List<DayOfWeek>,
    calendarState: com.kizitonwose.calendar.compose.CalendarState,
    state: HistoryState,
    today: LocalDate,
    selectedDate: LocalDate?,
    onDayClick: (LocalDate) -> Unit
) {
    val isScrolled by remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                    listState.firstVisibleItemScrollOffset > 0
        }
    }

    val elevation by animateDpAsState(
        targetValue = if (isScrolled) 6.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
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
                monthHeader = { MonthHeader(daysOfWeek = daysOfWeek) },
                dayContent = { day ->
                    SpendingDay(
                        day = day,
                        spending = state.spendingByDate[day.date] ?: 0.0, // expense-only heatmap
                        isSelected = selectedDate == day.date,
                        isToday = day.date == today,
                        onClick = { onDayClick(day.date) }
                    )
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Top bar showing selected day (or today by default) + a "Today" quick action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryTopBar(
    selectedDate: LocalDate?,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayDate = selectedDate ?: LocalDate.now()

    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = displayDate.toPrettyMonthDay(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )

                Column {
                    Text(
                        text = displayDate.year.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = displayDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        actions = { TodayButton(onClick = onTodayClick) }
    )
}

/**
 * Today action with press animation.
 */
@Composable
private fun TodayButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "TodayButtonScale"
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier.scale(scale),
        interactionSource = interactionSource
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
               painter = painterResource(R.drawable.calendar),
                contentDescription = "Today",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = LocalDate.now().dayOfMonth.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Calendar day with brand-consistent heatmap visuals.
 */
@Composable
private fun SpendingDay(
    day: CalendarDay,
    spending: Double,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val isCurrentMonth = day.position == DayPosition.MonthDate
    val hasSpending = spending > 0
    val cs = MaterialTheme.colorScheme

    // Heat background should be light tint (container-like), not saturated.
    val heatBg: Color? = when {
        !isCurrentMonth -> null
        spending > 1000 -> cs.errorContainer
        spending > 500  -> cs.tertiaryContainer
        spending > 100  -> cs.primaryContainer
        spending > 0    -> cs.secondaryContainer
        else -> null
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

            // Small dot indicator for spending days (skip when selected for clarity)
            if (hasSpending && !isSelected && isCurrentMonth) {
                val dotColor = when {
                    spending > 1000 -> CashioSemantic.ExpenseRed // ✅ new naming
                    spending > 500  -> cs.tertiary
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

/**
 * Calendar month header (day-of-week row).
 */
@Composable
private fun MonthHeader(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        daysOfWeek.forEach { dow ->
            Text(
                text = dow.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).take(1),
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
private fun EmptyTransactionsState(
    selectedDate: LocalDate?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(EmptyStateCornerRadius),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "📭", fontSize = 48.sp)
            Text(
                text = if (selectedDate != null) "No transactions on this day" else "No transactions yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (selectedDate != null) {
                    "Select another date or add a new transaction"
                } else {
                    "Your transaction history will appear here"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CenterMessage(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        textAlign = TextAlign.Center,
        color = color
    )
}

/* -------------------------------- Helpers -------------------------------- */

private fun LocalDate.toPrettyMonthDay(): String {
    val month = this.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    return "$month $dayOfMonth${dayOfMonth.ordinalSuffix()}"
}

private fun Int.ordinalSuffix(): String = when {
    this in 11..13 -> "th"
    this % 10 == 1 -> "st"
    this % 10 == 2 -> "nd"
    this % 10 == 3 -> "rd"
    else -> "th"
}
