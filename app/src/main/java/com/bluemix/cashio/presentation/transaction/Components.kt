//package com.bluemix.cashio.presentation.transaction.components
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material3.FilterChip
//import androidx.compose.material3.FilterChipDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.OutlinedTextFieldDefaults
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.bluemix.cashio.R
//import com.bluemix.cashio.core.format.CashioFormat
//import com.bluemix.cashio.domain.model.Currency
//import com.bluemix.cashio.domain.model.Expense
//import com.bluemix.cashio.domain.model.TransactionType
//import com.bluemix.cashio.presentation.transaction.TransactionTypeFilter
//import com.bluemix.cashio.ui.defaults.CashioCard
//import com.bluemix.cashio.ui.defaults.CashioRadius
//import com.bluemix.cashio.ui.defaults.CashioSpacing
//import com.bluemix.cashio.ui.theme.CashioSemantic.ExpenseRed
//import com.bluemix.cashio.ui.theme.CashioSemantic.IncomeGreen
//import java.time.format.DateTimeFormatter
//import java.util.Locale
//
///* -------------------------------------------------------------------------- */
///* Header card                                                                 */
///* -------------------------------------------------------------------------- */
//
///**
// * Transaction details header card showing amount and category.
// * All monetary values in **paise**.
// */
//@Composable
//fun TransactionDetailsHeaderCard(
//    tx: Expense,
//    currency: Currency = Currency.INR,
//    modifier: Modifier = Modifier
//) {
//    val color = if (tx.transactionType == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
//
//    CashioCard(
//        modifier = modifier.fillMaxWidth(),
//        padding = PaddingValues(CashioSpacing.default),
//        cornerRadius = CashioRadius.medium,
//        showBorder = true
//    ) {
//        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.compact)) {
//            Text(tx.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
//            Text(
//                text = CashioFormat.money(tx.amountPaise, currency),
//                style = MaterialTheme.typography.headlineMedium,
//                fontWeight = FontWeight.ExtraBold,
//                color = color
//            )
//            Text(
//                text = "${tx.category.icon}  ${tx.category.name}",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Info card                                                                   */
///* -------------------------------------------------------------------------- */
//
///** Transaction metadata rows (Type, Category, Date, etc.) */
//@Composable
//fun TransactionDetailsInfoCard(
//    tx: Expense,
//    modifier: Modifier = Modifier
//) {
//    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
//
//    val rows = buildList {
//        add("Type" to if (tx.transactionType == TransactionType.EXPENSE) "Expense" else "Income")
//        add("Category" to "${tx.category.icon}  ${tx.category.name}")
//        add("Date" to tx.date.format(dateFormatter))
//        if (tx.note.isNotBlank()) add("Note" to tx.note)
//        if (tx.merchantName?.isNotBlank() == true) add("Merchant" to tx.merchantName)
//        add("Source" to tx.source.name)
//    }
//
//    CashioCard(
//        modifier = modifier.fillMaxWidth(),
//        padding = PaddingValues(CashioSpacing.default),
//        cornerRadius = CashioRadius.medium,
//        showBorder = true
//    ) {
//        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.medium)) {
//            Text("Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
//            rows.forEach { (k, v) ->
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.Top
//                ) {
//                    Text(k, color = MaterialTheme.colorScheme.onSurfaceVariant)
//                    Text(v, fontWeight = FontWeight.Medium)
//                }
//            }
//        }
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Search bar                                                                  */
///* -------------------------------------------------------------------------- */
//
//@Composable
//fun TransactionSearchBar(
//    query: String,
//    onQueryChange: (String) -> Unit,
//    onClear: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    OutlinedTextField(
//        value = query,
//        onValueChange = onQueryChange,
//        modifier = modifier.fillMaxWidth(),
//        singleLine = true,
//        shape = RoundedCornerShape(CashioRadius.small),
//        leadingIcon = {
//            Icon(
//                painter = painterResource(R.drawable.search),
//                contentDescription = null,
//                modifier = Modifier.size(20.dp)
//            )
//        },
//        trailingIcon = {
//            AnimatedVisibility(visible = query.isNotBlank()) {
//                IconButton(onClick = onClear) {
//                    Icon(
//                        imageVector = Icons.Default.Close,
//                        contentDescription = "Clear",
//                        modifier = Modifier.size(20.dp),
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//        },
//        placeholder = { Text("Search title, category, amount…") },
//        colors = OutlinedTextFieldDefaults.colors(
//            focusedBorderColor = MaterialTheme.colorScheme.primary,
//            unfocusedBorderColor = MaterialTheme.colorScheme.outline
//        )
//    )
//}
//
///* -------------------------------------------------------------------------- */
///* Type filter row                                                             */
///* -------------------------------------------------------------------------- */
//
//@Composable
//fun TransactionTypeFilterRow(
//    filter: TransactionTypeFilter,
//    onChange: (TransactionTypeFilter) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Row(
//        modifier = modifier,
//        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.compact)
//    ) {
//        TransactionTypeFilter.entries.forEach { entry ->
//            FilterChip(
//                selected = filter == entry,
//                onClick = { onChange(entry) },
//                label = { Text(entry.name.lowercase().replaceFirstChar { it.uppercase() }) },
//                colors = if (entry == TransactionTypeFilter.ALL) {
//                    FilterChipDefaults.filterChipColors(
//                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
//                        selectedLabelColor = MaterialTheme.colorScheme.primary
//                    )
//                } else FilterChipDefaults.filterChipColors()
//            )
//        }
//    }
//}
package com.bluemix.cashio.presentation.transaction.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.R
import com.bluemix.cashio.core.format.CashioFormat
import com.bluemix.cashio.domain.model.Currency
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.presentation.transaction.TransactionTypeFilter
import com.bluemix.cashio.ui.defaults.CashioCard
import com.bluemix.cashio.ui.defaults.CashioPadding
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioSpacing
import com.bluemix.cashio.ui.theme.CashioSemantic
import java.time.format.DateTimeFormatter
import java.util.Locale

