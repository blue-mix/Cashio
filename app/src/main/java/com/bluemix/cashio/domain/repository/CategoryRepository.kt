package com.bluemix.cashio.domain.repository

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Contract for category persistence operations.
 *
 * All suspend functions are safe to call from any coroutine context —
 * implementations manage their own threading internally.
 */
interface CategoryRepository {

    /** Reactive stream of all categories, ordered by display sort order. */
    fun observeCategories(): Flow<List<Category>>

    /** One-shot fetch of all categories. */
    suspend fun getAllCategories(): Result<List<Category>>

    /** Fetch a single category by [id], or `null` if it does not exist. */
    suspend fun getCategoryById(id: String): Result<Category?>

    /** Persist a new category. Fails if [category.id] already exists. */
    suspend fun addCategory(category: Category): Result<Unit>

    /** Update all mutable fields of an existing category. */
    suspend fun updateCategory(category: Category): Result<Unit>

    /**
     * Delete a category by [categoryId].
     *
     * If [forceDelete] is `false` (default) and the category has associated
     * expenses, returns [Result.Error] with [IllegalStateException].
     * If [forceDelete] is `true`, expenses referencing the category are
     * reassigned to the "other" fallback before deletion.
     */
    suspend fun deleteCategory(categoryId: String, forceDelete: Boolean = false): Result<Unit>

    /** Returns `true` if any expense references [categoryId]. */
    suspend fun isCategoryInUse(categoryId: String): Result<Boolean>
}