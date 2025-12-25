package com.bluemix.cashio.presentation.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.R
import com.bluemix.cashio.components.CashioCard
import com.bluemix.cashio.components.CashioTopBar
import com.bluemix.cashio.components.CashioTopBarTitle
import com.bluemix.cashio.components.TopBarAction
import com.bluemix.cashio.components.TopBarIcon
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.ui.theme.CashioSemantic.ExpenseRed
import com.bluemix.cashio.ui.theme.CashioSemantic.IncomeGreen
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    expenseId: String? = null,
    viewModel: AddExpenseViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
    onNavigateCategory: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(expenseId) {
        if (expenseId != null) {
            viewModel.loadExpenseForEdit(expenseId)
        } else {
            viewModel.resetForm()
        }
    }


    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(80)
            viewModel.consumeSaveSuccess()
            onNavigateBack()
        }
    }

    var showMoreDetails by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        CashioTopBar(
            title = CashioTopBarTitle.Text( if (state.isEditMode) "Edit Transaction" else "Add Transaction",),
            leadingAction = TopBarAction(  icon = TopBarIcon.Vector(Icons.Default.Close),onNavigateBack) ,
            trailingAction = TopBarAction(  icon = TopBarIcon.Drawable(R.drawable.edit),onNavigateCategory) ,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = navBarPadding + 16.dp
            )
        ) {
            item {
                SectionCard {
                    Text(
                        text = "Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    TransactionTypeRow(
                        selectedType = state.transactionType,
                        onTypeSelected = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.updateTransactionType(it)
                        }
                    )
                }
            }

            item {
                SectionCard {
                    AmountField(
                        amount = state.amount,
                        transactionType = state.transactionType,
                        onAmountChange = viewModel::updateAmount
                    )
                    Spacer(Modifier.height(12.dp))
                    TitleField(
                        title = state.title,
                        onTitleChange = viewModel::updateTitle
                    )
                }
            }

            item {
                SectionCard {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))

                    when (val categoriesState = state.categories) {
                        is UiState.Loading -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) { CircularProgressIndicator() }
                        }

                        is UiState.Success -> {
                            CategoryRow(
                                categories = categoriesState.data,
                                selectedCategory = state.selectedCategory,
                                onCategorySelected = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.selectCategory(it)
                                }
                            )
                        }

                        is UiState.Error -> {
                            Text(
                                text = categoriesState.message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        else -> Unit
                    }
                }
            }

            item {
                SectionCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showMoreDetails = !showMoreDetails
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "More Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = if (showMoreDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(
                        visible = showMoreDetails,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            DateRow(date = state.date, onClick = { /* hook DatePickerDialog */ })
                            Spacer(Modifier.height(12.dp))
                            NoteField(note = state.note, onNoteChange = viewModel::updateNote)
                        }
                    }
                }
            }

            item {
                val isValid =
                    state.amount.isNotBlank() &&
                            state.title.isNotBlank() &&
                            state.selectedCategory != null

                SaveButton(
                    text = when {
                        state.isSaving -> "Saving..."
                        state.isEditMode -> "Update"
                        else -> "Save"
                    },
                    enabled = isValid && !state.isSaving,
                    isLoading = state.isSaving,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.saveExpense()
                    }
                )
            }
        }
    }

    state.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("OK") }
            }
        )
    }
}

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    CashioCard(
        modifier = modifier.fillMaxWidth(),
        padding = PaddingValues(16.dp),
        cornerRadius = 16.dp,
        showBorder = true
    ) {
        Column(content = content)
    }
}

@Composable
private fun TransactionTypeRow(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TypePill(
            text = "Expense",
            selected = selectedType == TransactionType.EXPENSE,
            color = ExpenseRed,
            onClick = { onTypeSelected(TransactionType.EXPENSE) },
            modifier = Modifier.weight(1f)
        )
        TypePill(
            text = "Income",
            selected = selectedType == TransactionType.INCOME,
            color = IncomeGreen,
            onClick = { onTypeSelected(TransactionType.INCOME) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TypePill(
    text: String,
    selected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "typePillScale"
    )

    Surface(
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
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

@Composable
private fun AmountField(
    amount: String,
    transactionType: TransactionType,
    onAmountChange: (String) -> Unit
) {
    val accent = if (transactionType == TransactionType.EXPENSE) ExpenseRed else IncomeGreen

    OutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Amount *") },
        placeholder = { Text("0.00") },
        prefix = { Text("₹", fontWeight = FontWeight.Bold, color = accent) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = accent,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun TitleField(
    title: String,
    onTitleChange: (String) -> Unit
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Title *") },
        placeholder = { Text("e.g., Grocery Shopping") },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun CategoryRow(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(categories, key = { it.id }) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category.name) },
                leadingIcon = { Text(category.icon) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedContainerColor = category.color.copy(alpha = 0.18f),
                    selectedLabelColor = category.color,
                    selectedLeadingIconColor = category.color
                )
            )
        }
    }
}

@Composable
private fun DateRow(
    date: LocalDateTime,
    onClick: () -> Unit
) {
    val label = remember(date) {
        date.format(DateTimeFormatter.ofPattern("dd MMM, yyyy"))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null)
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoteField(
    note: String,
    onNoteChange: (String) -> Unit
) {
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Note") },
        placeholder = { Text("Optional") },
        minLines = 3,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun SaveButton(
    text: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "saveScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
        }
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}
