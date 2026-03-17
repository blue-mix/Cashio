//package com.bluemix.cashio.presentation.home
//
//import androidx.compose.animation.AnimatedContent
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.slideInVertically
//import androidx.compose.animation.slideOutVertically
//import androidx.compose.animation.togetherWith
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ChevronRight
//import androidx.compose.material.icons.filled.TrendingDown
//import androidx.compose.material.icons.filled.TrendingUp
//import androidx.compose.material.icons.filled.Wallet
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.bluemix.cashio.core.format.CashioFormat
//import com.bluemix.cashio.domain.model.Currency
//import com.bluemix.cashio.ui.defaults.CashioCard
//import com.bluemix.cashio.ui.defaults.CashioPadding
//import com.bluemix.cashio.ui.defaults.CashioRadius
//import com.bluemix.cashio.ui.defaults.CashioSpacing
//import com.bluemix.cashio.ui.theme.CashioSemantic
//import kotlinx.coroutines.delay
//
//private object MonthSpendDefaults {
//    val TonalElevation = 4.dp
//    const val SurfaceAlpha = 0.25f
//}
//
//private object AnimationDefaults {
//    const val ItemDelayMs = 50L
//}
//
///* -------------------------------------------------------------------------- */
///* Month-spend hero card                                                       */
///* -------------------------------------------------------------------------- */
//
///**
// * Hero card at the top of the dashboard showing total monthly spending
// * with a trend indicator versus the previous month.
// *
// * All monetary values in **paise**.
// */
//@Composable
//fun MonthSpendCard(
//    amountPaise: Long,
//    percentageChange: Float,
//    isIncrease: Boolean,
//    currency: Currency = Currency.INR,
//    modifier: Modifier = Modifier
//) {
//    val percentageText = remember(percentageChange, isIncrease) {
//        val formatted = String.format("%.0f", percentageChange)
//        "$formatted% ${if (isIncrease) "above" else "below"} last month"
//    }
//
//    val deltaColor = if (isIncrease) CashioSemantic.ExpenseRed else CashioSemantic.IncomeGreen
//    val icon = if (isIncrease) Icons.Default.TrendingUp else Icons.Default.TrendingDown
//
//    Surface(
//        shape = RoundedCornerShape(CashioRadius.large),
//        color = MaterialTheme.colorScheme.surface.copy(alpha = MonthSpendDefaults.SurfaceAlpha),
//        tonalElevation = MonthSpendDefaults.TonalElevation,
//        modifier = modifier.fillMaxWidth()
//    ) {
//        Column(
//            modifier = Modifier.padding(CashioPadding.card),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = "This Month Spend",
//                style = MaterialTheme.typography.labelLarge,
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
//            )
//
//            Spacer(Modifier.height(CashioSpacing.small))
//
//            AnimatedContent(
//                targetState = amountPaise,
//                transitionSpec = {
//                    (slideInVertically { it } + fadeIn())
//                        .togetherWith(slideOutVertically { -it } + fadeOut())
//                },
//                label = "monthSpendAmount"
//            ) { target ->
//                Text(
//                    text = CashioFormat.money(target, currency),
//                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//            }
//
//            Spacer(Modifier.height(CashioSpacing.small))
//
//            // Trend pill
//            Surface(
//                shape = RoundedCornerShape(CashioRadius.pill),
//                color = deltaColor.copy(alpha = 0.12f)
//            ) {
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(CashioSpacing.tiny),
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.padding(
//                        horizontal = CashioSpacing.compact,
//                        vertical = CashioSpacing.tiny
//                    )
//                ) {
//                    Icon(icon, contentDescription = null, tint = deltaColor, modifier = Modifier.size(16.dp))
//                    Text(
//                        text = percentageText,
//                        style = MaterialTheme.typography.labelMedium,
//                        fontWeight = FontWeight.SemiBold,
//                        color = deltaColor
//                    )
//                }
//            }
//        }
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Spending-wallet card                                                        */
///* -------------------------------------------------------------------------- */
//
///**
// * Compact navigation card showing the current "Spending Wallet" balance.
// */
//@Composable
//fun SpendingWalletCard(
//    balancePaise: Long,
//    currency: Currency = Currency.INR,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    CashioCard(
//        onClick = onClick,
//        modifier = modifier.fillMaxWidth(),
//        cornerRadius = CashioRadius.medium,
//        padding = PaddingValues(CashioPadding.card)
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(Icons.Default.Wallet, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
//                Text("Spending Wallet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
//            }
//
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                AnimatedContent(
//                    targetState = balancePaise,
//                    transitionSpec = {
//                        (slideInVertically { it } + fadeIn())
//                            .togetherWith(slideOutVertically { -it } + fadeOut())
//                    },
//                    label = "walletBalance"
//                ) { target ->
//                    Text(
//                        text = CashioFormat.money(target, currency),
//                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
//                        color = MaterialTheme.colorScheme.onSurface
//                    )
//                }
//                Icon(Icons.Default.ChevronRight, "View details", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
//            }
//        }
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Staggered entrance animation                                                */
///* -------------------------------------------------------------------------- */
//
///**
// * Wraps [content] with a staggered fade-in + slide-up animation
// * based on the item's [index] in a list.
// */
//@Composable
//fun AnimatedTransactionItem(
//    key: Any,
//    index: Int,
//    content: @Composable () -> Unit
//) {
//    var isVisible by remember(key) { mutableStateOf(false) }
//
//    LaunchedEffect(key) {
//        delay(index * AnimationDefaults.ItemDelayMs)
//        isVisible = true
//    }
//
//    AnimatedVisibility(
//        visible = isVisible,
//        enter = fadeIn() + slideInVertically { it / 2 },
//        label = "txItemVisibility"
//    ) {
//        Box(Modifier.fillMaxWidth()) { content() }
//    }
//}

package com.bluemix.cashio.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.bluemix.cashio.domain.model.Currency
import com.bluemix.cashio.presentation.common.pressScale
import com.bluemix.cashio.ui.defaults.CashioBorder
import com.bluemix.cashio.ui.defaults.CashioCard
import com.bluemix.cashio.ui.defaults.CashioPadding
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioSpacing
import com.bluemix.cashio.ui.theme.CashioSemantic
import kotlinx.coroutines.delay

private object AnimationDefaults {
    const val ItemDelayMs = 50L
}

/* -------------------------------------------------------------------------- */
/* Month-spend hero card — FLAT, ExtraBold amount, neon trend pill             */
/* -------------------------------------------------------------------------- */

@Composable
fun MonthSpendCard(
    amountPaise: Long,
    percentageChange: Float,
    isIncrease: Boolean,
    currency: Currency = Currency.INR,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val percentageText = remember(percentageChange, isIncrease) {
        val formatted = String.format("%.0f", percentageChange)
        "$formatted% ${if (isIncrease) "above" else "below"} last month"
    }

    // Neon in dark mode, standard in light
    val deltaColor = if (isIncrease) {
        if (isDark) CashioSemantic.ExpenseRedNeon else CashioSemantic.ExpenseRed
    } else {
        if (isDark) CashioSemantic.IncomeGreenNeon else CashioSemantic.IncomeGreen
    }
    val icon = if (isIncrease) Icons.Default.TrendingUp else Icons.Default.TrendingDown

    Surface(
        shape = RoundedCornerShape(CashioRadius.large),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = CashioBorder.stroke(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(CashioPadding.card),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "This Month Spend",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(CashioSpacing.xs))

            // Hero amount — ExtraBold (set in type scale, reinforced here)
            AnimatedContent(
                targetState = amountPaise,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn())
                        .togetherWith(slideOutVertically { -it } + fadeOut())
                },
                label = "monthSpendAmount"
            ) { target ->
                Text(
                    text = CashioFormat.money(target, currency),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(CashioSpacing.xs))

            // Trend pill — flat, neon colour
            Surface(
                shape = RoundedCornerShape(CashioRadius.pill),
                color = deltaColor.copy(alpha = 0.12f),
                tonalElevation = 0.dp
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CashioSpacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        horizontal = CashioSpacing.xs,
                        vertical = CashioSpacing.xxs
                    )
                ) {
                    Icon(icon, null, tint = deltaColor, modifier = Modifier.size(14.dp))
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

/* -------------------------------------------------------------------------- */
/* Spending-wallet card — flat, press-scale, dense layout                      */
/* -------------------------------------------------------------------------- */

@Composable
fun SpendingWalletCard(
    balancePaise: Long,
    currency: Currency = Currency.INR,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CashioCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = CashioRadius.medium,
        padding = PaddingValues(CashioPadding.card),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Wallet, null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "Spending Wallet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = balancePaise,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn())
                            .togetherWith(slideOutVertically { -it } + fadeOut())
                    },
                    label = "walletBalance"
                ) { target ->
                    Text(
                        text = CashioFormat.money(target, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    Icons.Default.ChevronRight, "View details",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Staggered entrance animation                                                */
/* -------------------------------------------------------------------------- */

@Composable
fun AnimatedTransactionItem(
    key: Any,
    index: Int,
    content: @Composable () -> Unit
) {
    var isVisible by remember(key) { mutableStateOf(false) }
    LaunchedEffect(key) {
        delay(index * AnimationDefaults.ItemDelayMs)
        isVisible = true
    }
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { it / 2 },
        label = "txItemVisibility"
    ) {
        Box(Modifier.fillMaxWidth()) { content() }
    }
}