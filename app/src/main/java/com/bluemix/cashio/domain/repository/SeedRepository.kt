package com.bluemix.cashio.domain.repository

import com.bluemix.cashio.core.common.Result

/**
 * Contract for database seeding.
 *
 * Seeding is an infrastructure concern but is exposed as an interface here so
 * the [com.bluemix.cashio.domain.usecase.base.SeedDatabaseUseCase] can depend
 * on an abstraction rather than a concrete class, keeping it testable.
 *
 * Implementations should be idempotent — calling [seedIfNeeded] multiple times
 * must never produce duplicate rows.
 *
 * @return `true` if data was written, `false` if already seeded.
 */
interface SeedRepository {
    suspend fun seedIfNeeded(): Result<Boolean>
}