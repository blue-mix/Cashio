package com.bluemix.cashio.presentation.transaction

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

data class AddExpenseState(
    val amount: String = "",
    val title: String = "",
    val selectedCategory: Category? = null,
    val transactionType: TransactionType = TransactionType.EXPENSE,

    val date: LocalDateTime = LocalDateTime.now(),
    val note: String = "",

    val categories: UiState<List<Category>> = UiState.Idle,

    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,

    val isEditMode: Boolean = false,
    val editingExpenseId: String? = null
)

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
            _state.update { it.copy(categories = UiState.Loading) }

            when (val result = getCategoriesUseCase()) {
                is Result.Success -> {
                    val categories = result.data
                    _state.update { current ->
                        current.copy(
                            categories = UiState.Success(categories),
                            selectedCategory = current.selectedCategory ?: categories.firstOrNull()
                        )
                    }
                }

                is Result.Error -> {
                    _state.update {
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

    fun loadExpenseForEdit(expenseId: String) {
        viewModelScope.launch {
            when (val result = getExpenseByIdUseCase(expenseId)) {
                is Result.Success -> {
                    val expense = result.data
                    if (expense == null) {
                        _state.update { it.copy(errorMessage = "Expense not found") }
                        return@launch
                    }

                    _state.update { current ->
                        current.copy(
                            amount = expense.amount.toString(),
                            title = expense.title,
                            selectedCategory = expense.category,
                            transactionType = expense.transactionType,
                            date = expense.date,
                            note = expense.note,
                            isEditMode = true,
                            editingExpenseId = expense.id,
                            saveSuccess = false,
                            errorMessage = null
                        )
                    }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            errorMessage = result.message ?: "Failed to load expense"
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    fun updateAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _state.update { it.copy(amount = amount) }
        }
    }

    fun updateTitle(title: String) {
        _state.update { it.copy(title = title) }
    }

    fun selectCategory(category: Category) {
        _state.update { it.copy(selectedCategory = category) }
    }

    fun updateTransactionType(type: TransactionType) {
        _state.update { it.copy(transactionType = type) }
    }

    fun updateDate(date: LocalDateTime) {
        _state.update { it.copy(date = date) }
    }

    fun updateNote(note: String) {
        _state.update { it.copy(note = note) }
    }

    fun saveExpense() {
        val current = _state.value

        val amount = current.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.update { it.copy(errorMessage = "Please enter a valid amount") }
            return
        }

        if (current.title.isBlank()) {
            _state.update { it.copy(errorMessage = "Please enter a title") }
            return
        }

        val category = current.selectedCategory
        if (category == null) {
            _state.update { it.copy(errorMessage = "Please select a category") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null, saveSuccess = false) }

            val expense = Expense(
                id = current.editingExpenseId ?: "exp_${UUID.randomUUID()}",
                amount = amount,
                title = current.title.trim(),
                category = category,
                date = current.date,
                note = current.note.trim(),
                source = ExpenseSource.MANUAL,
                transactionType = current.transactionType
            )

            val result =
                if (current.isEditMode) updateExpenseUseCase(expense) else addExpenseUseCase(expense)

            when (result) {
                is Result.Success -> {
                    _state.update { it.copy(isSaving = false, saveSuccess = true) }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message ?: "Failed to save"
                        )
                    }
                }

                else -> {
                    _state.update { it.copy(isSaving = false) }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun consumeSaveSuccess() {
        _state.update { it.copy(saveSuccess = false) }
    }

    fun resetForm() {
        val currentCategories = _state.value.categories
        val defaultCategory = (currentCategories as? UiState.Success)?.data?.firstOrNull()

        _state.update {
            AddExpenseState(
                categories = currentCategories,
                selectedCategory = defaultCategory
            )
        }
    }
}
