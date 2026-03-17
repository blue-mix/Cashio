package com.bluemix.cashio.domain.usecase.base

import com.bluemix.cashio.core.common.Result

/**
 * Base class for use cases that accept typed [Params] and return [Result].
 *
 * ## Threading
 * No dispatcher is injected here. Repository implementations manage their own
 * threading (Realm's write dispatcher, [kotlinx.coroutines.Dispatchers.IO] for
 * DataStore, etc.). Adding another [kotlinx.coroutines.withContext] hop in the
 * use case layer is redundant overhead and obscures the actual execution context.
 *
 * ViewModels launch use cases inside [androidx.lifecycle.viewModelScope] which
 * uses [kotlinx.coroutines.Dispatchers.Main.immediate] — repository calls
 * inside use cases switch to the appropriate background dispatcher themselves.
 *
 * ## Responsibilities
 * Use cases should encapsulate **business logic** — validation, orchestration
 * across multiple repositories, or domain-level invariants. Pure delegation to
 * a single repository method without any logic is a sign the use case layer
 * is unnecessary for that operation.
 */
abstract class UseCase<in Params, out Type> {

    suspend operator fun invoke(params: Params): Result<Type> = execute(params)

    protected abstract suspend fun execute(params: Params): Result<Type>
}

/**
 * Use case with no input parameters.
 */
abstract class NoParamsUseCase<out Type> {

    suspend operator fun invoke(): Result<Type> = execute()

    protected abstract suspend fun execute(): Result<Type>
}