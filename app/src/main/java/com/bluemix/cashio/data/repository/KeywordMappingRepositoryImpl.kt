package com.bluemix.cashio.data.repository

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.core.common.resultOf
import com.bluemix.cashio.data.local.database.RealmManager
import com.bluemix.cashio.data.local.entity.KeywordMappingEntity
import com.bluemix.cashio.data.local.mapper.toDomain
import com.bluemix.cashio.data.local.mapper.toEntity
import com.bluemix.cashio.domain.model.KeywordMapping
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class KeywordMappingRepositoryImpl(
    private val realmManager: RealmManager
) : KeywordMappingRepository {

    private val realm get() = realmManager.realm

    // ── Reactive observation ───────────────────────────────────────────────

    override fun observeKeywordMappings(): Flow<List<KeywordMapping>> =
        realm.query<KeywordMappingEntity>()
            .sort("priority", Sort.DESCENDING)
            .asFlow()
            .map { changes -> changes.list.map { it.toDomain() } }

    // ── One-shot queries ───────────────────────────────────────────────────

    override suspend fun getAllKeywordMappings(): Result<List<KeywordMapping>> = resultOf {
        realm.query<KeywordMappingEntity>()
            .sort("priority", Sort.DESCENDING)
            .find()
            .map { it.toDomain() }
    }

    override suspend fun getKeywordMappingById(id: String): Result<KeywordMapping?> = resultOf {
        realm.query<KeywordMappingEntity>("id == $0", id).first().find()?.toDomain()
    }

    override suspend fun getKeywordMappingsByCategory(categoryId: String): Result<List<KeywordMapping>> =
        resultOf {
            realm.query<KeywordMappingEntity>("categoryId == $0", categoryId)
                .sort("priority", Sort.DESCENDING)
                .find()
                .map { it.toDomain() }
        }

    /**
     * Finds the best-matching category ID for [merchantName] using substring matching.
     *
     * Matching is case-insensitive containment: the keyword must appear somewhere in
     * the merchant name string. Short or ambiguous keywords (e.g. "vi", "water")
     * can produce false positives — keep default keywords specific (see [KeywordMapping.DEFAULT_KEYWORD_MAPPINGS]).
     *
     * Returns `null` if no rule matches, indicating the caller should use a fallback
     * category (typically "other").
     *
     * NOTE: This loads all mappings into memory on every call. For bulk SMS import,
     * load mappings once in the caller ([ExpenseRepositoryImpl.refreshExpensesFromSms])
     * and perform matching there rather than calling this method per transaction.
     */
    override suspend fun findCategoryForMerchant(merchantName: String): Result<String?> = resultOf {
        val normalized = merchantName.trim().lowercase(Locale.getDefault())
        if (normalized.isBlank()) return@resultOf null

        realm.query<KeywordMappingEntity>()
            .sort("priority", Sort.DESCENDING)
            .find()
            .firstOrNull { mapping ->
                val kw = mapping.keyword.trim().lowercase(Locale.getDefault())
                kw.isNotBlank() && kw in normalized
            }
            ?.categoryId
    }

    // ── Mutations ──────────────────────────────────────────────────────────

    override suspend fun addKeywordMapping(mapping: KeywordMapping): Result<Unit> = resultOf {
        realm.write { copyToRealm(mapping.toEntity()) }
        Unit
    }

    override suspend fun updateKeywordMapping(mapping: KeywordMapping): Result<Unit> = resultOf {
        realm.write {
            val existing = query<KeywordMappingEntity>("id == $0", mapping.id).first().find()
                ?: return@write   // Nothing to update
            existing.apply {
                keyword = mapping.keyword
                categoryId = mapping.categoryId
                priority = mapping.priority
            }
        }
    }

    override suspend fun deleteKeywordMapping(mappingId: String): Result<Unit> = resultOf {
        realm.write {
            val entity = query<KeywordMappingEntity>("id == $0", mappingId).first().find()
            if (entity != null) delete(entity)
        }
    }

    override suspend fun deleteKeywordMappingsByCategory(categoryId: String): Result<Unit> =
        resultOf {
            realm.write {
                val entities = query<KeywordMappingEntity>("categoryId == $0", categoryId).find()
                delete(entities)
            }
        }
}