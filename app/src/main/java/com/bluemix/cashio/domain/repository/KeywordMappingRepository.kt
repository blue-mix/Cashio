package com.bluemix.cashio.domain.repository

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.KeywordMapping
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for KeywordMapping operations
 */
interface KeywordMappingRepository {

    /**
     * Observe all keyword mappings as Flow
     */
    fun observeKeywordMappings(): Flow<List<KeywordMapping>>

    /**
     * Get all keyword mappings (one-time)
     */
    suspend fun getAllKeywordMappings(): Result<List<KeywordMapping>>

    /**
     * Get keyword mapping by ID
     */
    suspend fun getKeywordMappingById(id: String): Result<KeywordMapping?>

    /**
     * Get keyword mappings for a category
     */
    suspend fun getKeywordMappingsByCategory(categoryId: String): Result<List<KeywordMapping>>

    /**
     * Find category for a merchant name using keyword matching
     */
    suspend fun findCategoryForMerchant(merchantName: String): Result<String?>

    /**
     * Add new keyword mapping
     */
    suspend fun addKeywordMapping(mapping: KeywordMapping): Result<Unit>

    /**
     * Update existing keyword mapping
     */
    suspend fun updateKeywordMapping(mapping: KeywordMapping): Result<Unit>

    /**
     * Delete keyword mapping
     */
    suspend fun deleteKeywordMapping(mappingId: String): Result<Unit>

    /**
     * Delete all mappings for a category
     */
    suspend fun deleteKeywordMappingsByCategory(categoryId: String): Result<Unit>
}
