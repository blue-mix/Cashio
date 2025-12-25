package com.bluemix.cashio.domain.usecase.category

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.usecase.base.UseCase

/**
 * Add a new category
 */
class AddCategoryUseCase(
    private val categoryRepository: CategoryRepository
) : UseCase<Category, Unit>() {

    override suspend fun execute(params: Category): Result<Unit> {
        return categoryRepository.addCategory(params)
    }
}
