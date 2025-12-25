package com.bluemix.cashio.domain.usecase.keyword

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.KeywordMapping
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import com.bluemix.cashio.domain.usecase.base.UseCase

/**
 * Update an existing keyword mapping (edit)
 */
class UpdateKeywordMappingUseCase(
    private val keywordMappingRepository: KeywordMappingRepository
) : UseCase<KeywordMapping, Unit>() {

    override suspend fun execute(params: KeywordMapping): Result<Unit> {
        // Validate id
        if (params.id.isBlank()) {
            return Result.Error(
                IllegalArgumentException("Mapping id cannot be empty"),
                "Invalid mapping. Please try again."
            )
        }

        // Validate keyword
        if (params.keyword.isBlank()) {
            return Result.Error(
                IllegalArgumentException("Keyword cannot be empty"),
                "Please enter a valid keyword"
            )
        }

        // Validate categoryId
        if (params.categoryId.isBlank()) {
            return Result.Error(
                IllegalArgumentException("Category cannot be empty"),
                "Please select a category"
            )
        }

        // Validate priority (optional guard)
        if (params.priority <= 0) {
            return Result.Error(
                IllegalArgumentException("Priority must be positive"),
                "Priority must be at least 1"
            )
        }

        return keywordMappingRepository.updateKeywordMapping(
            params.copy(keyword = params.keyword.trim())
        )
    }
}
