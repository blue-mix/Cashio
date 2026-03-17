package com.bluemix.cashio.data.local.database

import android.util.Log
import com.bluemix.cashio.data.local.database.DatabaseSeeder.seedAll
import com.bluemix.cashio.data.local.entity.CategoryEntity
import com.bluemix.cashio.data.local.entity.KeywordMappingEntity
import com.bluemix.cashio.data.local.mapper.toEntity
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.KeywordMapping
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query

/**
 * Seeds default [CategoryEntity] and [KeywordMappingEntity] rows on first launch.
 *
 * ## Race-condition safety
 * The count check and the write happen inside a **single** [Realm.write] transaction.
 * Re-checking inside the write block ensures correctness even if two coroutines
 * call [seedAll] concurrently (e.g. background sync + foreground launch).
 *
 * ## Dispatcher
 * Realm Kotlin manages its own internal write dispatcher — do not wrap calls
 * in [kotlinx.coroutines.Dispatchers.IO]. [Realm.write] is already a suspending
 * function that runs off the calling coroutine's thread.
 */
object DatabaseSeeder {

    private const val TAG = "DatabaseSeeder"

    /**
     * Seeds default data if the database is empty.
     *
     * @return `true` if anything was written, `false` if already seeded.
     */
    suspend fun seedAll(realm: Realm): Boolean {
        // Re-check counts INSIDE the write transaction to eliminate the TOCTOU race.
        var didSeed = false

        realm.write {
            val catCount = query<CategoryEntity>().count().find()
            val mapCount = query<KeywordMappingEntity>().count().find()

            if (catCount == 0L) {
                Category.DEFAULT_CATEGORIES.forEachIndexed { index, category ->
                    copyToRealm(category.toEntity(sortOrder = index))
                }
                Log.i(TAG, "Seeded ${Category.DEFAULT_CATEGORIES.size} categories")
                didSeed = true
            }

            if (mapCount == 0L) {
                KeywordMapping.DEFAULT_KEYWORD_MAPPINGS.forEach { mapping ->
                    copyToRealm(mapping.toEntity())
                }
                Log.i(
                    TAG,
                    "Seeded ${KeywordMapping.DEFAULT_KEYWORD_MAPPINGS.size} keyword mappings"
                )
                didSeed = true
            }
        }

        return didSeed
    }
}