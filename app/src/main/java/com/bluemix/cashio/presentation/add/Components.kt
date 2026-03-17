//package com.bluemix.cashio.presentation.add
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.FilterChip
//import androidx.compose.material3.FilterChipDefaults
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.OutlinedTextFieldDefaults
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import com.bluemix.cashio.domain.model.Category
//import com.bluemix.cashio.domain.model.TransactionType
//import com.bluemix.cashio.ui.defaults.CashioRadius
//import com.bluemix.cashio.ui.defaults.CashioSpacing
//import com.bluemix.cashio.ui.theme.CashioSemantic
//import com.bluemix.cashio.ui.theme.toComposeColor
//
///* -------------------------------------------------------------------------- */
///* Transaction-type toggle                                                     */
///* -------------------------------------------------------------------------- */
//
///**
// * Stateless row of two mutually-exclusive "pills" for Expense / Income.
// *
// * @param selectedType Currently active type.
// * @param onTypeSelected Callback with the newly tapped type.
// */
//@Composable
//fun TransactionTypeRow(
//    selectedType: TransactionType,
//    onTypeSelected: (TransactionType) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Row(
//        modifier = modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
//    ) {
//        TypePill(
//            text = "Expense",
//            selected = selectedType == TransactionType.EXPENSE,
//            color = CashioSemantic.ExpenseRed,
//            onClick = { onTypeSelected(TransactionType.EXPENSE) },
//            modifier = Modifier.weight(1f)
//        )
//        TypePill(
//            text = "Income",
//            selected = selectedType == TransactionType.INCOME,
//            color = CashioSemantic.IncomeGreen,
//            onClick = { onTypeSelected(TransactionType.INCOME) },
//            modifier = Modifier.weight(1f)
//        )
//    }
//}
//
//@Composable
//private fun TypePill(
//    text: String,
//    selected: Boolean,
//    color: Color,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Surface(
//        modifier = modifier,
//        shape = RoundedCornerShape(CashioRadius.pill),
//        color = if (selected) color.copy(alpha = 0.18f)
//        else MaterialTheme.colorScheme.surfaceVariant,
//        onClick = onClick
//    ) {
//        Box(
//            modifier = Modifier.padding(vertical = CashioSpacing.medium),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = text,
//                style = MaterialTheme.typography.labelLarge,
//                fontWeight = FontWeight.SemiBold,
//                color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//    }
//}
//
///* -------------------------------------------------------------------------- */
///* Text inputs                                                                 */
///* -------------------------------------------------------------------------- */
//
///**
// * Amount text-field with a coloured currency prefix
// * that switches between expense-red and income-green.
// */
//@Composable
//fun AmountField(
//    amount: String,
//    transactionType: TransactionType,
//    onAmountChange: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val accent = if (transactionType == TransactionType.EXPENSE)
//        CashioSemantic.ExpenseRed else CashioSemantic.IncomeGreen
//
//    OutlinedTextField(
//        value = amount,
//        onValueChange = onAmountChange,
//        modifier = modifier.fillMaxWidth(),
//        label = { Text("Amount *") },
//        prefix = {
//            Text(
//                text = "₹ ",
//                fontWeight = FontWeight.Bold,
//                color = accent
//            )
//        },
//        colors = OutlinedTextFieldDefaults.colors(
//            focusedBorderColor = accent,
//            unfocusedBorderColor = MaterialTheme.colorScheme.outline
//        ),
//        shape = RoundedCornerShape(CashioRadius.small)
//    )
//}
//
///** Simple title text-field. */
//@Composable
//fun TitleField(
//    title: String,
//    onTitleChange: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    OutlinedTextField(
//        value = title,
//        onValueChange = onTitleChange,
//        modifier = modifier.fillMaxWidth(),
//        label = { Text("Title *") },
//        shape = RoundedCornerShape(CashioRadius.small)
//    )
//}
//
///** Multi-line note text-field. */
//@Composable
//fun NoteField(
//    note: String,
//    onNoteChange: (String) -> Unit,
//    minLines: Int = AddExpenseDefaults.MinNoteLines,
//    modifier: Modifier = Modifier
//) {
//    OutlinedTextField(
//        value = note,
//        onValueChange = onNoteChange,
//        modifier = modifier.fillMaxWidth(),
//        label = { Text("Note") },
//        minLines = minLines,
//        shape = RoundedCornerShape(CashioRadius.small)
//    )
//}
//
///* -------------------------------------------------------------------------- */
///* Category chips                                                              */
///* -------------------------------------------------------------------------- */
//
///**
// * Horizontal list of filter-chips representing categories.
// *
// * Each chip is keyed by [Category.id] and memoizes its colour
// * conversion so recomposition stays cheap.
// */
//@Composable
//fun CategoryChipRow(
//    categories: List<Category>,
//    selectedCategoryId: String?,
//    onCategorySelected: (Category) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    LazyRow(
//        modifier = modifier,
//        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.compact)
//    ) {
//        items(categories, key = { it.id }) { category ->
//            val selected = category.id == selectedCategoryId
//            val categoryColor = remember(category.colorHex) {
//                category.colorHex.toComposeColor()
//            }
//            FilterChip(
//                selected = selected,
//                onClick = { onCategorySelected(category) },
//                label = { Text(category.name) },
//                leadingIcon = { Text(category.icon) },
//                colors = FilterChipDefaults.filterChipColors(
//                    selectedContainerColor = categoryColor.copy(alpha = 0.18f),
//                    selectedLabelColor = categoryColor
//                ),
//                shape = RoundedCornerShape(CashioRadius.pill)
//            )
//        }
//    }
//}

