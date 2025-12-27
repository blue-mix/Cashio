//package com.bluemix.cashio.data.repository
//
//import android.content.Context
//import android.util.Log
//import com.bluemix.cashio.core.common.Result
//import com.bluemix.cashio.core.common.resultOf
//import com.bluemix.cashio.data.local.database.RealmManager
//import com.bluemix.cashio.data.local.entity.ExpenseEntity
//import com.bluemix.cashio.data.local.mapper.toDomain
//import com.bluemix.cashio.data.local.mapper.toEntity
//import com.bluemix.cashio.data.sms.SmsReader
//import com.bluemix.cashio.domain.model.Category
//import com.bluemix.cashio.domain.model.DateRange
//import com.bluemix.cashio.domain.model.Expense
//import com.bluemix.cashio.domain.model.ExpenseSource
//import com.bluemix.cashio.domain.model.FinancialStats
//import com.bluemix.cashio.domain.model.TransactionType
//import com.bluemix.cashio.domain.repository.CategoryRepository
//import com.bluemix.cashio.domain.repository.ExpenseRepository
//import com.bluemix.cashio.domain.repository.KeywordMappingRepository
//import io.realm.kotlin.ext.query
//import io.realm.kotlin.query.Sort
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.mapLatest
//import kotlinx.coroutines.withContext
//import java.time.LocalDateTime
//import java.time.ZoneId
//
///**
// * Implementation of ExpenseRepository using Realm
// */
//class ExpenseRepositoryImpl(
//    private val realmManager: RealmManager,
//    private val categoryRepository: CategoryRepository,
//    private val keywordMappingRepository: KeywordMappingRepository,
//    private val context: Context
//) : ExpenseRepository {
//
//    private val realm get() = realmManager.realm
//    private val smsReader = SmsReader(context)
//
//    private companion object {
//        const val TAG = "ExpenseRepo"
//    }
//
//    override fun observeExpenses(): Flow<List<Expense>> {
//        return realm.query<ExpenseEntity>()
//            .sort("dateMillis", Sort.DESCENDING)
//            .asFlow()
//            .mapLatest { resultsChange ->
//                resultsChange.list.mapNotNull { entity ->
//                    entityToDomain(entity)
//                }
//            }
//    }
//
//    override fun observeExpensesByDateRange(
//        startDate: LocalDateTime,
//        endDate: LocalDateTime
//    ): Flow<List<Expense>> {
//        val startMillis = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
//        val endMillis = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
//
//        return realm.query<ExpenseEntity>(
//            "dateMillis >= $0 AND dateMillis <= $1",
//            startMillis, endMillis
//        )
//            .sort("dateMillis", Sort.DESCENDING)
//            .asFlow()
//            .mapLatest { resultsChange ->
//                resultsChange.list.mapNotNull { entity ->
//                    entityToDomain(entity)
//                }
//            }
//    }
//
//
//    override suspend fun getAllExpenses(): Result<List<Expense>> = withContext(Dispatchers.IO) {
//        resultOf {
//            val entities = realm.query<ExpenseEntity>()
//                .sort("dateMillis", Sort.DESCENDING)
//                .find()
//
//            entities.mapNotNull { entityToDomain(it) }
//        }
//    }
//
//    override suspend fun getExpenseById(id: String): Result<Expense?> =
//        withContext(Dispatchers.IO) {
//            resultOf {
//                val entity = realm.query<ExpenseEntity>("id == $0", id).first().find()
//                entity?.let { entityToDomain(it) }
//            }
//        }
//
//    override suspend fun getExpensesByDateRange(
//        startDate: LocalDateTime,
//        endDate: LocalDateTime
//    ): Result<List<Expense>> = withContext(Dispatchers.IO) {
//        resultOf {
//            val startMillis =
//                startDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
//            val endMillis =
//                endDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
//
//            val entities = realm.query<ExpenseEntity>(
//                "dateMillis >= $0 AND dateMillis <= $1",
//                startMillis,
//                endMillis
//            )
//                .sort("dateMillis", Sort.DESCENDING)
//                .find()
//
//            entities.mapNotNull { entityToDomain(it) }
//        }
//    }
//
//    override suspend fun getExpensesByCategory(categoryId: String): Result<List<Expense>> =
//        withContext(Dispatchers.IO) {
//            resultOf {
//                val entities = realm.query<ExpenseEntity>("categoryId == $0", categoryId)
//                    .sort("dateMillis", Sort.DESCENDING)
//                    .find()
//
//                entities.mapNotNull { entityToDomain(it) }
//            }
//        }
//
//    override suspend fun addExpense(expense: Expense): Result<Unit> = withContext(Dispatchers.IO) {
//        resultOf {
//            realm.write {
//                val entity = expense.toEntity()
//                copyToRealm(entity)
//            }
//                .let { }
//        }
//    }
//
//    override suspend fun updateExpense(expense: Expense): Result<Unit> =
//        withContext(Dispatchers.IO) {
//            resultOf {
//                realm.write {
//                    val existingEntity = query<ExpenseEntity>("id == $0", expense.id).first().find()
//                    if (existingEntity != null) {
//                        existingEntity.apply {
//                            amount = expense.amount
//                            title = expense.title
//                            categoryId = expense.category.id
//                            date = expense.date
//                            note = expense.note
//                            merchantName = expense.merchantName
//                        }
//                    }
//                }
//            }
//        }
//
//    override suspend fun deleteExpense(expenseId: String): Result<Unit> =
//        withContext(Dispatchers.IO) {
//            resultOf {
//                realm.write {
//                    val entity = query<ExpenseEntity>("id == $0", expenseId).first().find()
//                    if (entity != null) {
//                        delete(entity)
//                    }
//                }
//            }
//        }
//
//    override suspend fun deleteExpenses(expenseIds: List<String>): Result<Unit> =
//        withContext(Dispatchers.IO) {
//            resultOf {
//                realm.write {
//                    expenseIds.forEach { id ->
//                        val entity = query<ExpenseEntity>("id == $0", id).first().find()
//                        if (entity != null) {
//                            delete(entity)
//                        }
//                    }
//                }
//            }
//        }
//
//    override suspend fun getFinancialStats(dateRange: DateRange): Result<FinancialStats> =
//        withContext(Dispatchers.IO) {
//            resultOf {
//                val (startDate, endDate) = dateRange.getDateBounds()
//                val expenses = when (val result = getExpensesByDateRange(startDate, endDate)) {
//                    is Result.Success -> result.data
//                    else -> emptyList()
//                }
//
//                if (expenses.isEmpty()) {
//                    return@resultOf FinancialStats(
//                        totalExpenses = 0.0,
//                        totalIncome = 0.0,
//                        expenseCount = 0,
//                        averagePerDay = 0.0,
//                        topCategory = null,
//                        topCategoryAmount = 0.0,
//                        categoryBreakdown = emptyMap()
//                    )
//                }
//
//                // Separate income and expenses
//                val expensesList = expenses.filter { it.transactionType == TransactionType.EXPENSE }
//                val incomeList = expenses.filter { it.transactionType == TransactionType.INCOME }
//
//                val totalExpenses = expensesList.sumOf { it.amount }
//                val totalIncome = incomeList.sumOf { it.amount }
//                val expenseCount = expensesList.size
//
//                val daysDifference = java.time.Duration.between(startDate, endDate).toDays() + 1
//                val averagePerDay = if (daysDifference > 0) totalExpenses / daysDifference else 0.0
//
//                // Category breakdown (expenses only)
//                val categoryBreakdown = expensesList.groupBy { it.category }
//                    .mapValues { (_, list) -> list.sumOf { it.amount } }
//
//                val topCategoryEntry = categoryBreakdown.maxByOrNull { it.value }
//
//                FinancialStats(
//                    totalExpenses = totalExpenses,
//                    totalIncome = totalIncome,
//                    expenseCount = expenseCount,
//                    averagePerDay = averagePerDay,
//                    topCategory = topCategoryEntry?.key,
//                    topCategoryAmount = topCategoryEntry?.value ?: 0.0,
//                    categoryBreakdown = categoryBreakdown
//                )
//            }
//        }
//
//    override suspend fun refreshExpensesFromSms(): Result<Int> = withContext(Dispatchers.IO) {
//        resultOf {
//            // Use smart sync (full first time, incremental after)
//            val parsedTransactions = smsReader.syncTransactions()
//
//            // Get all categories and keyword mappings BEFORE write transaction
//            val categoriesResult = categoryRepository.getAllCategories()
//            val categories = when (categoriesResult) {
//                is Result.Success -> categoriesResult.data
//                else -> emptyList()
//            }
//
//            val keywordMappingsResult = keywordMappingRepository.getAllKeywordMappings()
//            val keywordMappings = when (keywordMappingsResult) {
//                is Result.Success -> keywordMappingsResult.data
//                else -> emptyList()
//            }
//
//            // Create lookup maps
//            val categoryMap = categories.associateBy { it.id }
//
//            // Category finder function
//            fun findCategoryForMerchant(merchantName: String): Category? {
//                if (merchantName.isBlank()) return categoryMap["other"]
//
//                val lowerMerchant = merchantName.lowercase(java.util.Locale.getDefault())
//
//                val matchingMapping = keywordMappings
//                    .sortedByDescending { it.priority }
//                    .firstOrNull { mapping ->
//                        val keyword = mapping.keyword.lowercase(java.util.Locale.getDefault())
//                        keyword in lowerMerchant
//                    }
//
//                return if (matchingMapping != null) {
//                    categoryMap[matchingMapping.categoryId]
//                } else {
//                    categoryMap["other"]
//                }
//            }
//
//            var newExpensesCount = 0
//
//            // Write to database
//            realm.write {
//                parsedTransactions.forEach { transaction ->
//                    val category = findCategoryForMerchant(transaction.merchantName ?: "")
//
//                    if (category != null) {
//                        val expense = transaction.toExpense(category)
//
//                        // Check if already exists
//                        val exists = query<ExpenseEntity>("id == $0", expense.id)
//                            .first()
//                            .find() != null
//
//                        if (!exists) {
//                            copyToRealm(expense.toEntity())
//                            newExpensesCount++
//                        }
//                    }
//                }
//            }
//
//            newExpensesCount
//        }
//    }
//
//    override suspend fun getExpensesByType(
//        transactionType: TransactionType,
//        startDate: LocalDateTime,
//        endDate: LocalDateTime
//    ): Result<List<Expense>> = withContext(Dispatchers.IO) {
//        resultOf {
//            val startMillis = startDate.atZone(java.time.ZoneId.systemDefault())
//                .toInstant().toEpochMilli()
//            val endMillis = endDate.atZone(java.time.ZoneId.systemDefault())
//                .toInstant().toEpochMilli()
//
//            val entities = realm.query<ExpenseEntity>(
//                "dateMillis >= $0 AND dateMillis <= $1 AND transactionType == $2",
//                startMillis,
//                endMillis,
//                transactionType.name
//            )
//                .sort("dateMillis", Sort.DESCENDING)
//                .find()
//
//            entities.mapNotNull { entityToDomain(it) }
//        }
//    }
//
//    override suspend fun recategorizeExpensesByKeyword(keyword: String): Result<Int> =
//        withContext(Dispatchers.IO) {
//            resultOf {
//                val start = System.currentTimeMillis()
//                val kw = keyword.trim()
//                if (kw.isBlank()) return@resultOf 0
//
//                Log.i(TAG, "🔎 Recategorize START | keyword='$kw'")
//
//                // Load mappings/categories OUTSIDE write (good)
//                val mappings =
//                    (keywordMappingRepository.getAllKeywordMappings() as? Result.Success)?.data.orEmpty()
//
//                fun resolveCategoryId(merchant: String?, title: String?, raw: String?): String {
//                    val haystack = listOfNotNull(merchant, title, raw).joinToString(" ").lowercase()
//                    val match = mappings.sortedByDescending { it.priority }
//                        .firstOrNull { it.keyword.lowercase() in haystack }
//                    return match?.categoryId ?: "other"
//                }
//
//                var updatedCount = 0
//
//                realm.write {
//                    val entities = query<ExpenseEntity>(
//                        "source != $0 AND (" +
//                                "merchantName CONTAINS[c] $1 OR " +
//                                "title CONTAINS[c] $1 OR " +
//                                "rawSmsBody CONTAINS[c] $1" +
//                                ")",
//                        ExpenseSource.MANUAL.name,
//                        kw
//                    ).find()
//
//                    Log.i(TAG, "📌 Candidates found=${entities.size} for keyword='$kw'")
//
//                    entities.forEach { e ->
//                        val newCategoryId = resolveCategoryId(e.merchantName, e.title, e.rawSmsBody)
//                        if (e.categoryId != newCategoryId) {
//                            Log.d(TAG, "✏️ ${e.id}: '${e.categoryId}' -> '$newCategoryId'")
//                            e.categoryId = newCategoryId
//                            updatedCount++
//                        }
//                    }
//                }
//
//                Log.i(
//                    TAG,
//                    "✅ Recategorize DONE | keyword='$kw' updated=$updatedCount in ${System.currentTimeMillis() - start}ms"
//                )
//                updatedCount
//            }
//        }
//
//
//    /**
//     * Helper to convert entity to domain with category lookup
//     */
//    private suspend fun entityToDomain(entity: ExpenseEntity): Expense? {
//        val categoryResult = categoryRepository.getCategoryById(entity.categoryId)
//        return if (categoryResult is Result.Success && categoryResult.data != null) {
//            entity.toDomain(categoryResult.data)
//        } else {
//            null  // Skip expenses with missing categories
//        }
//    }
//}
package com.bluemix.cashio.data.repository

