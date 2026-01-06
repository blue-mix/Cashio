package com.bluemix.cashio.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.ExpenseSource
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.domain.usecase.category.GetCategoriesUseCase
import com.bluemix.cashio.domain.usecase.expense.AddExpenseUseCase
import com.bluemix.cashio.domain.usecase.expense.GetExpenseByIdUseCase
import com.bluemix.cashio.domain.usecase.expense.UpdateExpenseUseCase
import com.bluemix.cashio.presentation.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

/**
 * Regex for filtering input as the user types.
 * Allows digits and a single decimal point with up to 2 decimal places.
 */
private val AmountInputRegex = Regex("^\\d*\\.?\\d{0,2}$")

/**
 * UI State holding all form data and UI status flags.
 */
data class AddExpenseState(
    val amount: String = "",
    val title: String = "",
    val selectedCategory: Category? = null,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val date: LocalDateTime = LocalDateTime.now(),
    val note: String = "",

    // Data Loading
    val categories: UiState<List<Category>> = UiState.Idle,

    // Submission Status
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,

    // Edit Mode Flags
    val isEditMode: Boolean = false,
    val editingExpenseId: String? = null
)

/**
 * ViewModel managing the Create/Update Expense screen.
 *
 * Responsibilities:
 * 1. Validates form input (Amount, Title, Category).
 * 2. Fetches available Categories for the dropdown.
 * 3. Handles switching between "Add" and "Edit" modes based on navigation arguments.
 * 4. Persists the transaction to the database via UseCases.
 */
class AddExpenseViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val getExpenseByIdUseCase: GetExpenseByIdUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddExpenseState())
    val state: StateFlow<AddExpenseState> = _state.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            updateState { it.copy(categories = UiState.Loading) }

            when (val result = getCategoriesUseCase()) {
                is Result.Success -> {
                    val categories = result.data
                    updateState { current ->
                        // Logic: If a category was already selected (e.g. during rotation or edit mode load),
                        // try to find the updated instance of that object in the new list to ensure consistency.
                        // Otherwise, default to the first category.
                        val reconciledSelection = current.selectedCategory?.let { selected ->
                            categories.find { it.id == selected.id } ?: selected
                        } ?: categories.firstOrNull()

                        current.copy(
                            categories = UiState.Success(categories),
                            selectedCategory = reconciledSelection
                        )
                    }
                }

                is Result.Error -> {
                    updateState {
                        it.copy(
                            categories = UiState.Error(
                                result.message ?: "Failed to load categories"
                            )
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    /**
     * Initializes the form with data from an existing expense.
     * Prevents re-fetching if the ID is consistent (e.g., config changes).
     */
    fun loadExpenseForEdit(expenseId: String) {
        if (state.value.editingExpenseId == expenseId) return

        viewModelScope.launch {
            when (val result = getExpenseByIdUseCase(expenseId)) {
                is Result.Success -> {
                    result.data?.let { expense ->
                        updateState { current ->
                            // Attempt to map the expense's category to the loaded list
                            val categories =
                                (current.categories as? UiState.Success)?.data.orEmpty()
                            val matchingCategory = categories.find { it.id == expense.category.id }
                                ?: expense.category

                            current.copy(
                                amount = expense.amount.toString(),
                                title = expense.title,
                                selectedCategory = matchingCategory,
                                transactionType = expense.transactionType,
                                date = expense.date,
                                note = expense.note,
                                isEditMode = true,
                                editingExpenseId = expense.id
                            )
                        }
                    } ?: setError("Expense not found")
                }

                is Result.Error -> setError(result.message ?: "Failed to load expense")
                else -> Unit
            }
        }
    }

    /* -------------------------------------------------------------------------- */
    /* User Actions                                                               */
    /* -------------------------------------------------------------------------- */

    fun updateAmount(value: String) {
        if (value.isEmpty() || AmountInputRegex.matches(value)) {
            updateState { it.copy(amount = value) }
        }
    }

    fun updateTitle(value: String) = updateState { it.copy(title = value) }

    fun selectCategory(category: Category) = updateState { it.copy(selectedCategory = category) }

    fun updateTransactionType(type: TransactionType) =
        updateState { it.copy(transactionType = type) }

    fun updateDate(date: LocalDateTime) = updateState { it.copy(date = date) }

    fun updateNote(note: String) = updateState { it.copy(note = note) }

    fun saveExpense() {
        val current = state.value

        // Validation
        val amount = current.amount.toDoubleOrNull()
        if (amount == null || amount <= 0.0) return setError("Please enter a valid amount")
        if (current.title.isBlank()) return setError("Please enter a title")
        if (current.selectedCategory == null) return setError("Please select a category")

        viewModelScope.launch {
            updateState { it.copy(isSaving = true, errorMessage = null) }

            val expense = Expense(
                id = current.editingExpenseId ?: "exp_${UUID.randomUUID()}",
                amount = amount,
                title = current.title.trim(),
                category = current.selectedCategory,
                date = current.date,
                note = current.note.trim(),
                source = ExpenseSource.MANUAL,
                transactionType = current.transactionType
            )

            val result = if (current.isEditMode) {
                updateExpenseUseCase(expense)
            } else {
                addExpenseUseCase(expense)
            }

            when (result) {
                is Result.Success -> updateState { it.copy(isSaving = false, saveSuccess = true) }
                is Result.Error -> updateState {
                    it.copy(isSaving = false, errorMessage = result.message ?: "Save failed")
                }

                else -> updateState { it.copy(isSaving = false) }
            }
        }
    }

    /* -------------------------------------------------------------------------- */
    /* Helpers                                                                    */
    /* -------------------------------------------------------------------------- */

    private fun setError(msg: String) = updateState { it.copy(errorMessage = msg) }

    fun clearError() = updateState { it.copy(errorMessage = null) }

    fun consumeSaveSuccess() = updateState { it.copy(saveSuccess = false) }

    /**
     * Resets input fields but retains the category list to avoid unnecessary reloading.
     */
    fun resetForm() {
        updateState { current ->
            val categories = (current.categories as? UiState.Success)?.data.orEmpty()
            AddExpenseState(
                categories = current.categories,
                selectedCategory = current.selectedCategory ?: categories.firstOrNull(),
                date = LocalDateTime.now()
            )
        }
    }

    private fun updateState(transform: (AddExpenseState) -> AddExpenseState) {
        _state.update(transform)
    }
}