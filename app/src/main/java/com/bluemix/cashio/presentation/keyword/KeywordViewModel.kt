package com.bluemix.cashio.presentation.keyword

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.KeywordMapping
import com.bluemix.cashio.domain.usecase.category.GetCategoriesUseCase
import com.bluemix.cashio.domain.usecase.expense.RecategorizeExpensesByKeywordUseCase
import com.bluemix.cashio.domain.usecase.keyword.AddKeywordMappingUseCase
import com.bluemix.cashio.domain.usecase.keyword.DeleteKeywordMappingUseCase
import com.bluemix.cashio.domain.usecase.keyword.GetKeywordMappingsUseCase
import com.bluemix.cashio.domain.usecase.keyword.UpdateKeywordMappingUseCase
import com.bluemix.cashio.presentation.common.UiState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * State for the Keyword Mapping screen.
 *
 * @property query Search filter for the mapping list.
 * @property mappings The list of active keyword rules.
 * @property categories Available categories for the picker.
 * @property isSheetOpen True if the Add/Edit bottom sheet is visible.
 * @property isEditMode True if we are editing an existing rule, False if creating new.
 */
data class KeywordMappingState(
    // Data Lists
    val query: String = "",
    val mappings: UiState<List<KeywordMapping>> = UiState.Idle,
    val categories: UiState<List<Category>> = UiState.Idle,

    // Sheet / Editor State
    val isSheetOpen: Boolean = false,
    val isEditMode: Boolean = false,
    val editing: KeywordMapping? = null,
    val keyword: String = "",
    val categoryId: String = "",
    val priority: Int = 5,

    // Operations
    val confirmDelete: KeywordMapping? = null,
    val isSaving: Boolean = false,
    val operationMessage: String? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel managing the Keyword Mapping feature.
 *
 * This VM handles:
 * 1. CRUD operations for Keyword Mappings.
 * 2. Automatic **Recategorization** of past expenses when rules change.
 * 3. UI state for the list, search, and edit/create bottom sheet.
 */
class KeywordMappingViewModel(
    private val getKeywordMappings: GetKeywordMappingsUseCase,
    private val addKeywordMapping: AddKeywordMappingUseCase,
    private val updateKeywordMapping: UpdateKeywordMappingUseCase,
    private val deleteKeywordMapping: DeleteKeywordMappingUseCase,
    private val recategorizeExpensesByKeyword: RecategorizeExpensesByKeywordUseCase,
    private val getCategories: GetCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(KeywordMappingState())
    val state: StateFlow<KeywordMappingState> = _state.asStateFlow()

    private companion object {
        const val TAG = "KeywordMappingVM"
    }

    init {
        load()
    }

    /**
     * Loads both the mappings list and the category list in parallel.
     */
    fun load() {
        viewModelScope.launch {
            updateState { it.copy(mappings = UiState.Loading, categories = UiState.Loading) }

            try {
                coroutineScope {
                    val mappingsDeferred = async { getKeywordMappings() }
                    val categoriesDeferred = async { getCategories() }

                    val mappingsRes = mappingsDeferred.await()
                    val categoriesRes = categoriesDeferred.await()

                    updateState { current ->
                        current.copy(
                            mappings = when (mappingsRes) {
                                is Result.Success -> UiState.Success(mappingsRes.data)
                                is Result.Error -> UiState.Error(
                                    mappingsRes.message ?: "Failed to load mappings"
                                )

                                else -> UiState.Idle
                            },
                            categories = when (categoriesRes) {
                                is Result.Success -> UiState.Success(categoriesRes.data)
                                is Result.Error -> UiState.Error(
                                    categoriesRes.message ?: "Failed to load categories"
                                )

                                else -> UiState.Idle
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                updateState { it.copy(mappings = UiState.Error(e.message ?: "Unknown error")) }
            }
        }
    }

    /* ------------------------- UI Actions ------------------------- */

    fun onQueryChange(query: String) = updateState { it.copy(query = query) }

    fun openAddSheet(defaultCategoryId: String? = null) {
        // Default to provided category, OR first available, OR whatever was last selected
        val categoryId = defaultCategoryId
            ?: (state.value.categories as? UiState.Success)?.data?.firstOrNull()?.id
            ?: state.value.categoryId

        updateState {
            it.copy(
                isSheetOpen = true,
                isEditMode = false,
                editing = null,
                keyword = "",
                categoryId = categoryId,
                priority = 5,
                isSaving = false,
                errorMessage = null,
                operationMessage = null
            )
        }
    }

    fun openEditSheet(mapping: KeywordMapping) {
        updateState {
            it.copy(
                isSheetOpen = true,
                isEditMode = true,
                editing = mapping,
                keyword = mapping.keyword,
                categoryId = mapping.categoryId,
                priority = mapping.priority,
                isSaving = false,
                errorMessage = null,
                operationMessage = null
            )
        }
    }

    fun closeSheet() =
        updateState { it.copy(isSheetOpen = false, errorMessage = null, isSaving = false) }

    fun setKeyword(value: String) = updateState { it.copy(keyword = value) }
    fun setCategoryId(value: String) = updateState { it.copy(categoryId = value) }
    fun setPriority(value: Int) = updateState { it.copy(priority = value.coerceIn(1, 10)) }

    fun requestDelete(mapping: KeywordMapping) = updateState { it.copy(confirmDelete = mapping) }
    fun dismissDelete() = updateState { it.copy(confirmDelete = null) }

    fun clearMessages() = updateState { it.copy(operationMessage = null, errorMessage = null) }

    /* ------------------------- Business Logic ------------------------- */

    /**
     * Persists the mapping (Add or Update) and triggers recategorization.
     */
    fun save() {
        val s = state.value
        val keyword = s.keyword.trim()

        if (keyword.isBlank()) return setError("Enter a keyword")
        if (s.categoryId.isBlank()) return setError("Select a category")
        if (s.isSaving) return

        viewModelScope.launch {
            updateState { it.copy(isSaving = true, errorMessage = null, operationMessage = null) }

            val isEdit = s.isEditMode && s.editing != null
            val oldKeyword = s.editing?.keyword?.trim().orEmpty()

            val payload = if (isEdit) {
                s.editing!!.copy(
                    keyword = keyword,
                    categoryId = s.categoryId,
                    priority = s.priority
                )
            } else {
                KeywordMapping(
                    id = "kw_${UUID.randomUUID()}",
                    keyword = keyword,
                    categoryId = s.categoryId,
                    priority = s.priority
                )
            }

            val result = if (isEdit) updateKeywordMapping(payload) else addKeywordMapping(payload)

            when (result) {
                is Result.Success -> {
                    log("✅ Mapping ${if (isEdit) "UPDATED" else "ADDED"} | old='$oldKeyword' new='$keyword'")

                    // --- Smart Recategorization ---
                    if (isEdit) {
                        // 1. If the keyword string changed, we must re-process the OLD keyword
                        //    (to potentially remove incorrect matches or apply fallback rules).
                        if (oldKeyword.isNotBlank() && !keyword.equals(
                                oldKeyword,
                                ignoreCase = true
                            )
                        ) {
                            recategorizeExpensesByKeyword(oldKeyword)
                        }
                        // 2. Process the NEW keyword to apply the updated category/priority.
                        recategorizeExpensesByKeyword(keyword)
                    } else {
                        // New mapping: Apply immediately to existing transactions.
                        recategorizeExpensesByKeyword(keyword)
                    }

                    updateState {
                        it.copy(
                            isSheetOpen = false,
                            isSaving = false,
                            operationMessage = if (isEdit) "Mapping updated" else "Mapping added"
                        )
                    }
                    load() // Refresh list UI
                }

                is Result.Error -> {
                    updateState {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message ?: "Failed to save"
                        )
                    }
                }

                else -> updateState { it.copy(isSaving = false) }
            }
        }
    }

    fun deleteConfirmed() {
        val mapping = state.value.confirmDelete ?: return

        viewModelScope.launch {
            when (val res = deleteKeywordMapping(mapping.id)) {
                is Result.Success -> {
                    val kw = mapping.keyword.trim()
                    log("🗑️ Mapping DELETED | keyword='$kw'")

                    // If a rule is deleted, re-run categorization on that keyword.
                    // This allows other lower-priority rules (if any) to take effect,
                    // or leaves the transactions uncategorized/manual.
                    if (kw.isNotBlank()) recategorizeExpensesByKeyword(kw)

                    updateState {
                        it.copy(confirmDelete = null, operationMessage = "Mapping deleted")
                    }
                    load()
                }

                is Result.Error -> {
                    updateState {
                        it.copy(
                            confirmDelete = null,
                            errorMessage = res.message ?: "Failed to delete"
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    /* ------------------------- Helpers ------------------------- */

    private fun updateState(transform: (KeywordMappingState) -> KeywordMappingState) {
        _state.update(transform)
    }

    private fun setError(msg: String) = updateState { it.copy(errorMessage = msg) }

    private fun log(message: String) {
        Log.i(TAG, message)
    }
}