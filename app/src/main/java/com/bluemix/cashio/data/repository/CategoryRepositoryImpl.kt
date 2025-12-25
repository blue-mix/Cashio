package com.bluemix.cashio.data.repository

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.core.common.resultOf
import com.bluemix.cashio.data.local.database.RealmManager
import com.bluemix.cashio.data.local.entity.CategoryEntity
import com.bluemix.cashio.data.local.entity.ExpenseEntity
import com.bluemix.cashio.data.local.mapper.toDomain
import com.bluemix.cashio.data.local.mapper.toEntity
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.repository.CategoryRepository
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Implementation of CategoryRepository using Realm
 */
class CategoryRepositoryImpl(
    private val realmManager: RealmManager
) : CategoryRepository {

    private val realm get() = realmManager.realm

    override fun observeCategories(): Flow<List<Category>> {
        return realm.query<CategoryEntity>()
            .sort("name")
            .asFlow()
            .map { resultsChange: ResultsChange<CategoryEntity> ->
                resultsChange.list.map { it.toDomain() }
            }
    }

    override suspend fun getAllCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        resultOf {
            realm.query<CategoryEntity>()
                .sort("name")
                .find()
                .map { it.toDomain() }
        }
    }

    override suspend fun getCategoryById(id: String): Result<Category?> =
        withContext(Dispatchers.IO) {
            resultOf {
                realm.query<CategoryEntity>("id == $0", id)
                    .first()
                    .find()
                    ?.toDomain()
            }
        }

    override suspend fun getDefaultCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            resultOf {
                realm.query<CategoryEntity>("isDefault == true")
                    .sort("name")
                    .find()
                    .map { it.toDomain() }
            }
        }

    override suspend fun addCategory(category: Category): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                realm.write {
                    val entity = category.toEntity()
                    copyToRealm(entity)
                }
                    .let { }
            }
        }

    override suspend fun updateCategory(category: Category): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                realm.write {
                    val existingEntity = query<CategoryEntity>("id == $0", category.id)
                        .first()
                        .find()

                    if (existingEntity != null) {
                        existingEntity.apply {
                            name = category.name
                            icon = category.icon
                            color = category.color
                            isDefault = category.isDefault
                        }
                    }
                }
            }
        }

    override suspend fun deleteCategory(categoryId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                realm.write {
                    val entity = query<CategoryEntity>("id == $0", categoryId)
                        .first()
                        .find()

                    if (entity != null) {
                        delete(entity)
                    }
                }
            }
        }

    override suspend fun isCategoryInUse(categoryId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            resultOf {
                val count = realm.query<ExpenseEntity>("categoryId == $0", categoryId)
                    .count()
                    .find()
                count > 0
            }
        }
}
