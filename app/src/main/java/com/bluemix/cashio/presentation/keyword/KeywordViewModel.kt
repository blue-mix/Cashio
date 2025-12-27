package com.bluemix.cashio.presentation.keywordmapping

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class KeywordMappingState(
    val query: String = "",
    val mappings: UiState<List<KeywordMapping>> = UiState.Idle,
    val categories: UiState<List<Category>> = UiState.Idle,

    val isSheetOpen: Boolean = false,
    val isEditMode: Boolean = false,
    val editing: KeywordMapping? = null,

    val keyword: String = "",
    val categoryId: String = "",
    val priority: Int = 5,

    val confirmDelete: KeywordMapping? = null,

    val isSaving: Boolean = false,
    val operationMessage: String? = null,
    val errorMessage: String? = null
)

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

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(mappings = UiState.Loading, categories = UiState.Loading) }

            val mappingsDeferred = async { getKeywordMappings() }
            val categoriesDeferred = async { getCategories() }

            when (val res = mappingsDeferred.await()) {
                is Result.Success -> _state.update { it.copy(mappings = UiState.Success(res.data)) }
                is Result.Error -> _state.update {
                    it.copy(mappings = UiState.Error(res.message ?: "Failed to load mappings"))
                }

                else -> Unit
            }

            when (val res = categoriesDeferred.await()) {
                is Result.Success -> _state.update { it.copy(categories = UiState.Success(res.data)) }
                is Result.Error -> _state.update {
                    it.copy(categories = UiState.Error(res.message ?: "Failed to load categories"))
                }

                else -> Unit
            }
        }
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query) }

    fun openAddSheet(defaultCategoryId: String? = null) {
        val categoryId = defaultCategoryId
            ?: (_state.value.categories as? UiState.Success)?.data?.firstOrNull()?.id
            ?: _state.value.categoryId

        _state.update {
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
        _state.update {
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
        _state.update { it.copy(isSheetOpen = false, errorMessage = null, isSaving = false) }

    fun setKeyword(value: String) = _state.update { it.copy(keyword = value) }
    fun setCategoryId(value: String) = _state.update { it.copy(categoryId = value) }
    fun setPriority(value: Int) = _state.update { it.copy(priority = value.coerceIn(1, 10)) }

    fun requestDelete(mapping: KeywordMapping) = _state.update { it.copy(confirmDelete = mapping) }
    fun dismissDelete() = _state.update { it.copy(confirmDelete = null) }

    fun save() {
        val s = _state.value
        val keyword = s.keyword.trim()

        if (keyword.isBlank()) {
            _state.update { it.copy(errorMessage = "Enter a keyword") }
            return
        }
        if (s.categoryId.isBlank()) {
            _state.update { it.copy(errorMessage = "Select a category") }
            return
        }
        if (s.isSaving) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null, operationMessage = null) }

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

                    // Recategorize only when it matters
                    if (isEdit) {
                        if (oldKeyword.isNotBlank()) recategorizeExpensesByKeyword(oldKeyword)
                        if (!keyword.equals(
                                oldKeyword,
                                ignoreCase = true
                            )
                        ) recategorizeExpensesByKeyword(keyword)
                    } else {
                        recategorizeExpensesByKeyword(keyword)
                    }

                    _state.update {
                        it.copy(
                            isSheetOpen = false,
                            isSaving = false,
                            operationMessage = if (isEdit) "Mapping updated" else "Mapping added",
                            errorMessage = null
                        )
                    }
                    // Keep your current behavior: re-fetch after mutation.
                    load()
                }

                is Result.Error -> _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.message ?: "Failed to save"
                    )
                }

                else -> _state.update { it.copy(isSaving = false) }
            }
        }
    }

    fun deleteConfirmed() {
        val mapping = _state.value.confirmDelete ?: return

        viewModelScope.launch {
            when (val res = deleteKeywordMapping(mapping.id)) {
                is Result.Success -> {
                    val kw = mapping.keyword.trim()
                    log("🗑️ Mapping DELETED | keyword='$kw' id='${mapping.id}'")

                    if (kw.isNotBlank()) recategorizeExpensesByKeyword(kw)

                    _state.update {
                        it.copy(
                            confirmDelete = null,
                            operationMessage = "Mapping deleted",
                            errorMessage = null
                        )
                    }
                    load()
                }

                is Result.Error -> _state.update {
                    it.copy(
                        confirmDelete = null,
                        errorMessage = res.message ?: "Failed to delete"
                    )
                }

                else -> Unit
            }
        }
    }

    fun clearMessages() = _state.update { it.copy(operationMessage = null, errorMessage = null) }

    private fun log(message: String) {
        Log.i(TAG, message)
    }
}
