package com.bluemix.cashio.presentation.categories

import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.usecase.category.AddCategoryUseCase
import com.bluemix.cashio.domain.usecase.category.DeleteCategoryUseCase
import com.bluemix.cashio.domain.usecase.category.ObserveCategoriesUseCase
import com.bluemix.cashio.domain.usecase.category.UpdateCategoryUseCase
import com.bluemix.cashio.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Editor state for add / edit category dialog.
 *
 * [colorHex] is a [Long] ARGB value (e.g. 0xFF4CAF50L) — never a
 * [androidx.compose.ui.graphics.Color]. Conversion happens at the UI layer only.
 */
data class CategoryEditorState(
    val categoryId: String? = null,       // null = add mode
    val name: String = "",
    val icon: String = "📦",
    val colorHex: Long = 0xFF9E9E9EL,     // Default grey
    val isSaving: Boolean = false
) {
    val isEditMode: Boolean get() = categoryId != null
}

/**
 * Describes a category deletion that was blocked because the category is in use.
 * Shown to the user as a confirmation dialog.
 */
data class PendingForceDelete(
    val categoryId: String,
    val categoryName: String
)

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val editor: CategoryEditorState? = null,       // non-null = dialog open
    val pendingForceDelete: PendingForceDelete? = null,
    val message: String? = null,
    val isError: Boolean = false
)

class CategoriesViewModel(
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        // Reactive — no manual reloads needed after mutations.
        viewModelScope.launch {
            observeCategoriesUseCase().collect { categories ->
                _uiState.update { it.copy(categories = categories, isLoading = false) }
            }
        }
    }

    // ── Dialog open/close ──────────────────────────────────────────────────

    fun openAddCategory() {
        _uiState.update {
            it.copy(editor = CategoryEditorState())
        }
    }

    fun openEditCategory(category: Category) {
        _uiState.update {
            it.copy(
                editor = CategoryEditorState(
                    categoryId = category.id,
                    name = category.name,
                    icon = category.icon,
                    colorHex = category.colorHex
                )
            )
        }
    }

    fun closeEditor() {
        _uiState.update { it.copy(editor = null) }
    }

    // ── Editor field updates ───────────────────────────────────────────────

    fun setEditorName(name: String) =
        _uiState.update { it.copy(editor = it.editor?.copy(name = name)) }

    fun setEditorIcon(icon: String) =
        _uiState.update { it.copy(editor = it.editor?.copy(icon = icon)) }

    /** [colorHex] must be an ARGB Long (e.g. 0xFF4CAF50L). */
    fun setEditorColor(colorHex: Long) =
        _uiState.update { it.copy(editor = it.editor?.copy(colorHex = colorHex)) }

    // ── Save ───────────────────────────────────────────────────────────────

    fun saveCategory() {
        val editor = _uiState.value.editor ?: return
        if (editor.isSaving) return
        if (editor.name.isBlank()) {
            _uiState.update { it.copy(message = "Category name is required", isError = true) }
            return
        }

        _uiState.update { it.copy(editor = it.editor?.copy(isSaving = true)) }

        viewModelScope.launch {
            val category = if (editor.isEditMode) {
                // EDIT: look up the live category to preserve fields not in the editor.
                val existing = _uiState.value.categories.find { it.id == editor.categoryId }
                if (existing == null) {
                    // Category was deleted while editor was open — clean up and bail.
                    _uiState.update {
                        it.copy(
                            editor = null,
                            message = "Category no longer exists",
                            isError = true
                        )
                    }
                    return@launch
                }
                existing.copy(
                    name = editor.name.trim(),
                    icon = editor.icon,
                    colorHex = editor.colorHex
                )
            } else {
                // ADD: generate a stable slug ID from the name.
                Category(
                    id = generateCategoryId(editor.name),
                    name = editor.name.trim(),
                    icon = editor.icon,
                    colorHex = editor.colorHex,
                    isDefault = false
                )
            }

            val result = if (editor.isEditMode) {
                updateCategoryUseCase(category)
            } else {
                addCategoryUseCase(category)
            }

            when (result) {
                is Result.Success -> {
                    // Reactive flow already updates the list — just close the dialog.
                    _uiState.update {
                        it.copy(
                            editor = null,
                            message = if (editor.isEditMode) "Category updated" else "Category added",
                            isError = false
                        )
                    }
                }

                is Result.Error -> {
                    // Always reset isSaving on error so the user can retry.
                    _uiState.update {
                        it.copy(
                            editor = it.editor?.copy(isSaving = false),
                            message = result.message,
                            isError = true
                        )
                    }
                }

                else -> _uiState.update { it.copy(editor = it.editor?.copy(isSaving = false)) }
            }
        }
    }

    // ── Delete ─────────────────────────────────────────────────────────────

    /**
     * Attempts to delete [category]. If the category is in use, sets
     * [CategoriesUiState.pendingForceDelete] so the UI can show a confirmation
     * dialog offering the user two options: reassign-and-delete or cancel.
     *
     * This closes the UX dead-end where an error was shown but no action offered.
     */
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            when (val result = deleteCategoryUseCase(DeleteCategoryUseCase.Params(category.id))) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            editor = null,
                            message = "${category.name} deleted",
                            isError = false
                        )
                    }
                }

                is Result.Error -> {
                    val ex = result.exception
                    if (ex is IllegalStateException && ex.message?.contains("in use") == true) {
                        // Category is in use — surface confirmation dialog.
                        _uiState.update {
                            it.copy(
                                pendingForceDelete = PendingForceDelete(
                                    category.id,
                                    category.name
                                )
                            )
                        }
                    } else {
                        _uiState.update { it.copy(message = result.message, isError = true) }
                    }
                }

                else -> Unit
            }
        }
    }

    /**
     * User confirmed force-delete: reassigns all expenses to "other" then deletes.
     */
    fun confirmForceDelete() {
        val pending = _uiState.value.pendingForceDelete ?: return
        _uiState.update { it.copy(pendingForceDelete = null) }

        viewModelScope.launch {
            when (val result = deleteCategoryUseCase(
                DeleteCategoryUseCase.Params(pending.categoryId, forceDelete = true)
            )) {
                is Result.Success -> _uiState.update {
                    it.copy(message = "${pending.categoryName} deleted", isError = false)
                }

                is Result.Error -> _uiState.update {
                    it.copy(message = result.message, isError = true)
                }

                else -> Unit
            }
        }
    }

    fun dismissForceDeleteDialog() {
        _uiState.update { it.copy(pendingForceDelete = null) }
    }

    fun dismissMessage() = _uiState.update { it.copy(message = null, isError = false) }

    // ── Helpers ────────────────────────────────────────────────────────────

    /**
     * Generates a stable lowercase slug from [name] for use as the category ID.
     * Duplicate IDs are handled by [AddCategoryUseCase] returning [Result.Error].
     */
    private fun generateCategoryId(name: String): String =
        name.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .take(40)
            .trimEnd('_')
}