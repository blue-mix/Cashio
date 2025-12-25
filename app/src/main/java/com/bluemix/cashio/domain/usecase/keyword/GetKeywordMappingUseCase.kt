package com.bluemix.cashio.domain.usecase.keyword

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.KeywordMapping
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import com.bluemix.cashio.domain.usecase.base.NoParamsUseCase

/**
 * Get all keyword mappings
 */
class GetKeywordMappingsUseCase(
    private val keywordMappingRepository: KeywordMappingRepository
) : NoParamsUseCase<List<KeywordMapping>>() {

    override suspend fun execute(): Result<List<KeywordMapping>> {
        return keywordMappingRepository.getAllKeywordMappings()
    }
}
