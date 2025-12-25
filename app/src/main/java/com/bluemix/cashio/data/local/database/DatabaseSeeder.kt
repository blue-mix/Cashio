package com.bluemix.cashio.data.local.database

import com.bluemix.cashio.data.local.entity.CategoryEntity
import com.bluemix.cashio.data.local.entity.KeywordMappingEntity
import com.bluemix.cashio.data.local.mapper.toEntity
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.KeywordMapping
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {

    suspend fun seedDefaultCategories(realm: Realm): Boolean = withContext(Dispatchers.IO) {
        val existingCount = realm.query<CategoryEntity>().count().find()
        if (existingCount > 0) return@withContext false

        val defaultCategories = Category.getDefaultCategories()
        realm.write {
            defaultCategories.forEach { category ->
                copyToRealm(category.toEntity())
            }
        }
        println("✅ Seeded ${defaultCategories.size} default categories")
        true
    }

    suspend fun seedDefaultKeywordMappings(realm: Realm): Boolean = withContext(Dispatchers.IO) {
        val existingCount = realm.query<KeywordMappingEntity>().count().find()
        if (existingCount > 0) return@withContext false

        val defaultMappings = KeywordMapping.getDefaultKeywordMappings()
        realm.write {
            defaultMappings.forEach { mapping ->
                copyToRealm(mapping.toEntity())
            }
        }
        println("✅ Seeded ${defaultMappings.size} default keyword mappings")
        true
    }

    /** ✅ returns true if anything was seeded */
    suspend fun seedAll(realm: Realm): Boolean {
        val seededCategories = seedDefaultCategories(realm)
        val seededMappings = seedDefaultKeywordMappings(realm)
        return seededCategories || seededMappings
    }
}
