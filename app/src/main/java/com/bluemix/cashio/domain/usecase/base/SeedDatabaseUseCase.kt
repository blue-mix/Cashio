package com.bluemix.cashio.domain.usecase.base

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.repository.SeedRepository

/**
 * Seeds default categories and keyword mappings on first launch.
 *
 * Returns [Result.Success] with `true` if data was written, `false` if already seeded.
 * Returns [Result.Error] with the original exception if seeding fails — the caller
 * must handle this and must NOT mark onboarding as complete until this succeeds.
 */
class SeedDatabaseUseCase(
    private val seedRepository: SeedRepository
) : NoParamsUseCase<Boolean>() {

    override suspend fun execute(): Result<Boolean> = seedRepository.seedIfNeeded()
}