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

    val operationMessage: String? = null,
    val errorMessage: String? = null
)

class KeywordMappingViewModel(
    private val getKeywordMappingsUseCase: GetKeywordMappingsUseCase,
    private val addKeywordMappingUseCase: AddKeywordMappingUseCase,
    private val updateKeywordMappingUseCase: UpdateKeywordMappingUseCase,
    private val deleteKeywordMappingUseCase: DeleteKeywordMappingUseCase,
    private val recategorizeExpensesByKeywordUseCase: RecategorizeExpensesByKeywordUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
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
        loadMappings()
        loadCategories()
    }

    private fun loadMappings() {
        viewModelScope.launch {
            _state.update { it.copy(mappings = UiState.Loading) }
            when (val res = getKeywordMappingsUseCase()) {
                is Result.Success -> _state.update { it.copy(mappings = UiState.Success(res.data)) }
                is Result.Error -> _state.update {
                    it.copy(
                        mappings = UiState.Error(
                            res.message ?: "Failed to load mappings"
                        )
                    )
                }

                else -> Unit
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _state.update { it.copy(categories = UiState.Loading) }
            when (val res = getCategoriesUseCase()) {
                is Result.Success -> _state.update { it.copy(categories = UiState.Success(res.data)) }
                is Result.Error -> _state.update {
                    it.copy(
                        categories = UiState.Error(
                            res.message ?: "Failed to load categories"
                        )
                    )
                }

                else -> Unit
            }
        }
    }

    fun onQueryChange(q: String) = _state.update { it.copy(query = q) }

    fun openAddSheet(defaultCategoryId: String? = null) {
        _state.update {
            it.copy(
                isSheetOpen = true,
                isEditMode = false,
                editing = null,
                keyword = "",
                categoryId = defaultCategoryId ?: it.categoryId,
                priority = 5,
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
                errorMessage = null,
                operationMessage = null
            )
        }
    }

    fun closeSheet() = _state.update { it.copy(isSheetOpen = false, errorMessage = null) }

    fun setKeyword(value: String) = _state.update { it.copy(keyword = value) }
    fun setCategoryId(value: String) = _state.update { it.copy(categoryId = value) }
    fun setPriority(value: Int) = _state.update { it.copy(priority = value.coerceIn(1, 10)) }

    fun requestDelete(mapping: KeywordMapping) = _state.update { it.copy(confirmDelete = mapping) }
    fun dismissDelete() = _state.update { it.copy(confirmDelete = null) }

    fun save() {
        val s = _state.value
        val kw = s.keyword.trim()
        if (kw.isBlank()) {
            _state.update { it.copy(errorMessage = "Enter a keyword") }
            return
        }
        if (s.categoryId.isBlank()) {
            _state.update { it.copy(errorMessage = "Select a category") }
            return
        }

        viewModelScope.launch {
            val isEdit = s.isEditMode && s.editing != null
            val oldKeyword = s.editing?.keyword?.trim().orEmpty()
            val newKeyword = kw

            val payload = if (isEdit) {
                s.editing!!.copy(
                    keyword = newKeyword,
                    categoryId = s.categoryId,
                    priority = s.priority
                )
            } else {
                KeywordMapping(
                    id = "kw_${UUID.randomUUID()}",
                    keyword = newKeyword,
                    categoryId = s.categoryId,
                    priority = s.priority
                )
            }

            val result = if (isEdit) {
                updateKeywordMappingUseCase(payload)
            } else {
                addKeywordMappingUseCase(payload)
            }

            when (result) {
                is Result.Success -> {
                    Log.i(
                        TAG,
                        "✅ Mapping ${if (isEdit) "UPDATED" else "ADDED"} | old='$oldKeyword' new='$newKeyword' catId='${s.categoryId}' priority=${s.priority}"
                    )

                    if (isEdit) {
                        if (oldKeyword.isNotBlank()) {
                            Log.d(TAG, "🔁 Recategorize triggered for OLD keyword='$oldKeyword'")
                            val r1 = recategorizeExpensesByKeywordUseCase(oldKeyword)
                            Log.i(TAG, "✅ Recategorize result old='$oldKeyword' => $r1")
                        }
                        if (newKeyword.isNotBlank() && !newKeyword.equals(
                                oldKeyword,
                                ignoreCase = true
                            )
                        ) {
                            Log.d(TAG, "🔁 Recategorize triggered for NEW keyword='$newKeyword'")
                            val r2 = recategorizeExpensesByKeywordUseCase(newKeyword)
                            Log.i(TAG, "✅ Recategorize result new='$newKeyword' => $r2")
                        }
                    } else {
                        Log.d(TAG, "🔁 Recategorize triggered for NEW keyword='$newKeyword'")
                        val r = recategorizeExpensesByKeywordUseCase(newKeyword)
                        Log.i(TAG, "✅ Recategorize result new='$newKeyword' => $r")
                    }

                    _state.update {
                        it.copy(
                            isSheetOpen = false,
                            operationMessage = if (isEdit) "Mapping updated" else "Mapping added",
                            errorMessage = null
                        )
                    }
                    loadMappings()
                }


                is Result.Error -> _state.update {
                    it.copy(
                        errorMessage = result.message ?: "Failed to save"
                    )
                }

                else -> Unit
            }
        }
    }

    fun deleteConfirmed() {
        val mapping = _state.value.confirmDelete ?: return
        viewModelScope.launch {
            when (val res = deleteKeywordMappingUseCase(mapping.id)) {
                is Result.Success -> {
                    val kw = mapping.keyword.trim()
                    Log.i(TAG, "🗑️ Mapping DELETED | keyword='$kw' id='${mapping.id}'")

                    if (kw.isNotBlank()) {
                        Log.d(TAG, "🔁 Recategorize triggered after DELETE for keyword='$kw'")
                        val r = recategorizeExpensesByKeywordUseCase(kw)
                        Log.i(TAG, "✅ Recategorize result deleted='$kw' => $r")
                    }

                    _state.update {
                        it.copy(
                            confirmDelete = null,
                            operationMessage = "Mapping deleted"
                        )
                    }
                    loadMappings()
                }


                is Result.Error -> _state.update {
                    it.copy(
                        errorMessage = res.message ?: "Failed to delete",
                        confirmDelete = null
                    )
                }

                else -> Unit
            }
        }
    }

    fun clearMessages() = _state.update { it.copy(operationMessage = null, errorMessage = null) }
}
