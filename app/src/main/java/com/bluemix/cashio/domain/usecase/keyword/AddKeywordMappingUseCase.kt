package com.bluemix.cashio.domain.usecase.keyword

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.KeywordMapping
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import com.bluemix.cashio.domain.usecase.base.UseCase

/**
 * Add a new keyword mapping
 */
class AddKeywordMappingUseCase(
    private val keywordMappingRepository: KeywordMappingRepository
) : UseCase<KeywordMapping, Unit>() {

    override suspend fun execute(params: KeywordMapping): Result<Unit> {
        // Validate keyword is not empty
        if (params.keyword.isBlank()) {
            return Result.Error(
                IllegalArgumentException("Keyword cannot be empty"),
                "Please enter a valid keyword"
            )
        }

        return keywordMappingRepository.addKeywordMapping(params)
    }
}
