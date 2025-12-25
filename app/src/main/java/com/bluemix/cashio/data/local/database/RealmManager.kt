package com.bluemix.cashio.data.local.database

import com.bluemix.cashio.data.local.entity.CategoryEntity
import com.bluemix.cashio.data.local.entity.ExpenseEntity
import com.bluemix.cashio.data.local.entity.KeywordMappingEntity
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

/**
 * Manages Realm database instance
 */
class RealmManager {
    val realm: Realm by lazy {
        val config = RealmConfiguration.Builder(
            schema = setOf(
                ExpenseEntity::class,
                CategoryEntity::class,
                KeywordMappingEntity::class
            )
        )
            .name("cashio.realm")
            .schemaVersion(1)
            .compactOnLaunch()
            .build()

        Realm.open(config)
    }

    fun close() {
        if (!realm.isClosed()) {
            realm.close()
        }
    }
}
