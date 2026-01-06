package com.bluemix.cashio.presentation.categories

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.usecase.category.AddCategoryUseCase
import com.bluemix.cashio.domain.usecase.category.DeleteCategoryUseCase
import com.bluemix.cashio.domain.usecase.category.GetCategoriesUseCase
import com.bluemix.cashio.domain.usecase.category.UpdateCategoryUseCase
import com.bluemix.cashio.presentation.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Represents the main UI state for the Categories screen.
 *
 * @property categories The current state of the category list (Loading, Success, Error).
 * @property query The current search query for filtering categories.
 * @property editor The state of the add/edit dialog, or null if the editor is closed.
 * @property isDeleting Indicates if a delete operation is currently in progress.
 * @property message A transient message (snackbar) to be displayed to the user.
 */
@Immutable
data class CategoriesState(
    val categories: UiState<List<Category>> = UiState.Idle,
    val query: String = "",
    val editor: CategoryEditorState? = null,
    val isDeleting: Boolean = false,
    val message: UiMessage? = null
)

/**
 * Represents the state of the Category Editor (Add/Edit) sheet.
 *
 * @property mode Determines if the editor is creating a new category or updating an existing one.
 * @property categoryId The ID of the category being edited (null if in ADD mode).
 * @property name The current input value for the category name.
 * @property icon The currently selected emoji/icon.
 * @property color The currently selected color.
 * @property fieldError Validation error message for the input fields, if any.
 * @property isSaving Indicates if the save operation is currently in progress.
 */
@Immutable
data class CategoryEditorState(
    val mode: EditorMode,
    val categoryId: String? = null,
    val name: String = "",
    val icon: String = "📦",
    val color: Color = Color(0xFF4CAF50),
    val fieldError: String? = null,
    val isSaving: Boolean = false
)

/**
 * Defines the operational mode of the category editor.
 */
enum class EditorMode {
    ADD,
    EDIT
}

/**
 * Represents a one-time UI message event.
 */
@Immutable
data class UiMessage(val type: UiMessageType, val text: String)

/**
 * Defines the types of feedback messages available.
 */
enum class UiMessageType {
    SUCCESS,
    ERROR
}

/**
 * ViewModel responsible for managing the Categories screen.
 *
 * Handles fetching, filtering, creating, updating, and deleting categories.
 *
 * @property getCategoriesUseCase Use case to retrieve the list of categories.
 * @property addCategoryUseCase Use case to create a new category.
 * @property updateCategoryUseCase Use case to modify an existing category.
 * @property deleteCategoryUseCase Use case to remove a category.
 */
