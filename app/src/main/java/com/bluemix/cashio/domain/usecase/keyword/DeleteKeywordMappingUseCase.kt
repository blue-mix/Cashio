package com.bluemix.cashio.domain.usecase.keyword

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import com.bluemix.cashio.domain.usecase.base.UseCase

/**
 * Delete a keyword mapping
 */
class DeleteKeywordMappingUseCase(
    private val keywordMappingRepository: KeywordMappingRepository
) : UseCase<String, Unit>() {

    override suspend fun execute(params: String): Result<Unit> {
        return keywordMappingRepository.deleteKeywordMapping(params)
    }
}
