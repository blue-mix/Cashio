package com.bluemix.cashio.domain.usecase.category

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.usecase.base.UseCase

/**
 * Validates and persists changes to an existing [Category].
 */
class UpdateCategoryUseCase(
    private val categoryRepository: CategoryRepository
) : UseCase<Category, Unit>() {

    override suspend fun execute(params: Category): Result<Unit> {
        if (params.id.isBlank()) {
            return Result.Error(
                IllegalArgumentException("Category id cannot be blank"),
                "Cannot update a category without an ID"
            )
        }
        if (params.name.isBlank()) {
            return Result.Error(
                IllegalArgumentException("Category name cannot be blank"),
                "Category name is required"
            )
        }
        return categoryRepository.updateCategory(params.copy(name = params.name.trim()))
    }
}