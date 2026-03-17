package com.bluemix.cashio.presentation.history

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.R
import com.bluemix.cashio.core.format.CashioFormat
import com.bluemix.cashio.core.format.CashioFormat.toPrettyMonthDay
import com.bluemix.cashio.ui.components.cards.StateCard
import com.bluemix.cashio.ui.components.cards.StateCardVariant
import com.bluemix.cashio.ui.defaults.CashioSpacing
import java.time.LocalDate
import java.time.format.TextStyle


/* -------------------------------------------------------------------------- */
/* Top Bar                                                                     */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTopBar(
    selectedDate: LocalDate?,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayDate = selectedDate ?: remember { LocalDate.now() }
    val locale = CashioFormat._locale

    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.sm)
            ) {
                Text(
                    text = displayDate.toPrettyMonthDay(locale),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Column {
                    Text(
                        text = displayDate.year.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = displayDate.dayOfWeek.getDisplayName(
                            TextStyle.SHORT,
                            locale
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
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

@Composable
private fun TodayButton(onClick: () -> Unit) {
    val today = remember { LocalDate.now() }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
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
                contentDescription = "Jump to today",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = today.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
fun EmptyTransactionsState(
    selectedDate: LocalDate?,
    modifier: Modifier = Modifier
) {
    StateCard(
        variant = StateCardVariant.EMPTY,
        emoji = "📭",
        title = if (selectedDate != null)
            "No transactions on this day"
        else
            "No transactions yet",
        message = if (selectedDate != null)
            "Select another date or add a new transaction"
        else
            "Your transaction history will appear here",
        modifier = modifier
    )
}