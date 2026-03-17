package com.bluemix.cashio.domain.usecase.category

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.usecase.base.UseCase

/**
 * Validates and persists a new [Category].
 *
 * Validation rules:
 * - [Category.id] must not be blank.
 * - [Category.name] must not be blank after trimming.
 */
class AddCategoryUseCase(
    private val categoryRepository: CategoryRepository
) : UseCase<Category, Unit>() {

    override suspend fun execute(params: Category): Result<Unit> {
        if (params.id.isBlank()) {
            return Result.Error(
                IllegalArgumentException("Category id cannot be blank"),
                "Invalid category — missing ID"
            )
        }
        if (params.name.isBlank()) {
            return Result.Error(
                IllegalArgumentException("Category name cannot be blank"),
                "Category name is required"
            )
        }
        return categoryRepository.addCategory(params.copy(name = params.name.trim()))
    }
}