package com.bluemix.cashio.domain.usecase.base

import com.bluemix.cashio.core.common.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Base class for use cases that return a Result
 */
abstract class UseCase<in Params, out Type>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(params: Params): Result<Type> {
        return withContext(dispatcher) {
            execute(params)
        }
    }

    protected abstract suspend fun execute(params: Params): Result<Type>
}

/**
 * Use case with no parameters
 */
abstract class NoParamsUseCase<out Type>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<Type> {
        return withContext(dispatcher) {
            execute()
        }
    }

    protected abstract suspend fun execute(): Result<Type>
}
