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
        val catJob = async { categoryRepository.seedDefaults() }
        val kwJob = async { keywordMappingRepository.seedDefaults() }

        val catRes = catJob.await()
        val kwRes = kwJob.await()

        when {
            catRes is Result.Error -> catRes
            kwRes is Result.Error -> kwRes
            else -> Result.Success(Unit)
        }
    }
}