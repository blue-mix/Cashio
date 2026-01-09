package com.bluemix.cashio.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.core.format.CashioFormat
import com.bluemix.cashio.ui.components.defaults.CashioCard
import com.bluemix.cashio.ui.components.defaults.CashioPadding
import com.bluemix.cashio.ui.components.defaults.CashioRadius
import com.bluemix.cashio.ui.components.defaults.CashioSpacing
import com.bluemix.cashio.ui.theme.CashioSemantic
import kotlinx.coroutines.delay

/**
 * A hero card displayed at the top of the dashboard.
 *
 * Visualizes the total amount spent in the current month alongside a
 * trend indicator comparing it to the previous month's spending.
 *
 * @param amount The total expense amount for the current month.
 * @param percentageChange The delta percentage (0-100+) vs last month.
 * @param isIncrease True if spending is higher than last month (triggers red/bad), false if lower (green/good).
 */
@Composable
fun MonthSpendCard(
    amount: Double,
    percentageChange: Float,
    isIncrease: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(CashioRadius.large),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.25f),
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(CashioPadding.card),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "This Month Spend",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(CashioSpacing.small))

            // Animate the number when it changes (e.g., after adding a new expense)
            AnimatedContent(
                targetState = amount,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()).togetherWith(
                        slideOutVertically { -it } + fadeOut()
                    )
                },
                label = "monthSpendAmount"
            ) { targetAmount ->
                Text(
                    text = CashioFormat.money(targetAmount),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(CashioSpacing.small))

            val percentageText = remember(percentageChange, isIncrease) {
                val formatted = String.format("%.0f", percentageChange)
                "$formatted% ${if (isIncrease) "above" else "below"} last month"
            }

            // In financial context: Increase in spending = Bad (Red), Decrease = Good (Green)
            val deltaColor =
                if (isIncrease) CashioSemantic.ExpenseRed else CashioSemantic.IncomeGreen
            val icon = if (isIncrease) Icons.Default.TrendingUp else Icons.Default.TrendingDown

            Surface(
                shape = RoundedCornerShape(CashioRadius.pill),
                color = deltaColor.copy(alpha = 0.12f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CashioSpacing.tiny),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        horizontal = CashioSpacing.compact,
                        vertical = CashioSpacing.tiny
                    )
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = deltaColor,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = percentageText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = deltaColor
                    )
                }
            }
        }
    }
}

/**
 * A compact navigation card showing the user's current "Spending Wallet" balance.
 * Clicking this navigates to the detailed Wallet/History screen.
 */
@Composable
fun SpendingWalletCard(
    balance: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CashioCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        cornerRadius = CashioRadius.medium,
        padding = PaddingValues(CashioPadding.card)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.huge),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Wallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Spending Wallet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = balance,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()).togetherWith(
                            slideOutVertically { -it } + fadeOut()
                        )
                    },
                    label = "walletBalance"
                ) { targetBalance ->
                    Text(
                        text = CashioFormat.money(targetBalance),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * A utility wrapper that creates a staggered entrance animation for list items.
 *
 * @param key Unique identifier for the item (ensures animation runs only once per item).
 * @param index The position index in the list, used to calculate the start delay.
 * @param content The composable content to animate.
 */
@Composable
fun AnimatedTransactionItem(
    key: Any,
    index: Int,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key) {
        // Stagger effect: 50ms delay per item index
        delay(index * 50L)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { it / 2 },
        label = "txItemVisibility"
    ) {
        Box(modifier = Modifier.fillMaxWidth()) { content() }
    }
}