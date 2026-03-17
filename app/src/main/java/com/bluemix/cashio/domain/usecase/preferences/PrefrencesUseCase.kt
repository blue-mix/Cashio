package com.bluemix.cashio.domain.usecase.preferences

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.core.common.resultOf
import com.bluemix.cashio.domain.model.Currency
import com.bluemix.cashio.domain.repository.UserPreferencesRepository
import com.bluemix.cashio.domain.usecase.base.NoParamsUseCase
import com.bluemix.cashio.domain.usecase.base.UseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// ── Onboarding ─────────────────────────────────────────────────────────────

class ObserveOnboardingCompletedUseCase(
    private val prefsRepository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<Boolean> = prefsRepository.isOnboardingCompleted
}

class SetOnboardingCompletedUseCase(
    private val prefsRepository: UserPreferencesRepository
) : UseCase<Boolean, Unit>() {
    override suspend fun execute(params: Boolean): Result<Unit> = resultOf {
        prefsRepository.setOnboardingCompleted(params)
    }
}

// ── Dark mode ──────────────────────────────────────────────────────────────

class ObserveDarkModeUseCase(
    private val prefsRepository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<Boolean> = prefsRepository.darkModeEnabled
}

class SetDarkModeUseCase(
    private val prefsRepository: UserPreferencesRepository
) : UseCase<Boolean, Unit>() {
    override suspend fun execute(params: Boolean): Result<Unit> = resultOf {
        prefsRepository.setDarkModeEnabled(params)
    }
}

// ── Currency ───────────────────────────────────────────────────────────────

/**
 * Reactive stream of the selected [Currency].
 *
 * The underlying preference stores an ISO code string (e.g. "INR").
 * This use case resolves the code to a [Currency] object, falling back to
 * [Currency.INR] for any unrecognised code — consistent with the app default.
 */
class ObserveSelectedCurrencyUseCase(
    private val prefsRepository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<Currency> =
        prefsRepository.selectedCurrencyCode.map { code ->
            Currency.fromCode(code) ?: Currency.INR
        }
}

/**
 * Persists the selected currency by its ISO code.
 * Validates that the code is known before writing.
 */
class SetSelectedCurrencyUseCase(
    private val prefsRepository: UserPreferencesRepository
) : UseCase<String, Unit>() {
    override suspend fun execute(params: String): Result<Unit> {
        if (Currency.fromCode(params) == null) {
            return Result.Error(
                IllegalArgumentException("Unknown currency code: $params"),
                "Unsupported currency"
            )
        }
        return resultOf { prefsRepository.setSelectedCurrencyCode(params.uppercase()) }
    }
}

/** One-shot snapshot of the currently selected currency. */
class GetSelectedCurrencyUseCase(
    private val prefsRepository: UserPreferencesRepository
) : NoParamsUseCase<Currency>() {
    override suspend fun execute(): Result<Currency> = resultOf {
        val code = prefsRepository.selectedCurrencyCode.first()
        Currency.fromCode(code) ?: Currency.INR
    }
}