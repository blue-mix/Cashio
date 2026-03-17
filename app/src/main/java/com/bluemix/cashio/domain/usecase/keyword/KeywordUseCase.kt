package com.bluemix.cashio.domain.usecase.keywordmapping

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.KeywordMapping
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import com.bluemix.cashio.domain.usecase.base.NoParamsUseCase
import com.bluemix.cashio.domain.usecase.base.UseCase
import com.bluemix.cashio.domain.usecase.expense.RecategorizeExpensesByKeywordUseCase
import kotlinx.coroutines.flow.Flow

// ── Observe ────────────────────────────────────────────────────────────────

/** Reactive stream of all keyword mappings, highest priority first. */
class ObserveKeywordMappingsUseCase(
    private val keywordMappingRepository: KeywordMappingRepository
) {
    operator fun invoke(): Flow<List<KeywordMapping>> =
        keywordMappingRepository.observeKeywordMappings()
}

/** One-shot fetch of all keyword mappings. */
class GetKeywordMappingsUseCase(
    private val keywordMappingRepository: KeywordMappingRepository
) : NoParamsUseCase<List<KeywordMapping>>() {
    override suspend fun execute(): Result<List<KeywordMapping>> =
        keywordMappingRepository.getAllKeywordMappings()
}

/** All mappings for a given [categoryId]. */
class GetKeywordMappingsByCategoryUseCase(
    private val keywordMappingRepository: KeywordMappingRepository
) : UseCase<String, List<KeywordMapping>>() {
    override suspend fun execute(params: String): Result<List<KeywordMapping>> =
        keywordMappingRepository.getKeywordMappingsByCategory(params)
}

// ── Shared validation ──────────────────────────────────────────────────────

/**
 * Validates a [KeywordMapping] before persistence.
 *
 * Priority range is **0–10** (0 is the lowest valid priority, matching defaults).
 * The original code rejected priority <= 0 which broke editing of default mappings
 * that legitimately use priority 0.
 */
private fun validateMapping(mapping: KeywordMapping): Result<Unit>? {
    if (mapping.id.isBlank()) {
        return Result.Error(
            IllegalArgumentException("Mapping id cannot be blank"),
            "Missing keyword mapping ID"
        )
    }
    if (mapping.keyword.isBlank()) {
        return Result.Error(
            IllegalArgumentException("Keyword cannot be blank"),
            "Keyword is required"
        )
    }
    if (mapping.categoryId.isBlank()) {
        return Result.Error(
            IllegalArgumentException("categoryId cannot be blank"),
            "Please select a category"
        )
    }
    if (mapping.priority < 0 || mapping.priority > 10) {
        return Result.Error(
            IllegalArgumentException("Priority must be between 0 and 10"),
            "Priority must be between 0 and 10"
        )
    }
    return null   // null = valid
}

// ── Add ────────────────────────────────────────────────────────────────────

/**
 * Validates and persists a new keyword mapping, then triggers retroactive
 * recategorisation of any existing expenses matching the new keyword.
 */
class AddKeywordMappingUseCase(
    private val keywordMappingRepository: KeywordMappingRepository,
    private val recategorizeExpensesUseCase: RecategorizeExpensesByKeywordUseCase
) : UseCase<KeywordMapping, Unit>() {

    override suspend fun execute(params: KeywordMapping): Result<Unit> {
        validateMapping(params)?.let { return it }

        val saveResult = keywordMappingRepository.addKeywordMapping(
            params.copy(keyword = params.keyword.trim().lowercase())
        )
        if (saveResult is Result.Error) return saveResult

        // Best-effort retroactive recategorisation — a failure here is non-fatal.
        // The mapping was saved successfully; historical re-tagging is advisory.
        recategorizeExpensesUseCase(params.keyword.trim())

        return Result.Success(Unit)
    }
}

// ── Update ─────────────────────────────────────────────────────────────────

/**
 * Validates and updates an existing keyword mapping, then triggers retroactive
 * recategorisation for the new keyword.
 */
class UpdateKeywordMappingUseCase(
    private val keywordMappingRepository: KeywordMappingRepository,
    private val recategorizeExpensesUseCase: RecategorizeExpensesByKeywordUseCase
) : UseCase<KeywordMapping, Unit>() {

    override suspend fun execute(params: KeywordMapping): Result<Unit> {
        validateMapping(params)?.let { return it }

        val updateResult = keywordMappingRepository.updateKeywordMapping(
            params.copy(keyword = params.keyword.trim().lowercase())
        )
        if (updateResult is Result.Error) return updateResult

        recategorizeExpensesUseCase(params.keyword.trim())

        return Result.Success(Unit)
    }
}

// ── Delete ─────────────────────────────────────────────────────────────────

/**
 * Deletes a single keyword mapping by id.
 * Does not retroactively undo previously applied categories — existing expenses
 * keep whatever category they were assigned.
 */
class DeleteKeywordMappingUseCase(
    private val keywordMappingRepository: KeywordMappingRepository
) : UseCase<String, Unit>() {
    override suspend fun execute(params: String): Result<Unit> =
        keywordMappingRepository.deleteKeywordMapping(params)
}

/**
 * Deletes all keyword mappings targeting [categoryId].
 * Called as part of the category deletion flow to maintain referential integrity.
 */
class DeleteKeywordMappingsByCategoryUseCase(
    private val keywordMappingRepository: KeywordMappingRepository
) : UseCase<String, Unit>() {
    override suspend fun execute(params: String): Result<Unit> =
        keywordMappingRepository.deleteKeywordMappingsByCategory(params)
}