class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state.asStateFlow()

    init {
        loadCategories()
    }

    /**
     * Triggers the loading of categories from the data source.
     * Updates the UI state to Loading, Success, or Error accordingly.
     */
    fun loadCategories() {
        viewModelScope.launch {
            if (state.value.categories !is UiState.Success) {
                updateState { it.copy(categories = UiState.Loading) }
            }

            when (val result = getCategoriesUseCase()) {
                is Result.Success -> updateState { it.copy(categories = UiState.Success(result.data)) }
                is Result.Error -> updateState {
                    it.copy(
                        categories = UiState.Error(
                            result.message ?: "Failed to load categories"
                        )
                    )
                }

                else -> Unit
            }
        }
    }

    /**
     * Updates the search query used to filter the category list.
     *
     * @param value The new search string.
     */
    fun setQuery(value: String) = updateState { it.copy(query = value) }

    /**
     * Opens the category editor in [EditorMode.ADD] mode with default values.
     */
    fun openAddCategory() {
        updateState {
            it.copy(
                editor = CategoryEditorState(
                    mode = EditorMode.ADD,
                    name = "",
                    icon = "📦",
                    color = Color(0xFF4CAF50)
                )
            )
        }
    }

    /**
     * Opens the category editor in [EditorMode.EDIT] mode, pre-filled with the provided category data.
     *
     * @param category The category to be edited.
     */
    fun openEditCategory(category: Category) {
        updateState {
            it.copy(
                editor = CategoryEditorState(
                    mode = EditorMode.EDIT,
                    categoryId = category.id,
                    name = category.name,
                    icon = category.icon,
                    color = category.color
                )
            )
        }
    }

    /**
     * Updates the name field in the active editor.
     */
    fun updateEditorName(value: String) = updateEditor { it.copy(name = value, fieldError = null) }

    /**
     * Updates the icon field in the active editor.
     */
    fun updateEditorIcon(value: String) = updateEditor { it.copy(icon = value) }

    /**
     * Updates the color field in the active editor.
     */
    fun updateEditorColor(value: Color) = updateEditor { it.copy(color = value) }

    /**
     * Closes the editor without saving changes.
     */
    fun dismissEditor() = updateState { it.copy(editor = null) }

    /**
     * Validates the input and saves the category.
     *
     * - If in [EditorMode.ADD], creates a new category.
     * - If in [EditorMode.EDIT], updates the existing category.
     * - If validation fails, sets a field error in the editor state.
     */
    fun saveCategory() {
        val editor = state.value.editor ?: return
        if (editor.isSaving) return

        val name = editor.name.trim()
        if (name.isBlank()) {
            updateEditor { it.copy(fieldError = "Category name is required") }
            return
        }

        viewModelScope.launch {
            updateEditor { it.copy(isSaving = true, fieldError = null) }

            val category = if (editor.mode == EditorMode.ADD) {
                Category(
                    id = "cat_${UUID.randomUUID()}",
                    name = name,
                    icon = editor.icon,
                    color = editor.color,
                    isDefault = false
                )
            } else {
                val existingList = (state.value.categories as? UiState.Success)?.data
                val existing = existingList?.find { it.id == editor.categoryId }

                existing?.copy(name = name, icon = editor.icon, color = editor.color)
                    ?: return@launch showMessage(UiMessageType.ERROR, "Category not found")
            }

            val result = if (editor.mode == EditorMode.ADD) {
                addCategoryUseCase(category)
            } else {
                updateCategoryUseCase(category)
            }

            when (result) {
                is Result.Success -> {
                    updateState { it.copy(editor = null) }
                    showMessage(
                        UiMessageType.SUCCESS,
                        if (editor.mode == EditorMode.ADD) "Category added" else "Category updated"
                    )
                    loadCategories()
                }

                is Result.Error -> {
                    updateEditor { it.copy(isSaving = false) }
                    showMessage(UiMessageType.ERROR, result.message ?: "Failed to save category")
                }

                else -> updateEditor { it.copy(isSaving = false) }
            }
        }
    }

    /**
     * Deletes a category by its ID.
     *
     * @param categoryId The unique identifier of the category to delete.
     * @param forceDelete If true, deletes the category even if it has associated expenses.
     */
    fun deleteCategory(categoryId: String, forceDelete: Boolean = false) {
        if (state.value.isDeleting) return

        viewModelScope.launch {
            updateState { it.copy(isDeleting = true) }

            val params = DeleteCategoryUseCase.Params(categoryId, forceDelete)
            when (val result = deleteCategoryUseCase(params)) {
                is Result.Success -> {
                    updateState { it.copy(isDeleting = false) }
                    showMessage(UiMessageType.SUCCESS, "Category deleted")
                    loadCategories()
                }

                is Result.Error -> {
                    updateState { it.copy(isDeleting = false) }
                    showMessage(UiMessageType.ERROR, result.message ?: "Failed to delete category")
                }

                else -> updateState { it.copy(isDeleting = false) }
            }
        }
    }

    /**
     * Clears the current UI message (snackbar).
     */
    fun clearMessage() = updateState { it.copy(message = null) }

    private fun showMessage(type: UiMessageType, text: String) {
        updateState { it.copy(message = UiMessage(type, text)) }
    }

    private fun updateState(transform: (CategoriesState) -> CategoriesState) {
        _state.update(transform)
    }

    private fun updateEditor(transform: (CategoryEditorState) -> CategoryEditorState) {
        updateState { s ->
            val editor = s.editor ?: return@updateState s
            s.copy(editor = transform(editor))
        }
    }
}