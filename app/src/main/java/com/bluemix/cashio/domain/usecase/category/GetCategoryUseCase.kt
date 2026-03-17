package com.bluemix.cashio.domain.usecase.category

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.usecase.base.NoParamsUseCase
import kotlinx.coroutines.flow.Flow

/**
 * One-shot fetch of all categories ordered by display sort order.
 */
class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) : NoParamsUseCase<List<Category>>() {

    override suspend fun execute(): Result<List<Category>> =
        categoryRepository.getAllCategories()
}

/**
 * Reactive stream of all categories.
 * Use this on screens that must react to category additions or edits in real time.
 */
class ObserveCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<List<Category>> = categoryRepository.observeCategories()
}