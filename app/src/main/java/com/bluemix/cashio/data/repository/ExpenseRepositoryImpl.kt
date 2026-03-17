package com.bluemix.cashio.data.repository

import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.core.common.resultOf
import com.bluemix.cashio.data.local.database.RealmManager
import com.bluemix.cashio.data.local.entity.CategoryEntity
import com.bluemix.cashio.data.local.entity.ExpenseEntity
import com.bluemix.cashio.data.local.entity.ExpenseSourceValues
import com.bluemix.cashio.data.local.mapper.toDomain
import com.bluemix.cashio.data.local.mapper.toEntity
import com.bluemix.cashio.data.sms.SmsReader
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.DateRange
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.FinancialStats
import com.bluemix.cashio.domain.model.KeywordMapping
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.max

class ExpenseRepositoryImpl(
    private val realmManager: RealmManager,
    private val categoryRepository: CategoryRepository,
    private val keywordMappingRepository: KeywordMappingRepository,
    private val smsReader: SmsReader
) : ExpenseRepository {

    private val realm get() = realmManager.realm

    // Cache zone once per instance — avoids repeated system calls in hot paths.
    private val zone: ZoneId = ZoneId.systemDefault()

    // ── Reactive helpers ───────────────────────────────────────────────────

    /**
     * Reactive category map shared across all flow observations in this repository.
     * Emits a new map whenever any category changes. Using [kotlinx.coroutines.flow.shareIn]
     * at the ViewModel level (via [ObserveExpensesUseCase]) prevents duplicate Realm
     * subscriptions when multiple screens are active.
     */
    private fun observeCategoryMap(): Flow<Map<String, Category>> =
        realm.query<CategoryEntity>()
            .sort("sortOrder", Sort.ASCENDING)
            .asFlow()
            .map { changes -> changes.list.associate { it.id to it.toDomain() } }

    private fun List<ExpenseEntity>.mapToDomain(categoryMap: Map<String, Category>): List<Expense> =
        map { entity ->
            val category = categoryMap[entity.categoryId] ?: Category.default()
            entity.toDomain(category, zone)
        }

    private suspend fun getCategoryMapSnapshot(): Map<String, Category> {
        return when (val result = categoryRepository.getAllCategories()) {
            is Result.Success -> result.data.associateBy { it.id }
            is Result.Error -> throw result.exception   // Propagate — don't silently return empty
            else -> emptyMap()
        }
    }

    // ── Flow observations ──────────────────────────────────────────────────

    override fun observeExpenses(): Flow<List<Expense>> {
        val expensesFlow = realm.query<ExpenseEntity>()
            .sort("dateMillis", Sort.DESCENDING)
            .asFlow()
            .map { it.list.toList() }

        return combine(expensesFlow, observeCategoryMap()) { entities, categories ->
            entities.mapToDomain(categories)
        }
    }

    override fun observeExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Expense>> {
        val startMillis = startDate.toEpochMillis()
        val endMillis = endDate.toEpochMillis()

        val expensesFlow = realm.query<ExpenseEntity>(
            "dateMillis >= $0 AND dateMillis <= $1", startMillis, endMillis
        )
            .sort("dateMillis", Sort.DESCENDING)
            .asFlow()
            .map { it.list.toList() }

        return combine(expensesFlow, observeCategoryMap()) { entities, categories ->
            entities.mapToDomain(categories)
        }
    }

    // ── One-shot queries ───────────────────────────────────────────────────

    override suspend fun getAllExpenses(): Result<List<Expense>> = resultOf {
        val categories = getCategoryMapSnapshot()
        realm.query<ExpenseEntity>()
            .sort("dateMillis", Sort.DESCENDING)
            .find()
            .mapToDomain(categories)
    }

    override suspend fun getExpenseById(id: String): Result<Expense?> = resultOf {
        val entity = realm.query<ExpenseEntity>("id == $0", id).first().find()
            ?: return@resultOf null
        val categoryResult = categoryRepository.getCategoryById(entity.categoryId)
        val category = (categoryResult as? Result.Success)?.data ?: Category.default()
        entity.toDomain(category, zone)
    }

    override suspend fun getExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<List<Expense>> = resultOf {
        val startMillis = startDate.toEpochMillis()
        val endMillis = endDate.toEpochMillis()
        val categories = getCategoryMapSnapshot()

        realm.query<ExpenseEntity>(
            "dateMillis >= $0 AND dateMillis <= $1", startMillis, endMillis
        )
            .sort("dateMillis", Sort.DESCENDING)
            .find()
            .mapToDomain(categories)
    }

    override suspend fun getExpensesByCategory(categoryId: String): Result<List<Expense>> =
        resultOf {
            val categories = getCategoryMapSnapshot()
            realm.query<ExpenseEntity>("categoryId == $0", categoryId)
                .sort("dateMillis", Sort.DESCENDING)
                .find()
                .mapToDomain(categories)
        }

    override suspend fun getExpensesByType(
        transactionType: TransactionType,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<List<Expense>> = resultOf {
        val startMillis = startDate.toEpochMillis()
        val endMillis = endDate.toEpochMillis()
        val categories = getCategoryMapSnapshot()

        realm.query<ExpenseEntity>(
            "dateMillis >= $0 AND dateMillis <= $1 AND transactionType == $2",
            startMillis, endMillis, transactionType.name
        )
            .sort("dateMillis", Sort.DESCENDING)
            .find()
            .mapToDomain(categories)
    }

    // ── Mutations ──────────────────────────────────────────────────────────

    override suspend fun addExpense(expense: Expense): Result<Unit> = resultOf {
        realm.write {
            val exists = query<ExpenseEntity>("id == $0", expense.id).first().find() != null
            if (!exists) copyToRealm(expense.toEntity(zone))
        }
    }

    override suspend fun updateExpense(expense: Expense): Result<Unit> = resultOf {
        realm.write {
            val existing = query<ExpenseEntity>("id == $0", expense.id).first().find()
                ?: return@write  // Nothing to update — silently succeed
            existing.apply {
                amountPaise = expense.amountPaise
                title = expense.title
                categoryId = expense.category.id
                // Set dateMillis directly — the computed `date` setter would do a
                // redundant second ZoneId lookup and overwrite the same value.
                dateMillis = expense.date.atZone(zone).toInstant().toEpochMilli()
                note = expense.note
                merchantName = expense.merchantName
                transactionType = expense.transactionType.name
            }
        }
    }

    override suspend fun deleteExpense(expenseId: String): Result<Unit> = resultOf {
        realm.write {
            val entity = query<ExpenseEntity>("id == $0", expenseId).first().find()
            if (entity != null) delete(entity)
        }
    }

    override suspend fun deleteExpenses(expenseIds: List<String>): Result<Unit> = resultOf {
        if (expenseIds.isEmpty()) return@resultOf
        // Batch fetch with IN operator — single query instead of N queries.
        realm.write {
            val entities = query<ExpenseEntity>("id IN $0", expenseIds).find()
            delete(entities)
        }
    }

    // ── Complex logic ──────────────────────────────────────────────────────

    override suspend fun getFinancialStats(dateRange: DateRange): Result<FinancialStats> =
        resultOf {
            val (startDate, endDate) = dateRange.getDateBounds()
            val startMillis = startDate.toEpochMillis()
            val endMillis = endDate.toEpochMillis()
            val categories = getCategoryMapSnapshot()

            // Query directly — do NOT delegate to getExpensesByDateRange() to avoid
            // nested resultOf wrapping and double dispatcher switches.
            val expenses = realm.query<ExpenseEntity>(
                "dateMillis >= $0 AND dateMillis <= $1", startMillis, endMillis
            )
                .find()
                .mapToDomain(categories)

            if (expenses.isEmpty()) return@resultOf FinancialStats()

            val expenseItems = expenses.filter { it.transactionType == TransactionType.EXPENSE }
            val incomeItems = expenses.filter { it.transactionType == TransactionType.INCOME }

            val totalExpensesPaise = expenseItems.sumOf { it.amountPaise }
            val totalIncomePaise = incomeItems.sumOf { it.amountPaise }

            // Days in range — coerce to at least 1 to prevent division by zero.
            val days = max(1L, ChronoUnit.DAYS.between(startDate, endDate) + 1)

            val categoryBreakdown = expenseItems
                .groupBy { it.category }
                .mapValues { (_, list) -> list.sumOf { it.amountPaise } }

            val topCategoryEntry = categoryBreakdown.maxByOrNull { it.value }

            FinancialStats(
                totalExpensesPaise = totalExpensesPaise,
                totalIncomePaise = totalIncomePaise,
                expenseCount = expenseItems.size,
                incomeCount = incomeItems.size,
                averagePerDayPaise = totalExpensesPaise / days,
                topCategory = topCategoryEntry?.key,
                topCategoryAmountPaise = topCategoryEntry?.value ?: 0L,
                categoryBreakdown = categoryBreakdown
            )
        }

    override suspend fun getFinancialStatsByDates(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<FinancialStats> = resultOf {
        val startMillis = startDate.toEpochMillis()
        val endMillis = endDate.toEpochMillis()
        val categories = getCategoryMapSnapshot()

        val expenses = realm.query<ExpenseEntity>(
            "dateMillis >= $0 AND dateMillis <= $1", startMillis, endMillis
        )
            .find()
            .mapToDomain(categories)

        if (expenses.isEmpty()) return@resultOf FinancialStats()

        val expenseItems = expenses.filter { it.transactionType == TransactionType.EXPENSE }
        val incomeItems = expenses.filter { it.transactionType == TransactionType.INCOME }

        val totalExpensesPaise = expenseItems.sumOf { it.amountPaise }
        val totalIncomePaise = incomeItems.sumOf { it.amountPaise }

        // Days in range — coerce to at least 1 to prevent division by zero.
        val days = max(1L, ChronoUnit.DAYS.between(startDate, endDate) + 1)

        val categoryBreakdown = expenseItems
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amountPaise } }

        val topCategoryEntry = categoryBreakdown.maxByOrNull { it.value }

        FinancialStats(
            totalExpensesPaise = totalExpensesPaise,
            totalIncomePaise = totalIncomePaise,
            expenseCount = expenseItems.size,
            incomeCount = incomeItems.size,
            averagePerDayPaise = totalExpensesPaise / days,
            topCategory = topCategoryEntry?.key,
            topCategoryAmountPaise = topCategoryEntry?.value ?: 0L,
            categoryBreakdown = categoryBreakdown
        )
    }

    override suspend fun refreshExpensesFromSms(): Result<Int> = resultOf {
        val parsed = smsReader.syncTransactions()
        if (parsed.isEmpty()) return@resultOf 0

        val categories = getCategoryMapSnapshot()

        // Load and sort keyword mappings once — not per merchant lookup.
        val keywordMappings: List<KeywordMapping> =
            (keywordMappingRepository.getAllKeywordMappings() as? Result.Success)
                ?.data
                ?.sortedByDescending { it.priority }
                .orEmpty()

        fun findCategory(merchantName: String): Category {
            if (merchantName.isBlank()) return Category.default()
            val lower = merchantName.lowercase()
            val match = keywordMappings.firstOrNull { it.keyword.lowercase() in lower }
            return match?.let { categories[it.categoryId] } ?: Category.default()
        }

        // Batch dedup: fetch all existing IDs in one query.
        val incomingIds = parsed.map { it.smsId }
        val existingIds = realm.query<ExpenseEntity>("id IN $0", incomingIds)
            .find()
            .mapTo(HashSet()) { it.id }

        var newCount = 0
        realm.write {
            for (tx in parsed) {
                if (tx.smsId in existingIds) continue
                val category = findCategory(tx.merchantName.orEmpty())
                copyToRealm(tx.toExpense(category).toEntity(zone))
                existingIds.add(tx.smsId)   // Guard against duplicates within this batch
                newCount++
            }
        }

        newCount
    }

    override suspend fun recategorizeExpensesByKeyword(keyword: String): Result<Int> = resultOf {
        val kw = keyword.trim()
        if (kw.isBlank()) return@resultOf 0

        // Sort once outside the write block.
        val mappings = (keywordMappingRepository.getAllKeywordMappings() as? Result.Success)
            ?.data
            ?.sortedByDescending { it.priority }
            .orEmpty()

        fun resolveCategoryId(text: String): String {
            val lower = text.lowercase()
            return mappings.firstOrNull { it.keyword.lowercase() in lower }?.categoryId ?: "other"
        }

        var updatedCount = 0
        realm.write {
            // Exclude manually-entered expenses from auto-recategorization.
            val entities = query<ExpenseEntity>(
                "source != $0 AND (merchantName CONTAINS[c] $1 OR title CONTAINS[c] $1)",
                ExpenseSourceValues.MANUAL, kw
            ).find()

            for (entity in entities) {
                val haystack =
                    "${entity.merchantName.orEmpty()} ${entity.title} ${entity.rawSmsBody.orEmpty()}"
                val newCategory = resolveCategoryId(haystack)
                if (entity.categoryId != newCategory) {
                    entity.categoryId = newCategory
                    updatedCount++
                }
            }
        }
        updatedCount
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private fun LocalDateTime.toEpochMillis(): Long =
        atZone(zone).toInstant().toEpochMilli()
}