package com.bluemix.cashio.presentation.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.presentation.common.pressScale
import com.bluemix.cashio.ui.defaults.CashioBorder
import com.bluemix.cashio.ui.defaults.CashioRadius
import com.bluemix.cashio.ui.defaults.CashioSpacing
import com.bluemix.cashio.ui.theme.CashioSemantic
import com.bluemix.cashio.ui.theme.toComposeColor

/* -------------------------------------------------------------------------- */
/* Transaction-type toggle                                                     */
/* -------------------------------------------------------------------------- */

@Composable
fun TransactionTypeRow(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.sm)
    ) {
        TypePill(
            text = "Expense",
            selected = selectedType == TransactionType.EXPENSE,
            color = CashioSemantic.ExpenseRed,
            onClick = { onTypeSelected(TransactionType.EXPENSE) },
            modifier = Modifier.weight(1f)
        )
        TypePill(
            text = "Income",
            selected = selectedType == TransactionType.INCOME,
            color = CashioSemantic.IncomeGreen,
            onClick = { onTypeSelected(TransactionType.INCOME) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TypePill(
    text: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.pressScale(onClick = onClick),
        shape = RoundedCornerShape(CashioRadius.pill),
        color = if (selected) color.copy(alpha = 0.14f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        border = if (selected) null else CashioBorder.stroke(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.padding(vertical = CashioSpacing.sm),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Text inputs                                                                 */
/* -------------------------------------------------------------------------- */

@Composable
fun AmountField(
    amount: String,
    transactionType: TransactionType,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (transactionType == TransactionType.EXPENSE)
        CashioSemantic.ExpenseRed else CashioSemantic.IncomeGreen

    OutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text("Amount *") },
        prefix = {
            Text(
                text = "₹ ",
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accent,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(CashioRadius.small)
    )
}

@Composable
fun TitleField(
    title: String,
    onTitleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text("Title *") },
        shape = RoundedCornerShape(CashioRadius.small)
    )
}

@Composable
fun NoteField(
    note: String,
    onNoteChange: (String) -> Unit,
    minLines: Int = AddExpenseDefaults.MinNoteLines,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text("Note") },
        minLines = minLines,
        shape = RoundedCornerShape(CashioRadius.small)
    )
}

/* -------------------------------------------------------------------------- */
/* Category chips                                                              */
/* -------------------------------------------------------------------------- */

@Composable
fun CategoryChipRow(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.xs)
    ) {
        items(categories, key = { it.id }) { category ->
            val selected = category.id == selectedCategoryId
            val categoryColor = remember(category.colorHex) {
                category.colorHex.toComposeColor()
            }
            FilterChip(
                selected = selected,
                onClick = { onCategorySelected(category) },
                label = { Text(category.name) },
                leadingIcon = { Text(category.icon) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = categoryColor.copy(alpha = 0.14f),
                    selectedLabelColor = categoryColor
                ),
                shape = RoundedCornerShape(CashioRadius.pill)
            )
        }
    }
}
