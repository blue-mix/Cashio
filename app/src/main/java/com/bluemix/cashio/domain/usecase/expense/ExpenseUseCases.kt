package com.bluemix.cashio.domain.usecase.expense

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.usecase.base.NoParamsUseCase
import com.bluemix.cashio.domain.usecase.base.UseCase
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

// ── Observe (reactive) ─────────────────────────────────────────────────────

/**
 * Reactive stream of all expenses, newest first.
 * Use on screens that must reflect SMS/notification imports in real time.
 */
class ObserveExpensesUseCase(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<Expense>> = expenseRepository.observeExpenses()
}

/**
 * Reactive stream of expenses within [startDate]..[endDate].
 */
class ObserveExpensesByDateRangeUseCase(
    private val expenseRepository: ExpenseRepository
) {
    data class Params(val startDate: LocalDateTime, val endDate: LocalDateTime)

    operator fun invoke(params: Params): Flow<List<Expense>> =
        expenseRepository.observeExpensesByDateRange(params.startDate, params.endDate)
}

// ── One-shot queries ───────────────────────────────────────────────────────

/** All expenses as a one-shot snapshot. */
class GetAllExpensesUseCase(
    private val expenseRepository: ExpenseRepository
) : NoParamsUseCase<List<Expense>>() {
    override suspend fun execute(): Result<List<Expense>> =
        expenseRepository.getAllExpenses()
}

/** Single expense by id. */
class GetExpenseByIdUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<String, Expense?>() {
    override suspend fun execute(params: String): Result<Expense?> =
        expenseRepository.getExpenseById(params)
}

/** Expenses within an explicit date window. */
class GetExpensesByDateRangeUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<GetExpensesByDateRangeUseCase.Params, List<Expense>>() {
    data class Params(val startDate: LocalDateTime, val endDate: LocalDateTime)

    override suspend fun execute(params: Params): Result<List<Expense>> =
        expenseRepository.getExpensesByDateRange(params.startDate, params.endDate)
}

/** Expenses filtered by category. */
class GetExpensesByCategoryUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<String, List<Expense>>() {
    override suspend fun execute(params: String): Result<List<Expense>> =
        expenseRepository.getExpensesByCategory(params)
}

/** Expenses filtered by transaction type within a date window. */
class GetExpensesByTypeUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<GetExpensesByTypeUseCase.Params, List<Expense>>() {
    data class Params(
        val transactionType: TransactionType,
        val startDate: LocalDateTime,
        val endDate: LocalDateTime
    )

    override suspend fun execute(params: Params): Result<List<Expense>> =
        expenseRepository.getExpensesByType(
            params.transactionType,
            params.startDate,
            params.endDate
        )
}

// ── Mutations ──────────────────────────────────────────────────────────────

/**
 * Validates and persists a new expense.
 *
 * Validation rules:
 * - [Expense.amountPaise] must be positive.
 * - [Expense.title] must not be blank.
 * - [Expense.id] must not be blank.
 */
class AddExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<Expense, Unit>() {

    override suspend fun execute(params: Expense): Result<Unit> {
        if (params.id.isBlank()) {
            return Result.Error(
                IllegalArgumentException("Expense id cannot be blank"),
                "Missing ID"
            )
        }
        if (params.amountPaise <= 0L) {
            return Result.Error(
                IllegalArgumentException("Amount must be positive"),
                "Amount must be greater than zero"
            )
        }
        if (params.title.isBlank()) {
            return Result.Error(
                IllegalArgumentException("Expense title cannot be blank"),
                "Title is required"
            )
        }
        return expenseRepository.addExpense(params.copy(title = params.title.trim()))
    }
}

/**
 * Validates and updates an existing expense.
 * Same validation rules as [AddExpenseUseCase].
 */
class UpdateExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<Expense, Unit>() {

    override suspend fun execute(params: Expense): Result<Unit> {
        if (params.id.isBlank()) {
            return Result.Error(
                IllegalArgumentException("Expense id cannot be blank"),
                "Missing ID"
            )
        }
        if (params.amountPaise <= 0L) {
            return Result.Error(
                IllegalArgumentException("Amount must be positive"),
                "Amount must be greater than zero"
            )
        }
        if (params.title.isBlank()) {
            return Result.Error(
                IllegalArgumentException("Expense title cannot be blank"),
                "Title is required"
            )
        }
        return expenseRepository.updateExpense(params.copy(title = params.title.trim()))
    }
}

/** Deletes a single expense by id. */
class DeleteExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<String, Unit>() {
    override suspend fun execute(params: String): Result<Unit> =
        expenseRepository.deleteExpense(params)
}

/** Deletes multiple expenses in one transaction. */
class DeleteExpensesUseCase(
    private val expenseRepository: ExpenseRepository
) : UseCase<List<String>, Unit>() {
    override suspend fun execute(params: List<String>): Result<Unit> =
        expenseRepository.deleteExpenses(params)
}