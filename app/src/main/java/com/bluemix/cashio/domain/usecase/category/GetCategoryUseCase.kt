package com.bluemix.cashio.domain.usecase.category

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.usecase.base.NoParamsUseCase

/**
 * Get all categories
 */
class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) : NoParamsUseCase<List<Category>>() {

    override suspend fun execute(): Result<List<Category>> {
        return categoryRepository.getAllCategories()
    }
}
