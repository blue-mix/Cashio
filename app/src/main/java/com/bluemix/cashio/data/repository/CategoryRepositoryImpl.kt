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
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val realmManager: RealmManager
) : CategoryRepository {

    private val realm get() = realmManager.realm

    // ── Reactive observation ───────────────────────────────────────────────

    override fun observeCategories(): Flow<List<Category>> =
        realm.query<CategoryEntity>()
            .sort("sortOrder", Sort.ASCENDING)   // Preserves semantic grouping from seed
            .asFlow()
            .map { changes -> changes.list.map { it.toDomain() } }

    // ── One-shot queries ───────────────────────────────────────────────────

    override suspend fun getAllCategories(): Result<List<Category>> = resultOf {
        realm.query<CategoryEntity>()
            .sort("sortOrder", Sort.ASCENDING)
            .find()
            .map { it.toDomain() }
    }

    override suspend fun getCategoryById(id: String): Result<Category?> = resultOf {
        realm.query<CategoryEntity>("id == $0", id).first().find()?.toDomain()
    }

    // ── Mutations ──────────────────────────────────────────────────────────

    override suspend fun addCategory(category: Category): Result<Unit> = resultOf {
        realm.write {
            // Assign sortOrder = current max + 1 so user-created categories appear at the bottom.
            val maxOrder = query<CategoryEntity>()
                .sort("sortOrder", Sort.DESCENDING)
                .first()
                .find()
                ?.sortOrder ?: -1
            copyToRealm(category.toEntity(sortOrder = maxOrder + 1))
        }
        Unit
    }

    override suspend fun updateCategory(category: Category): Result<Unit> = resultOf {
        realm.write {
            val existing = query<CategoryEntity>("id == $0", category.id).first().find()
                ?: return@write   // Nothing to update — silently succeed
            existing.apply {
                name = category.name
                icon = category.icon
                // Convert Long colorHex → Int ARGB, preserving all bits via unsigned mask
                colorArgb = (category.colorHex and 0xFFFFFFFFL).toInt()
                isDefault = category.isDefault
                // sortOrder intentionally not changed on update
            }
        }
    }

    /**
     * Deletes a category.
     *
     * If [forceDelete] is false (default) and the category has associated expenses,
     * returns [Result.Error] — the caller must explicitly pass [forceDelete] = true
     * to override, and should first offer the user a reassignment flow.
     *
     * If [forceDelete] is true, expenses referencing this category are reassigned
     * to the "other" fallback category before deletion to maintain referential integrity.
     */
    override suspend fun deleteCategory(
        categoryId: String,
        forceDelete: Boolean
    ): Result<Unit> = resultOf {
        realm.write {
            val inUseCount = query<ExpenseEntity>("categoryId == $0", categoryId).count().find()

            if (inUseCount > 0L && !forceDelete) {
                // Throw inside the write block — Realm rolls back, resultOf catches.
                throw IllegalStateException(
                    "Category '$categoryId' is used by $inUseCount expense(s). " +
                            "Pass forceDelete = true to reassign and delete."
                )
            }

            if (inUseCount > 0L) {
                // Reassign all referencing expenses to the fallback "other" category.
                query<ExpenseEntity>("categoryId == $0", categoryId).find().forEach { expense ->
                    findLatest(expense)?.categoryId = "other"
                }
            }

            val entity = query<CategoryEntity>("id == $0", categoryId).first().find()
            if (entity != null) delete(entity)
        }
    }

    override suspend fun isCategoryInUse(categoryId: String): Result<Boolean> = resultOf {
        realm.query<ExpenseEntity>("categoryId == $0", categoryId).count().find() > 0
    }
}