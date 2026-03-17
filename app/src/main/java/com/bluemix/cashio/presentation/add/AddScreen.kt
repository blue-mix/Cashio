//package com.bluemix.cashio.presentation.add
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.asPaddingValues
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.navigationBars
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.statusBarsPadding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.hapticfeedback.HapticFeedbackType
//import androidx.compose.ui.platform.LocalHapticFeedback
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.bluemix.cashio.presentation.common.SectionCard
//import com.bluemix.cashio.ui.defaults.CashioPadding
//import com.bluemix.cashio.ui.defaults.CashioSpacing
//import com.bluemix.cashio.ui.defaults.CashioTopBar
//import com.bluemix.cashio.ui.defaults.CashioTopBarTitle
//import com.bluemix.cashio.ui.defaults.TopBarAction
//import com.bluemix.cashio.ui.defaults.TopBarIcon
//import kotlinx.coroutines.delay
//import org.koin.compose.viewmodel.koinViewModel
//import java.time.LocalDateTime
//
///**
// * Screen for creating a new transaction or editing an existing one.
// *
// * **All ViewModel / NavController logic lives here.**
// * Every child composable is stateless and receives only
// * raw data + lambdas.
// *
// * @param onNavigateBack       Close the screen.
// * @param onNavigateToCategories Open category management.
// * @param expenseId            Non-null triggers Edit Mode.
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddExpenseScreen(
//    onNavigateBack: () -> Unit,
//    onNavigateToCategories: () -> Unit,
//    expenseId: String? = null,
//    viewModel: AddExpenseViewModel = koinViewModel(),
//    modifier: Modifier = Modifier
//) {
//    val state by viewModel.state.collectAsStateWithLifecycle()
//    val haptic = LocalHapticFeedback.current
//    val navBarPadding = WindowInsets.navigationBars
//        .asPaddingValues()
//        .calculateBottomPadding()
//
//    // ── Lifecycle effects ────────────────────────────────────────────────
//    LaunchedEffect(expenseId) {
//        if (expenseId != null) viewModel.loadExpenseForEdit(expenseId)
//        else viewModel.resetForm()
//    }
//
//    LaunchedEffect(state.saveSuccess) {
//        if (!state.saveSuccess) return@LaunchedEffect
//        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
//        delay(AddExpenseDefaults.SaveDelayMs)
//        viewModel.consumeSaveSuccess()
//        onNavigateBack()
//    }
//
//    // ── Local UI state ──────────────────────────────────────────────────
//    var showMoreDetails by rememberSaveable { mutableStateOf(false) }
//    var showDatePicker by rememberSaveable { mutableStateOf(false) }
//    var showTimePicker by rememberSaveable { mutableStateOf(false) }
//
//    // ── Memoized validation ─────────────────────────────────────────────
//    val isFormValid = remember(state.amount, state.title, state.selectedCategory) {
//        state.amount.isNotBlank() &&
//                state.title.isNotBlank() &&
//                state.selectedCategory != null
//    }
//
//    // ── Memoized callbacks ──────────────────────────────────────────────
//    val onTypeSelected = remember(haptic) {
//        { type: com.bluemix.cashio.domain.model.TransactionType ->
//            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//            viewModel.updateTransactionType(type)
//        }
//    }
//
//    val onCategorySelected = remember(haptic) {
//        { cat: com.bluemix.cashio.domain.model.Category ->
//            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//            viewModel.selectCategory(cat)
//        }
//    }
//
//    val onToggleDetails = remember(haptic) {
//        {
//            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//            showMoreDetails = !showMoreDetails
//        }
//    }
//
//    val onSaveClick = remember(haptic) {
//        {
//            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
//            viewModel.saveExpense()
//        }
//    }
//
//    // ── Layout ──────────────────────────────────────────────────────────
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .statusBarsPadding()
//    ) {
//        CashioTopBar(
//            title = CashioTopBarTitle.Text(
//                if (state.isEditMode) "Edit Transaction" else "Add Transaction"
//            ),
//            leadingAction = TopBarAction(
//                icon = TopBarIcon.Vector(Icons.Default.Close),
//                onClick = onNavigateBack
//            ),
//            modifier = Modifier.padding(horizontal = CashioPadding.screen)
//        )
//
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.spacedBy(CashioSpacing.default),
//            contentPadding = PaddingValues(
//                start = CashioPadding.screen,
//                end = CashioPadding.screen,
//                top = CashioSpacing.small,
//                bottom = navBarPadding + CashioSpacing.default
//            )
//        ) {
//            // Type toggle
//            item {
//                SectionCard {
//                    SectionTitle("Type")
//                    Spacer(Modifier.height(CashioSpacing.medium))
//                    TransactionTypeRow(
//                        selectedType = state.transactionType,
//                        onTypeSelected = onTypeSelected
//                    )
//                }
//            }
//
//            // Amount & Title
//            item {
//                SectionCard {
//                    AmountField(
//                        amount = state.amount,
//                        transactionType = state.transactionType,
//                        onAmountChange = viewModel::updateAmount
//                    )
//                    Spacer(Modifier.height(CashioSpacing.medium))
//                    TitleField(
//                        title = state.title,
//                        onTitleChange = viewModel::updateTitle
//                    )
//                }
//            }
//
//            // Category
//            item {
//                CategorySection(
//                    categoriesState = state.categories,
//                    selectedCategoryId = state.selectedCategory?.id,
//                    onCategorySelected = onCategorySelected,
//                    onManageCategories = onNavigateToCategories
//                )
//            }
//
//            // Optional details
//            item {
//                MoreDetailsSection(
//                    expanded = showMoreDetails,
//                    onToggleExpanded = onToggleDetails,
//                    date = state.date,
//                    note = state.note,
//                    onDateClick = { showDatePicker = true },
//                    onTimeClick = { showTimePicker = true },
//                    onNoteChange = viewModel::updateNote
//                )
//            }
//
//            // Save
//            item {
//                SaveButton(
//                    text = when {
//                        state.isSaving -> "Saving..."
//                        state.isEditMode -> "Update"
//                        else -> "Save"
//                    },
//                    enabled = isFormValid && !state.isSaving,
//                    isLoading = state.isSaving,
//                    onClick = onSaveClick
//                )
//            }
//        }
//    }
//
//    // ── Dialogs ─────────────────────────────────────────────────────────
//    if (showDatePicker) {
//        CashioDatePickerDialog(
//            initialDateTime = state.date,
//            onDismiss = { showDatePicker = false },
//            onConfirm = { newDate ->
//                viewModel.updateDate(LocalDateTime.of(newDate, state.date.toLocalTime()))
//                showDatePicker = false
//            }
//        )
//    }
//
//    if (showTimePicker) {
//        CashioTimePickerDialog(
//            initialDateTime = state.date,
//            onDismiss = { showTimePicker = false },
//            onConfirm = { newTime ->
//                viewModel.updateDate(LocalDateTime.of(state.date.toLocalDate(), newTime))
//                showTimePicker = false
//            }
//        )
//    }
//}

package com.bluemix.cashio.presentation.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluemix.cashio.presentation.common.SectionCard
import com.bluemix.cashio.ui.defaults.CashioPadding
import com.bluemix.cashio.ui.defaults.CashioSpacing
import com.bluemix.cashio.ui.defaults.CashioTopBar
import com.bluemix.cashio.ui.defaults.CashioTopBarTitle
import com.bluemix.cashio.ui.defaults.TopBarAction
import com.bluemix.cashio.ui.defaults.TopBarIcon
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategories: () -> Unit,
    expenseId: String? = null,
    viewModel: AddExpenseViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val navBarPadding = WindowInsets.navigationBars
        .asPaddingValues().calculateBottomPadding()

    LaunchedEffect(expenseId) {
        if (expenseId != null) viewModel.loadExpenseForEdit(expenseId)
        else viewModel.resetForm()
    }

    LaunchedEffect(state.saveSuccess) {
        if (!state.saveSuccess) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        delay(AddExpenseDefaults.SaveDelayMs)
        viewModel.consumeSaveSuccess()
        onNavigateBack()
    }

    var showMoreDetails by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    val isFormValid = remember(state.amount, state.title, state.selectedCategory) {
        state.amount.isNotBlank() && state.title.isNotBlank() && state.selectedCategory != null
    }

    val onTypeSelected = remember(haptic) {
        { type: com.bluemix.cashio.domain.model.TransactionType ->
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.updateTransactionType(type)
        }
    }
    val onCategorySelected = remember(haptic) {
        { cat: com.bluemix.cashio.domain.model.Category ->
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.selectCategory(cat)
        }
    }
    val onToggleDetails = remember(haptic) {
        {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            showMoreDetails = !showMoreDetails
        }
    }
    val onSaveClick = remember(haptic) {
        {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.saveExpense()
        }
    }

    Column(modifier = modifier.fillMaxSize().statusBarsPadding()) {
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
            verticalArrangement = Arrangement.spacedBy(CashioSpacing.md),
            contentPadding = PaddingValues(
                start = CashioPadding.screen,
                end = CashioPadding.screen,
                top = CashioSpacing.xs,
                bottom = navBarPadding + CashioSpacing.md
            )
        ) {
            item {
                SectionCard {
                    SectionTitle("Type")
                    Spacer(Modifier.height(CashioSpacing.sm))
                    TransactionTypeRow(
                        selectedType = state.transactionType,
                        onTypeSelected = onTypeSelected
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
                    Spacer(Modifier.height(CashioSpacing.sm))
                    TitleField(title = state.title, onTitleChange = viewModel::updateTitle)
                }
            }
            item {
                CategorySection(
                    categoriesState = state.categories,
                    selectedCategoryId = state.selectedCategory?.id,
                    onCategorySelected = onCategorySelected,
                    onManageCategories = onNavigateToCategories
                )
            }
            item {
                MoreDetailsSection(
                    expanded = showMoreDetails,
                    onToggleExpanded = onToggleDetails,
                    date = state.date,
                    note = state.note,
                    onDateClick = { showDatePicker = true },
                    onTimeClick = { showTimePicker = true },
                    onNoteChange = viewModel::updateNote
                )
            }
            item {
                SaveButton(
                    text = when {
                        state.isSaving -> "Saving..."
                        state.isEditMode -> "Update"
                        else -> "Save"
                    },
                    enabled = isFormValid && !state.isSaving,
                    isLoading = state.isSaving,
                    onClick = onSaveClick
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