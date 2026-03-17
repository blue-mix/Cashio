package com.bluemix.cashio.domain.repository

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.KeywordMapping
import kotlinx.coroutines.flow.Flow

/**
 * Contract for keyword mapping persistence operations.
 *
 * Keyword mappings drive auto-categorisation of SMS and notification transactions.
 * Higher [KeywordMapping.priority] rules are evaluated first.
 */
interface KeywordMappingRepository {

    /** Live stream of all keyword mappings, highest priority first. */
    fun observeKeywordMappings(): Flow<List<KeywordMapping>>

    /** All keyword mappings, highest priority first. */
    suspend fun getAllKeywordMappings(): Result<List<KeywordMapping>>

    /** Single mapping by [id], or `null` if not found. */
    suspend fun getKeywordMappingById(id: String): Result<KeywordMapping?>

    /** All mappings targeting [categoryId], highest priority first. */
    suspend fun getKeywordMappingsByCategory(categoryId: String): Result<List<KeywordMapping>>

    /**
     * Finds the best-matching category ID for [merchantName] by scanning
     * all keyword mappings in priority order.
     *
     * Returns `null` if no rule matches — callers should fall back to "other".
     *
     * NOTE: Loads all mappings on every call. For bulk import operations,
     * load mappings once via [getAllKeywordMappings] and perform matching
     * in the caller to avoid repeated full-table reads.
     */
    suspend fun findCategoryForMerchant(merchantName: String): Result<String?>

    /** Persist a new keyword mapping. */
    suspend fun addKeywordMapping(mapping: KeywordMapping): Result<Unit>

    /** Update keyword, category, and priority of an existing mapping. */
    suspend fun updateKeywordMapping(mapping: KeywordMapping): Result<Unit>

    /** Delete a single mapping by [mappingId]. */
    suspend fun deleteKeywordMapping(mappingId: String): Result<Unit>

    /** Delete all mappings targeting [categoryId] (used before category deletion). */
    suspend fun deleteKeywordMappingsByCategory(categoryId: String): Result<Unit>
}