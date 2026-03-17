package com.bluemix.cashio.domain.usecase.category

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.usecase.base.UseCase

/**
 * Deletes a category with safety checks.
 *
 * ## In-use guard
 * If [Params.forceDelete] is `false` (default) and the category is referenced
 * by existing expenses, returns [Result.Error]. The ViewModel should surface a
 * confirmation dialog offering the user a choice to force-delete (which reassigns
 * referencing expenses to "other") or cancel.
 *
 * If [Params.forceDelete] is `true`, the repository handles reassignment and
 * deletion atomically in a single transaction.
 *
 * ## Fail-closed on check error
 * If [CategoryRepository.isCategoryInUse] itself returns an error (e.g. a
 * database failure), deletion is blocked and the error is returned to the caller.
 * We never silently proceed when the safety check is uncertain.
 */
class DeleteCategoryUseCase(
    private val categoryRepository: CategoryRepository
) : UseCase<DeleteCategoryUseCase.Params, Unit>() {

    data class Params(
        val categoryId: String,
        /** If `true`, reassign referencing expenses to "other" before deleting. */
        val forceDelete: Boolean = false
    )

    override suspend fun execute(params: Params): Result<Unit> {
        if (!params.forceDelete) {
            when (val inUseResult = categoryRepository.isCategoryInUse(params.categoryId)) {
                is Result.Success -> {
                    if (inUseResult.data) {
                        return Result.Error(
                            IllegalStateException("Category ${params.categoryId} is in use"),
                            "This category has expenses. Delete them first, or choose to reassign them."
                        )
                    }
                }

                is Result.Error -> {
                    // Fail closed — if we cannot determine whether the category
                    // is in use, block deletion rather than risk orphaning expenses.
                    return inUseResult
                }

                else -> Unit
            }
        }

        return categoryRepository.deleteCategory(params.categoryId, params.forceDelete)
    }
}