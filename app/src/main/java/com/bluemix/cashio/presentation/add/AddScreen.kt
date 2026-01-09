package com.bluemix.cashio.presentation.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.core.format.CashioFormat.toDateLabel
import com.bluemix.cashio.core.format.CashioFormat.toEpochMillis
import com.bluemix.cashio.core.format.CashioFormat.toLocalDate
import com.bluemix.cashio.core.format.CashioFormat.toTimeLabel
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.presentation.common.UiState
import com.bluemix.cashio.ui.components.defaults.CashioCard
import com.bluemix.cashio.ui.components.defaults.CashioPadding
import com.bluemix.cashio.ui.components.defaults.CashioRadius
import com.bluemix.cashio.ui.components.defaults.CashioShapes
import com.bluemix.cashio.ui.components.defaults.CashioSpacing
import com.bluemix.cashio.ui.components.defaults.CashioTopBar
import com.bluemix.cashio.ui.components.defaults.CashioTopBarTitle
import com.bluemix.cashio.ui.components.defaults.TopBarAction
import com.bluemix.cashio.ui.components.defaults.TopBarIcon
import com.bluemix.cashio.ui.theme.CashioSemantic
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Screen for creating a new transaction or editing an existing one.
 *
 * This screen operates in two modes based on the [expenseId] argument:
 * 1. **Create Mode (null ID):** Starts with a blank form.
 * 2. **Edit Mode (valid ID):** Pre-fills the form with existing data via the ViewModel.
 *
 * Key Features:
 * - Haptic feedback integration for user interactions.
 * - Collapsible "More Details" section to keep the UI clean.
 * - Custom Date and Time pickers bridging Java Time with Material 3.
 *
 * @param onNavigateBack Callback to finish the screen.
 * @param onNavigateToCategories Callback to open the Category management screen.
 * @param expenseId If provided, triggers Edit Mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategories: () -> Unit,
    expenseId: String? = null,
    viewModel: AddExpenseViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Initialize mode (Add vs Edit)
    LaunchedEffect(expenseId) {
        if (expenseId != null) viewModel.loadExpenseForEdit(expenseId)
        else viewModel.resetForm()
    }

    // Handle successful save navigation
    LaunchedEffect(state.saveSuccess) {
        if (!state.saveSuccess) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        delay(80) // Slight delay to allow ripple animation
        viewModel.consumeSaveSuccess()
        onNavigateBack()
    }

    var showMoreDetails by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        CashioTopBar(
            title = CashioTopBarTitle.Text(
                if (state.isEditMode) "Edit Transaction" else "Add Transaction"
            ),
            leadingAction = TopBarAction(
                icon = TopBarIcon.Vector(Icons.Default.Close),
                onClick = onNavigateBack
            ),
            modifier = Modifier.padding(horizontal = CashioPadding.screen)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(CashioSpacing.default),
            contentPadding = PaddingValues(
                start = CashioPadding.screen,
                end = CashioPadding.screen,
                top = CashioSpacing.small,
                bottom = navBarPadding + CashioSpacing.default
            )
        ) {
            // Section: Transaction Type (Income/Expense)
            item {
                SectionCard {
                    Text(
                        text = "Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(CashioSpacing.medium))
                    TransactionTypeRow(
                        selectedType = state.transactionType,
                        onTypeSelected = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.updateTransactionType(it)
                        }
                    )
                }
            }

            // Section: Main Details (Amount & Title)
            item {
                SectionCard {
                    AmountField(
                        amount = state.amount,
                        transactionType = state.transactionType,
                        onAmountChange = viewModel::updateAmount
                    )
                    Spacer(Modifier.height(CashioSpacing.medium))
                    TitleField(
                        title = state.title,
                        onTitleChange = viewModel::updateTitle
                    )
                }
            }

            // Section: Category Selection
            item {
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = onNavigateToCategories) { Text("Manage") }
                    }

                    Spacer(Modifier.height(CashioSpacing.medium))

                    when (val categoriesState = state.categories) {
                        UiState.Loading -> {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }

                        is UiState.Success -> {
                            CategoryRow(
                                categories = categoriesState.data,
                                selectedCategoryId = state.selectedCategory?.id,
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

            // Section: Optional Details (Date, Time, Note)
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
                        Column(modifier = Modifier.padding(top = CashioSpacing.medium)) {
                            DetailPickerRow(
                                label = "Date",
                                value = state.date.toDateLabel(),
                                icon = Icons.Default.CalendarToday,
                                onClick = { showDatePicker = true }
                            )
                            Spacer(Modifier.height(CashioSpacing.medium))
                            DetailPickerRow(
                                label = "Time",
                                value = state.date.toTimeLabel(),
                                icon = Icons.Default.Schedule,
                                onClick = { showTimePicker = true }
                            )
                            Spacer(Modifier.height(CashioSpacing.medium))
                            NoteField(note = state.note, onNoteChange = viewModel::updateNote)
                        }
                    }
                }
            }

            // Save Button
            item {
                val isValid =
                    state.amount.isNotBlank() && state.title.isNotBlank() && state.selectedCategory != null
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

    if (showDatePicker) {
        CashioDatePickerDialog(
            initialDateTime = state.date,
            onDismiss = { showDatePicker = false },
            onConfirm = { newDate ->
                viewModel.updateDate(LocalDateTime.of(newDate, state.date.toLocalTime()))
                showDatePicker = false
            }
        )
    }

    if (showTimePicker) {
        CashioTimePickerDialog(
            initialDateTime = state.date,
            onDismiss = { showTimePicker = false },
            onConfirm = { newTime ->
                viewModel.updateDate(LocalDateTime.of(state.date.toLocalDate(), newTime))
                showTimePicker = false
            }
        )
    }
}

