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

@Immutable
data class CategoriesState(
    val categories: UiState<List<Category>> = UiState.Idle,
    val query: String = "",
    val editor: CategoryEditorState? = null, // null = closed
    val isDeleting: Boolean = false,
    val message: UiMessage? = null
)

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

enum class EditorMode { ADD, EDIT }

@Immutable
data class UiMessage(val type: UiMessageType, val text: String)

enum class UiMessageType { SUCCESS, ERROR }

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

    fun loadCategories() {
        viewModelScope.launch {
            _state.update { it.copy(categories = UiState.Loading) }

            when (val result = getCategoriesUseCase()) {
                is Result.Success -> _state.update {
                    it.copy(categories = UiState.Success(result.data))
                }

                is Result.Error -> _state.update {
                    it.copy(categories = UiState.Error(result.message ?: "Failed to load categories"))
                }

                else -> Unit
            }
        }
    }

    /* ------------------------- Search ------------------------- */

    fun setQuery(value: String) = _state.update { it.copy(query = value) }

    /* ------------------------- Editor ------------------------- */

    fun openAddCategory() {
        _state.update {
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

    fun openEditCategory(category: Category) {
        _state.update {
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

    fun updateEditorName(value: String) = _state.update { s ->
        val editor = s.editor ?: return@update s
        s.copy(editor = editor.copy(name = value, fieldError = null))
    }

    fun updateEditorIcon(value: String) = _state.update { s ->
        val editor = s.editor ?: return@update s
        s.copy(editor = editor.copy(icon = value))
    }

    fun updateEditorColor(value: Color) = _state.update { s ->
        val editor = s.editor ?: return@update s
        s.copy(editor = editor.copy(color = value))
    }

    fun dismissEditor() = _state.update { it.copy(editor = null) }

    fun saveCategory() {
        val currentEditor = _state.value.editor ?: return
        if (currentEditor.isSaving) return

        val name = currentEditor.name.trim()
        if (name.isBlank()) {
            _state.update { s ->
                val editor = s.editor ?: return@update s
                s.copy(editor = editor.copy(fieldError = "Please enter a category name"))
            }
            return
        }

        viewModelScope.launch {
            // mark saving using latest editor
            _state.update { s ->
                val editor = s.editor ?: return@update s
                s.copy(editor = editor.copy(isSaving = true, fieldError = null))
            }

            val editorSnapshot = _state.value.editor ?: currentEditor

            val category = when (editorSnapshot.mode) {
                EditorMode.ADD -> Category(
                    id = "cat_${UUID.randomUUID()}",
                    name = name,
                    icon = editorSnapshot.icon,
                    color = editorSnapshot.color,
                    isDefault = false
                )

                EditorMode.EDIT -> {
                    val existing = (state.value.categories as? UiState.Success)
                        ?.data
                        ?.firstOrNull { it.id == editorSnapshot.categoryId }

                    val base = existing ?: Category(
                        id = editorSnapshot.categoryId ?: "unknown",
                        name = name,
                        icon = editorSnapshot.icon,
                        color = editorSnapshot.color,
                        isDefault = false
                    )

                    base.copy(
                        name = name,
                        icon = editorSnapshot.icon,
                        color = editorSnapshot.color
                    )
                }
            }

            val result = when (editorSnapshot.mode) {
                EditorMode.ADD -> addCategoryUseCase(category)
                EditorMode.EDIT -> updateCategoryUseCase(category)
            }

            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            editor = null,
                            message = UiMessage(
                                type = UiMessageType.SUCCESS,
                                text = if (editorSnapshot.mode == EditorMode.EDIT) "Category updated" else "Category added"
                            )
                        )
                    }
                    loadCategories()
                }

                is Result.Error -> {
                    _state.update { s ->
                        val editor = s.editor ?: editorSnapshot
                        s.copy(
                            editor = editor.copy(isSaving = false),
                            message = UiMessage(
                                type = UiMessageType.ERROR,
                                text = result.message ?: "Failed to save category"
                            )
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    /* ------------------------- Delete ------------------------- */

    fun deleteCategory(categoryId: String, forceDelete: Boolean = false) {
        if (_state.value.isDeleting) return

        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }

            val params = DeleteCategoryUseCase.Params(categoryId, forceDelete)
            when (val result = deleteCategoryUseCase(params)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isDeleting = false,
                            message = UiMessage(UiMessageType.SUCCESS, "Category deleted")
                        )
                    }
                    loadCategories()
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isDeleting = false,
                            message = UiMessage(UiMessageType.ERROR, result.message ?: "Failed to delete category")
                        )
                    }
                }

                else -> _state.update { it.copy(isDeleting = false) }
            }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}
