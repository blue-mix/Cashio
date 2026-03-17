package com.bluemix.cashio.domain.usecase.preferences

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.core.common.resultOf
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import com.bluemix.cashio.domain.usecase.base.NoParamsUseCase

/**
 * Clears all user data from the database.
 *
 * **WARNING:** This is a destructive operation that cannot be undone.
 * All expenses, custom categories, and keyword mappings will be permanently deleted.
 *
 * Default categories (the ones seeded during onboarding) are NOT deleted -
 * they will need to be re-seeded if the user wants them back.
 *
 * Use cases:
 * - User wants to start fresh
 * - Testing/debugging during development
 * - User is switching to a different device
 */
class ClearAllDataUseCase(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val keywordMappingRepository: KeywordMappingRepository
) : NoParamsUseCase<Unit>() {

    override suspend fun execute(): Result<Unit> = resultOf {
        // Delete in order: expenses first (they reference categories),
        // then mappings, then categories

        // 1. Delete all expenses
        val allExpenses = when (val result = expenseRepository.getAllExpenses()) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception
            else -> emptyList()
        }

        if (allExpenses.isNotEmpty()) {
            val expenseIds = allExpenses.map { it.id }
            when (val deleteResult = expenseRepository.deleteExpenses(expenseIds)) {
                is Result.Error -> throw deleteResult.exception
                else -> Unit
            }
        }

        // 2. Delete all keyword mappings
        val allMappings = when (val result = keywordMappingRepository.getAllKeywordMappings()) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception
            else -> emptyList()
        }

        allMappings.forEach { mapping ->
            when (val deleteResult = keywordMappingRepository.deleteKeywordMapping(mapping.id)) {
                is Result.Error -> throw deleteResult.exception
                else -> Unit
            }
        }

        // 3. Delete all custom categories (keep defaults for now)
        val allCategories = when (val result = categoryRepository.getAllCategories()) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception
            else -> emptyList()
        }

        allCategories.filter { !it.isDefault }.forEach { category ->
            // Force delete even if in use (expenses are already deleted)
            when (val deleteResult =
                categoryRepository.deleteCategory(category.id, forceDelete = true)) {
                is Result.Error -> throw deleteResult.exception
                else -> Unit
            }
        }
    }
}