/* -------------------------------------------------------------------------- */
/* Internal UI Components                                                      */
/* -------------------------------------------------------------------------- */

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    CashioCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(CashioPadding.card),
        cornerRadius = CashioShapes.card,
        showBorder = true,
        content = { Column(content = content) }
    )
}

@Composable
private fun TransactionTypeRow(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CashioSpacing.medium)
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
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(CashioRadius.pill),
        color = if (selected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.padding(vertical = CashioSpacing.medium),
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
private fun DetailPickerRow(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(CashioRadius.small),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CashioSpacing.mediumLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(CashioSpacing.compact),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val accent =
        if (transactionType == TransactionType.EXPENSE) CashioSemantic.ExpenseRed else CashioSemantic.IncomeGreen
    OutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Amount *") },
        prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = accent) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accent,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(CashioRadius.small)
    )
}

@Composable
private fun TitleField(title: String, onTitleChange: (String) -> Unit) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Title *") },
        shape = RoundedCornerShape(CashioRadius.small)
    )
}

@Composable
private fun CategoryRow(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (Category) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(CashioSpacing.compact)) {
        items(categories, key = { it.id }) { category ->
            val selected = category.id == selectedCategoryId
            FilterChip(
                selected = selected,
                onClick = { onCategorySelected(category) },
                label = { Text(category.name) },
                leadingIcon = { Text(category.icon) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = category.color.copy(alpha = 0.18f),
                    selectedLabelColor = category.color
                ),
                shape = RoundedCornerShape(CashioRadius.pill)
            )
        }
    }
}

@Composable
private fun NoteField(note: String, onNoteChange: (String) -> Unit) {
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Note") },
        minLines = 3,
        shape = RoundedCornerShape(CashioRadius.small)
    )
}

@Composable
private fun SaveButton(text: String, enabled: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(CashioRadius.mediumSmall),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        if (isLoading) CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp
        )
        else Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

/* -------------------------------------------------------------------------- */
/* Premium Pickers (Wrappers)                                                  */
/* -------------------------------------------------------------------------- */

/**
 * A wrapper around Material 3 [DatePickerDialog] that bridges
 * standard [LocalDate] with the picker's millisecond-based state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashioDatePickerDialog(
    initialDateTime: LocalDateTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = initialDateTime.toEpochMillis())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    datePickerState.selectedDateMillis?.toLocalDate() ?: LocalDate.now()
                )
            }) { Text("Done") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) { DatePicker(state = datePickerState) }
}

/**
 * A wrapper around Material 3 [TimePicker] that bridges
 * standard [LocalTime] with the picker's hour/minute state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashioTimePickerDialog(
    initialDateTime: LocalDateTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val timeState = rememberTimePickerState(
        initialHour = initialDateTime.hour,
        initialMinute = initialDateTime.minute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select time") },
        text = { TimePicker(state = timeState) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    LocalTime.of(
                        timeState.hour,
                        timeState.minute
                    )
                )
            }) { Text("Done") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}