/* -------------------------------------------------------------------------- */
/* Header card — ExtraBold amount, neon semantics, squircle                    */
/* -------------------------------------------------------------------------- */

@Composable
fun TransactionDetailsHeaderCard(
    tx: Expense,
    currency: Currency = Currency.INR,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val color = if (tx.transactionType == TransactionType.EXPENSE) {
        if (isDark) CashioSemantic.ExpenseRedNeon else CashioSemantic.ExpenseRed
    } else {
        if (isDark) CashioSemantic.IncomeGreenNeon else CashioSemantic.IncomeGreen
    }

    CashioCard(
        modifier = modifier.fillMaxWidth(),
        padding = PaddingValues(CashioPadding.card),
        cornerRadius = CashioRadius.medium,
        showBorder = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.xs)) {
            Text(
                tx.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = CashioFormat.money(tx.amountPaise, currency),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = "${tx.category.icon}  ${tx.category.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Info card — flat, dense rows                                                */
/* -------------------------------------------------------------------------- */

@Composable
fun TransactionDetailsInfoCard(
    tx: Expense,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    val rows = buildList {
        add("Type" to if (tx.transactionType == TransactionType.EXPENSE) "Expense" else "Income")
        add("Category" to "${tx.category.icon}  ${tx.category.name}")
        add("Date" to tx.date.format(dateFormatter))
        if (tx.note.isNotBlank()) add("Note" to tx.note)
        if (tx.merchantName?.isNotBlank() == true) add("Merchant" to tx.merchantName)
        add("Source" to tx.source.name)
    }

    CashioCard(
        modifier = modifier.fillMaxWidth(),
        padding = PaddingValues(CashioPadding.card),
        cornerRadius = CashioRadius.medium,
        showBorder = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(CashioSpacing.sm)) {
            Text(
                "Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            rows.forEach { (k, v) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(k, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(v, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Search bar                                                                  */
/* -------------------------------------------------------------------------- */

@Composable
fun TransactionSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(CashioRadius.small),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            AnimatedVisibility(visible = query.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        placeholder = { Text("Search title, category, amount…") },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

/* -------------------------------------------------------------------------- */
/* Type filter row                                                             */
/* -------------------------------------------------------------------------- */

@Composable
fun TransactionTypeFilterRow(
    filter: TransactionTypeFilter,
    onChange: (TransactionTypeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.xs)
    ) {
        TransactionTypeFilter.entries.forEach { entry ->
            FilterChip(
                selected = filter == entry,
                onClick = { onChange(entry) },
                label = { Text(entry.name.lowercase().replaceFirstChar { it.uppercase() }) },
                colors = if (entry == TransactionTypeFilter.ALL) {
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                } else FilterChipDefaults.filterChipColors(),
                shape = RoundedCornerShape(CashioRadius.pill)
            )
        }
    }
}