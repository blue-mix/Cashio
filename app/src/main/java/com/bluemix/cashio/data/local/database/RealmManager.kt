package com.bluemix.cashio.data.local.database

import android.util.Log
import com.bluemix.cashio.data.local.database.RealmManager.Companion.SCHEMA_VERSION
import com.bluemix.cashio.data.local.entity.CategoryEntity
import com.bluemix.cashio.data.local.entity.ExpenseEntity
import com.bluemix.cashio.data.local.entity.KeywordMappingEntity
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.migration.AutomaticSchemaMigration

/**
 * Owns the single [Realm] instance for the application.
 *
 * ## Lifecycle
 * Must be registered as a Koin [single] so only one instance ever exists.
 * [close] should be called from [android.app.Application.onTerminate] or a
 * Koin lifecycle hook — not from individual screens.
 *
 * ## Opening the database
 * Call [open] explicitly from a background coroutine during app startup
 * (e.g. in [com.bluemix.cashio.domain.usecase.base.SeedDatabaseUseCase])
 * rather than relying on [lazy] to defer opening to the first UI access,
 * which risks a blocking call on the main thread.
 *
 * ## Schema versioning
 * Increment [SCHEMA_VERSION] whenever you add, remove, or rename a field.
 * Document each version in the table below.
 *
 * | Version | Change                                      |
 * |---------|---------------------------------------------|
 * |       1 | Initial schema                              |
 * |       2 | ExpenseEntity: amount Double → amountPaise Long; CategoryEntity: +sortOrder |
 */
class RealmManager {

    companion object {
        private const val TAG = "RealmManager"
        private const val DB_NAME = "cashio.realm"
        private const val SCHEMA_VERSION = 2L

        // Compact when file > 50 MB AND less than 50 % of space is used.
        private const val COMPACT_SIZE_THRESHOLD_BYTES = 50L * 1024L * 1024L
        private const val COMPACT_USAGE_RATIO = 0.5
    }

    @Volatile
    private var _realm: Realm? = null

    val realm: Realm
        get() = _realm ?: error(
            "Realm has not been opened yet. Call RealmManager.open() during app startup."
        )

    /**
     * Opens the Realm database. Safe to call multiple times — subsequent calls are no-ops.
     * Must be called from a background thread / coroutine.
     */
    fun open() {
        if (_realm != null) return

        val config = RealmConfiguration.Builder(
            schema = setOf(
                ExpenseEntity::class,
                CategoryEntity::class,
                KeywordMappingEntity::class
            )
        )
            .name(DB_NAME)
            .schemaVersion(SCHEMA_VERSION)
            .migration(AutomaticSchemaMigration { context ->
                // AutomaticSchemaMigration handles field additions/removals automatically.
                // Add manual migration steps here for field renames or type changes.
                Log.i(
                    TAG,
                    "Migrating schema: ${context.oldRealm.schemaVersion()} → ${context.newRealm.schemaVersion()}"
                )
            })
            .compactOnLaunch { totalBytes, usedBytes ->
                totalBytes > COMPACT_SIZE_THRESHOLD_BYTES &&
                        (usedBytes.toDouble() / totalBytes) < COMPACT_USAGE_RATIO
            }
            .build()

        _realm = Realm.open(config)
        Log.i(TAG, "Realm opened: $DB_NAME v$SCHEMA_VERSION")
    }

    /**
     * Closes the Realm instance. Should only be called from application shutdown.
     */
    fun close() {
        _realm?.takeIf { !it.isClosed() }?.close()
        _realm = null
        Log.i(TAG, "Realm closed")
    }
}