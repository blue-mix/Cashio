//package com.bluemix.cashio.data.repository
//
//import com.bluemix.cashio.core.common.Result
//import com.bluemix.cashio.core.common.resultOf
//import com.bluemix.cashio.data.local.database.RealmManager
//import com.bluemix.cashio.data.local.entity.KeywordMappingEntity
//import com.bluemix.cashio.data.local.mapper.toDomain
//import com.bluemix.cashio.data.local.mapper.toEntity
//import com.bluemix.cashio.domain.model.KeywordMapping
//import com.bluemix.cashio.domain.repository.KeywordMappingRepository
//import io.realm.kotlin.ext.query
//import io.realm.kotlin.notifications.ResultsChange
//import io.realm.kotlin.query.Sort
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.withContext
//import java.util.Locale
//
///**
// * Implementation of KeywordMappingRepository using Realm
// */
//class KeywordMappingRepositoryImpl(
//    private val realmManager: RealmManager
//) : KeywordMappingRepository {
//
//    private val realm get() = realmManager.realm
//
//    override fun observeKeywordMappings(): Flow<List<KeywordMapping>> {
//        return realm.query<KeywordMappingEntity>()
//            .sort("priority", Sort.DESCENDING)
//            .asFlow()
//            .map { resultsChange: ResultsChange<KeywordMappingEntity> ->
//                resultsChange.list.map { it.toDomain() }
//            }
//    }
//
//    override suspend fun getAllKeywordMappings(): Result<List<KeywordMapping>> =
//        withContext(Dispatchers.IO) {
//            resultOf {
//                realm.query<KeywordMappingEntity>()
//                    .sort("priority", Sort.DESCENDING)
//                    .find()
//                    .map { it.toDomain() }
//            }
//        }
//
//    override suspend fun getKeywordMappingById(id: String): Result<KeywordMapping?> =
//        withContext(Dispatchers.IO) {
//            resultOf {
//                realm.query<KeywordMappingEntity>("id == $0", id)
//                    .first()
//                    .find()
//                    ?.toDomain()
//            }
//        }
//
//    override suspend fun getKeywordMappingsByCategory(categoryId: String): Result<List<KeywordMapping>> =
//        withContext(Dispatchers.IO) {
//            resultOf {
//                realm.query<KeywordMappingEntity>("categoryId == $0", categoryId)
//                    .sort("priority", Sort.DESCENDING)
//                    .find()
//                    .map { it.toDomain() }
//            }
//        }
//
//    override suspend fun findCategoryForMerchant(merchantName: String): Result<String?> =
//        withContext(Dispatchers.IO) {
//            resultOf {
//                if (merchantName.isBlank()) return@resultOf null
//
//                val lowerMerchant = merchantName.lowercase(Locale.getDefault())
//
//                // Get all keyword mappings sorted by priority
//                val mappings = realm.query<KeywordMappingEntity>()
//                    .sort("priority", Sort.DESCENDING)
//                    .find()
//
//                // Find first matching keyword
//                for (mapping in mappings) {
//                    val keyword = mapping.keyword.lowercase(Locale.getDefault())
//                    if (keyword in lowerMerchant) {
//                        return@resultOf mapping.categoryId
//                    }
//                }
//
//                null  // No match found
//            }
//        }
//
//    override suspend fun addKeywordMapping(mapping: KeywordMapping): Result<Unit> =
//        withContext(Dispatchers.IO) {
//            resultOf {
//                realm.write {
//                    val entity = mapping.toEntity()
//                    copyToRealm(entity)
//                }
//                    .let { }
//            }
//
//        }
//
//    override suspend fun updateKeywordMapping(mapping: KeywordMapping): Result<Unit> =
//        withContext(Dispatchers.IO) {
//            resultOf {
//                realm.write {
//                    val existingEntity = query<KeywordMappingEntity>("id == $0", mapping.id)
//                        .first()
//                        .find()
//
//                    if (existingEntity != null) {
//                        existingEntity.apply {
//                            keyword = mapping.keyword
//                            categoryId = mapping.categoryId
//                            priority = mapping.priority
//                        }
//                    }
//                }
//            }
//        }
//
//    override suspend fun deleteKeywordMapping(mappingId: String): Result<Unit> =
//        withContext(Dispatchers.IO) {
//            resultOf {
//                realm.write {
//                    val entity = query<KeywordMappingEntity>("id == $0", mappingId)
//                        .first()
//                        .find()
//
//                    if (entity != null) {
//                        delete(entity)
//                    }
//                }
//            }
//        }
//
//    override suspend fun deleteKeywordMappingsByCategory(categoryId: String): Result<Unit> =
//        withContext(Dispatchers.IO) {
//            resultOf {
//                realm.write {
//                    val entities = query<KeywordMappingEntity>("categoryId == $0", categoryId)
//                        .find()
//
//                    delete(entities)
//                }
//            }
//        }
//}
package com.bluemix.cashio.data.repository

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.core.common.resultOf
import com.bluemix.cashio.data.local.database.DatabaseSeeder
import com.bluemix.cashio.data.local.database.RealmManager
import com.bluemix.cashio.data.local.entity.KeywordMappingEntity
import com.bluemix.cashio.data.local.mapper.toDomain
import com.bluemix.cashio.data.local.mapper.toEntity
import com.bluemix.cashio.domain.model.KeywordMapping
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class KeywordMappingRepositoryImpl(
    private val realmManager: RealmManager
) : KeywordMappingRepository {

    private val realm get() = realmManager.realm

    override suspend fun seedDefaults(): Result<Boolean> = withContext(Dispatchers.IO) {
        resultOf {
            // Uses your DatabaseSeeder object
            DatabaseSeeder.seedDefaultKeywordMappings(realmManager.realm)
        }
    }

    override fun observeKeywordMappings(): Flow<List<KeywordMapping>> {
        return realm.query<KeywordMappingEntity>()
            .sort("priority", Sort.DESCENDING)
            .asFlow()
            .map { changes -> changes.list.map { it.toDomain() } }
    }

    override suspend fun getAllKeywordMappings(): Result<List<KeywordMapping>> =
        withContext(Dispatchers.IO) {
            resultOf {
                realm.query<KeywordMappingEntity>()
                    .sort("priority", Sort.DESCENDING)
                    .find()
                    .map { it.toDomain() }
            }
        }

    override suspend fun getKeywordMappingById(id: String): Result<KeywordMapping?> =
        withContext(Dispatchers.IO) {
            resultOf {
                realm.query<KeywordMappingEntity>("id == $0", id).first().find()?.toDomain()
            }
        }

    override suspend fun getKeywordMappingsByCategory(categoryId: String): Result<List<KeywordMapping>> =
        withContext(Dispatchers.IO) {
            resultOf {
                realm.query<KeywordMappingEntity>("categoryId == $0", categoryId)
                    .sort("priority", Sort.DESCENDING)
                    .find()
                    .map { it.toDomain() }
            }
        }

    override suspend fun findCategoryForMerchant(merchantName: String): Result<String?> =
        withContext(Dispatchers.IO) {
            resultOf {
                if (merchantName.isBlank()) return@resultOf null

                // Optimization: Fetch once, filter in memory
                val mappings = realm.query<KeywordMappingEntity>()
                    .sort("priority", Sort.DESCENDING)
                    .find()

                val lowerMerchant = merchantName.lowercase()
                val match = mappings.firstOrNull { it.keyword.lowercase() in lowerMerchant }

                match?.categoryId
            }
        }

    override suspend fun addKeywordMapping(mapping: KeywordMapping): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                realm.write { copyToRealm(mapping.toEntity()) }
                Unit
            }
        }

    override suspend fun updateKeywordMapping(mapping: KeywordMapping): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                realm.write {
                    val existing =
                        query<KeywordMappingEntity>("id == $0", mapping.id).first().find()
                    existing?.apply {
                        keyword = mapping.keyword
                        categoryId = mapping.categoryId
                        priority = mapping.priority
                    }
                }
                Unit
            }
        }

    override suspend fun deleteKeywordMapping(mappingId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                realm.write {
                    val entity = query<KeywordMappingEntity>("id == $0", mappingId).first().find()
                    if (entity != null) delete(entity)
                }
            }
        }

    override suspend fun deleteKeywordMappingsByCategory(categoryId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                realm.write {
                    val entities =
                        query<KeywordMappingEntity>("categoryId == $0", categoryId).find()
                    delete(entities)
                }
            }
        }
}