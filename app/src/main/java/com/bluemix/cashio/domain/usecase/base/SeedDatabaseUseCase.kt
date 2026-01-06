package com.bluemix.cashio.domain.usecase.base

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SeedDatabaseUseCase(
    private val categoryRepository: CategoryRepository,
    private val keywordMappingRepository: KeywordMappingRepository
) {
    suspend operator fun invoke(): Result<Unit> = coroutineScope {
        // Run both seeds in parallel for faster startup
        val catJob = async { categoryRepository.seedDefaults() }
        val kwJob = async { keywordMappingRepository.seedDefaults() }

        catJob.await()
        kwJob.await()

        Result.Success(Unit)
    }
}