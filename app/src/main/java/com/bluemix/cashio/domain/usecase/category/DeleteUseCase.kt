package com.bluemix.cashio.domain.usecase.category

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.usecase.base.UseCase

/**
 * Delete a category (with validation)
 */
class DeleteCategoryUseCase(
    private val categoryRepository: CategoryRepository
) : UseCase<DeleteCategoryUseCase.Params, Unit>() {

    data class Params(
        val categoryId: String,
        val forceDelete: Boolean = false  // If true, delete even if in use
    )

    override suspend fun execute(params: Params): Result<Unit> {
        // Check if category is in use
        if (!params.forceDelete) {
            val inUseResult = categoryRepository.isCategoryInUse(params.categoryId)
            if (inUseResult is Result.Success && inUseResult.data) {
                return Result.Error(
                    IllegalStateException("Cannot delete category that is in use"),
                    "This category is being used by expenses. Please reassign or delete those expenses first."
                )
            }
        }

        return categoryRepository.deleteCategory(params.categoryId)
    }
}
