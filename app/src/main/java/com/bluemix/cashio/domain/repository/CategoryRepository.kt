package com.bluemix.cashio.domain.repository

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Category operations
 */
interface CategoryRepository {

    /**
     * Observe all categories as Flow
     */
    fun observeCategories(): Flow<List<Category>>

    /**
     * Get all categories (one-time)
     */
    suspend fun getAllCategories(): Result<List<Category>>

    /**
     * Get category by ID
     */
    suspend fun getCategoryById(id: String): Result<Category?>

    /**
     * Get default categories
     */
    suspend fun getDefaultCategories(): Result<List<Category>>

    /**
     * Add new category
     */
    suspend fun addCategory(category: Category): Result<Unit>

    /**
     * Update existing category
     */
    suspend fun updateCategory(category: Category): Result<Unit>

    /**
     * Delete category
     */
    suspend fun deleteCategory(categoryId: String): Result<Unit>

    /**
     * Check if category is in use by any expense
     */
    suspend fun isCategoryInUse(categoryId: String): Result<Boolean>
}
