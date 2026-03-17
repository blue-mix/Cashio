package com.bluemix.cashio.data.repository

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.core.common.resultOf
import com.bluemix.cashio.data.local.database.DatabaseSeeder
import com.bluemix.cashio.data.local.database.RealmManager
import com.bluemix.cashio.domain.repository.SeedRepository

class SeedRepositoryImpl(
    private val realmManager: RealmManager
) : SeedRepository {

    /**
     * Delegates to [DatabaseSeeder.seedAll].
     *
     * [DatabaseSeeder] uses [Realm.write] which manages its own internal dispatcher —
     * wrapping in [kotlinx.coroutines.Dispatchers.IO] here would be redundant and
     * potentially misleading.
     */
    override suspend fun seedIfNeeded(): Result<Boolean> = resultOf {
        DatabaseSeeder.seedAll(realmManager.realm)
    }
}