import android.content.Context
import android.util.Log
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.core.common.resultOf
import com.bluemix.cashio.data.local.database.RealmManager
import com.bluemix.cashio.data.local.entity.ExpenseEntity
import com.bluemix.cashio.data.local.mapper.toDomain
import com.bluemix.cashio.data.local.mapper.toEntity
import com.bluemix.cashio.data.sms.SmsReader
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.DateRange
import com.bluemix.cashio.domain.model.Expense
import com.bluemix.cashio.domain.model.ExpenseSource
import com.bluemix.cashio.domain.model.FinancialStats
import com.bluemix.cashio.domain.model.TransactionType
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId

class ExpenseRepositoryImpl(
    private val realmManager: RealmManager,
    private val categoryRepository: CategoryRepository,
    private val keywordMappingRepository: KeywordMappingRepository,
    private val context: Context
) : ExpenseRepository {

    private val realm get() = realmManager.realm
    private val smsReader = SmsReader(context)

    private companion object {
        const val TAG = "ExpenseRepo"
    }

    // ✅ In-memory category cache to avoid N+1 lookups per emission
    @Volatile
    private var categoryCache: Map<String, Category> = emptyMap()

    private suspend fun getCategoryMap(): Map<String, Category> {
        val cached = categoryCache
        if (cached.isNotEmpty()) return cached

        val result = categoryRepository.getAllCategories()
        val map = (result as? Result.Success)?.data?.associateBy { it.id }.orEmpty()

        // Only set cache if we actually got something
        if (map.isNotEmpty()) {
            categoryCache = map
        }
        return map
    }

    private suspend fun entityToDomain(entity: ExpenseEntity): Expense? {
        val map = getCategoryMap()
        val category = map[entity.categoryId] ?: map["other"]
        return category?.let { entity.toDomain(it) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeExpenses(): Flow<List<Expense>> {
        return realm.query<ExpenseEntity>()
            .sort("dateMillis", Sort.DESCENDING)
            .asFlow()
            .mapLatest { resultsChange ->
                resultsChange.list.mapNotNull { entity ->
                    entityToDomain(entity)
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Expense>> {
        val startMillis = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return realm.query<ExpenseEntity>(
            "dateMillis >= $0 AND dateMillis <= $1",
            startMillis, endMillis
        )
            .sort("dateMillis", Sort.DESCENDING)
            .asFlow()
            .mapLatest { resultsChange ->
                resultsChange.list.mapNotNull { entity ->
                    entityToDomain(entity)
                }
            }
    }

    override suspend fun getAllExpenses(): Result<List<Expense>> = withContext(Dispatchers.IO) {
        resultOf {
            val entities = realm.query<ExpenseEntity>()
                .sort("dateMillis", Sort.DESCENDING)
                .find()

            entities.mapNotNull { entityToDomain(it) }
        }
    }

    override suspend fun getExpenseById(id: String): Result<Expense?> =
        withContext(Dispatchers.IO) {
            resultOf {
                val entity = realm.query<ExpenseEntity>("id == $0", id).first().find()
                entity?.let { entityToDomain(it) }
            }
        }

    override suspend fun getExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<List<Expense>> = withContext(Dispatchers.IO) {
        resultOf {
            val startMillis = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val entities = realm.query<ExpenseEntity>(
                "dateMillis >= $0 AND dateMillis <= $1",
                startMillis, endMillis
            )
                .sort("dateMillis", Sort.DESCENDING)
                .find()

            entities.mapNotNull { entityToDomain(it) }
        }
    }

    override suspend fun getExpensesByCategory(categoryId: String): Result<List<Expense>> =
        withContext(Dispatchers.IO) {
            resultOf {
                val entities = realm.query<ExpenseEntity>("categoryId == $0", categoryId)
                    .sort("dateMillis", Sort.DESCENDING)
                    .find()

                entities.mapNotNull { entityToDomain(it) }
            }
        }

    override suspend fun addExpense(expense: Expense): Result<Unit> = withContext(Dispatchers.IO) {
        resultOf {
            realm.write {
                copyToRealm(expense.toEntity())
            }
            Unit
        }
    }

    override suspend fun updateExpense(expense: Expense): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                realm.write {
                    val existing = query<ExpenseEntity>("id == $0", expense.id).first().find()
                    existing?.apply {
                        amount = expense.amount
                        title = expense.title
                        categoryId = expense.category.id
                        date = expense.date
                        note = expense.note
                        merchantName = expense.merchantName
                        transactionType = expense.transactionType.name
                    }
                }
                Unit
            }
        }

    override suspend fun deleteExpense(expenseId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                realm.write {
                    val entity = query<ExpenseEntity>("id == $0", expenseId).first().find()
                    if (entity != null) delete(entity)
                }
            }
        }

    override suspend fun deleteExpenses(expenseIds: List<String>): Result<Unit> =
        withContext(Dispatchers.IO) {
            resultOf {
                realm.write {
                    expenseIds.forEach { id ->
                        val entity = query<ExpenseEntity>("id == $0", id).first().find()
                        if (entity != null) delete(entity)
                    }
                }
            }
        }

    override suspend fun getFinancialStats(dateRange: DateRange): Result<FinancialStats> =
        withContext(Dispatchers.IO) {
            resultOf {
                val (startDate, endDate) = dateRange.getDateBounds()
                val expenses =
                    (getExpensesByDateRange(startDate, endDate) as? Result.Success)?.data.orEmpty()

                if (expenses.isEmpty()) {
                    return@resultOf FinancialStats(
                        totalExpenses = 0.0,
                        totalIncome = 0.0,
                        expenseCount = 0,
                        averagePerDay = 0.0,
                        topCategory = null,
                        topCategoryAmount = 0.0,
                        categoryBreakdown = emptyMap()
                    )
                }

                val expensesList = expenses.filter { it.transactionType == TransactionType.EXPENSE }
                val incomeList = expenses.filter { it.transactionType == TransactionType.INCOME }

                val totalExpenses = expensesList.sumOf { it.amount }
                val totalIncome = incomeList.sumOf { it.amount }
                val expenseCount = expensesList.size

                val daysDifference = java.time.Duration.between(startDate, endDate).toDays() + 1
                val averagePerDay = if (daysDifference > 0) totalExpenses / daysDifference else 0.0

                val categoryBreakdown = expensesList.groupBy { it.category }
                    .mapValues { (_, list) -> list.sumOf { it.amount } }

                val topCategoryEntry = categoryBreakdown.maxByOrNull { it.value }

                FinancialStats(
                    totalExpenses = totalExpenses,
                    totalIncome = totalIncome,
                    expenseCount = expenseCount,
                    averagePerDay = averagePerDay,
                    topCategory = topCategoryEntry?.key,
                    topCategoryAmount = topCategoryEntry?.value ?: 0.0,
                    categoryBreakdown = categoryBreakdown
                )
            }
        }

    override suspend fun refreshExpensesFromSms(): Result<Int> = withContext(Dispatchers.IO) {
        resultOf {
            val parsedTransactions = smsReader.syncTransactions()

            val categories =
                (categoryRepository.getAllCategories() as? Result.Success)?.data.orEmpty()
            val keywordMappings =
                (keywordMappingRepository.getAllKeywordMappings() as? Result.Success)?.data.orEmpty()

            // ✅ Refresh cache after seeding/updates (simple)
            if (categories.isNotEmpty()) {
                categoryCache = categories.associateBy { it.id }
            }

            val categoryMap = categories.associateBy { it.id }

            fun findCategoryForMerchant(merchantName: String): Category? {
                if (merchantName.isBlank()) return categoryMap["other"]
                val lowerMerchant = merchantName.lowercase()

                val match = keywordMappings
                    .sortedByDescending { it.priority }
                    .firstOrNull { it.keyword.lowercase() in lowerMerchant }

                return if (match != null) categoryMap[match.categoryId] else categoryMap["other"]
            }

            var newCount = 0

            realm.write {
                parsedTransactions.forEach { transaction ->
                    val category = findCategoryForMerchant(transaction.merchantName ?: "")
                    if (category != null) {
                        val expense = transaction.toExpense(category)

                        val exists =
                            query<ExpenseEntity>("id == $0", expense.id).first().find() != null
                        if (!exists) {
                            copyToRealm(expense.toEntity())
                            newCount++
                        }
                    }
                }
            }

            newCount
        }
    }

    override suspend fun getExpensesByType(
        transactionType: TransactionType,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<List<Expense>> = withContext(Dispatchers.IO) {
        resultOf {
            val startMillis = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val entities = realm.query<ExpenseEntity>(
                "dateMillis >= $0 AND dateMillis <= $1 AND transactionType == $2",
                startMillis,
                endMillis,
                transactionType.name
            )
                .sort("dateMillis", Sort.DESCENDING)
                .find()

            entities.mapNotNull { entityToDomain(it) }
        }
    }

    override suspend fun recategorizeExpensesByKeyword(keyword: String): Result<Int> =
        withContext(Dispatchers.IO) {
            resultOf {
                val start = System.currentTimeMillis()
                val kw = keyword.trim()
                if (kw.isBlank()) return@resultOf 0

                Log.i(TAG, "🔎 Recategorize START | keyword='$kw'")

                val mappings =
                    (keywordMappingRepository.getAllKeywordMappings() as? Result.Success)?.data.orEmpty()

                fun resolveCategoryId(merchant: String?, title: String?, raw: String?): String {
                    val haystack = listOfNotNull(merchant, title, raw).joinToString(" ").lowercase()
                    val match = mappings.sortedByDescending { it.priority }
                        .firstOrNull { it.keyword.lowercase() in haystack }
                    return match?.categoryId ?: "other"
                }

                var updatedCount = 0

                realm.write {
                    val entities = query<ExpenseEntity>(
                        "source != $0 AND (" +
                                "merchantName CONTAINS[c] $1 OR " +
                                "title CONTAINS[c] $1 OR " +
                                "rawSmsBody CONTAINS[c] $1" +
                                ")",
                        ExpenseSource.MANUAL.name,
                        kw
                    ).find()

                    Log.i(TAG, " Candidates found=${entities.size} for keyword='$kw'")

                    entities.forEach { e ->
                        val newCategoryId = resolveCategoryId(e.merchantName, e.title, e.rawSmsBody)
                        if (e.categoryId != newCategoryId) {
                            Log.d(TAG, "✏️ ${e.id}: '${e.categoryId}' -> '$newCategoryId'")
                            e.categoryId = newCategoryId
                            updatedCount++
                        }
                    }
                }

                // ✅ Optional: cache might now be stale if categories changed elsewhere
                // categoryCache = emptyMap()

                Log.i(
                    TAG,
                    "✅ Recategorize DONE | keyword='$kw' updated=$updatedCount in ${System.currentTimeMillis() - start}ms"
                )
                updatedCount
            }
        }
}
