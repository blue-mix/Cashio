//package com.bluemix.cashio.data.notification
//
//import android.app.Notification
//import android.service.notification.NotificationListenerService
//import android.service.notification.StatusBarNotification
//import com.bluemix.cashio.domain.repository.CategoryRepository
//import com.bluemix.cashio.domain.repository.ExpenseRepository
//import com.bluemix.cashio.domain.repository.KeywordMappingRepository
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.launch
//import org.koin.android.ext.android.inject
//import java.time.Instant
//import java.time.LocalDateTime
//import java.time.ZoneId
//
///**
// * NotificationListenerService to capture UPI transaction notifications
// */
//class CashioNotificationListener : NotificationListenerService() {
//
//    private val expenseRepository: ExpenseRepository by inject()
//    private val categoryRepository: CategoryRepository by inject()
//    private val keywordMappingRepository: KeywordMappingRepository by inject()
//
//    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
//
//    override fun onNotificationPosted(sbn: StatusBarNotification) {
//        super.onNotificationPosted(sbn)
//
//        val packageName = sbn.packageName
//
//        // Only process UPI app notifications
//        if (!NotificationParser.isUpiApp(packageName)) {
//            return
//        }
//
//        val notification = sbn.notification ?: return
//        val extras = notification.extras ?: return
//
//        // Extract notification title and text
//        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
//        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
//
//        // Check if it's a transaction notification
//        if (!NotificationParser.isTransactionNotification(title, text)) {
//            return
//        }
//
//        // Parse notification
//        val timestamp = LocalDateTime.ofInstant(
//            Instant.ofEpochMilli(sbn.postTime),
//            ZoneId.systemDefault()
//        )
//
//        val parsedTransaction = NotificationParser.parseNotification(
//            title = title,
//            text = text,
//            packageName = packageName,
//            timestamp = timestamp
//        )
//
//        if (parsedTransaction != null) {
//            processTransaction(parsedTransaction)
//        }
//    }
//
//    private fun processTransaction(transaction: com.bluemix.cashio.domain.model.ParsedSmsTransaction) {
//        serviceScope.launch {
//            try {
//                // Find appropriate category using keyword mapping
//                val categoryIdResult = keywordMappingRepository.findCategoryForMerchant(
//                    transaction.merchantName ?: ""
//                )
//
//                val categoryId = when (categoryIdResult) {
//                    is com.bluemix.cashio.core.common.Result.Success -> categoryIdResult.data
//                    else -> null
//                }
//
//                // Get category or default to "Other"
//                val categoryResult = if (categoryId != null) {
//                    categoryRepository.getCategoryById(categoryId)
//                } else {
//                    categoryRepository.getCategoryById("other")
//                }
//
//                val category = when (categoryResult) {
//                    is com.bluemix.cashio.core.common.Result.Success -> categoryResult.data
//                    else -> null
//                }
//
//                if (category != null) {
//                    val expense = transaction.toExpense(category)
//                    expenseRepository.addExpense(expense)
//
//                    println("✅ Added expense from notification: ${expense.title} - ₹${expense.amount}")
//                }
//            } catch (e: Exception) {
//                println("❌ Error processing notification transaction: ${e.message}")
//            }
//        }
//    }
//
//    override fun onNotificationRemoved(sbn: StatusBarNotification) {
//        super.onNotificationRemoved(sbn)
//        // Optional: Handle notification removal if needed
//    }
//}


package com.bluemix.cashio.data.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.domain.model.ParsedSmsTransaction
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

/**
 * NotificationListenerService to capture UPI transaction notifications and save them as Expenses.
 *
 * Notes:
 * - Notification access must be enabled manually in system settings.
 * - UPI apps often "update" notifications -> duplicates are common.
 * - This service uses short-lived in-memory dedupe. For production, persist a unique sourceId.
 */
class CashioNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "CashioNotifListener"
        private const val DEDUPE_TTL_MS = 2 * 60 * 1000L // 2 minutes
    }

    // Safer than `by inject()` inside NotificationListenerService.
    private val expenseRepository by lazy { getKoin().get<ExpenseRepository>() }
    private val categoryRepository by lazy { getKoin().get<CategoryRepository>() }
    private val keywordMappingRepository by lazy { getKoin().get<KeywordMappingRepository>() }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // key/contentHash -> lastProcessedTime
    private val dedupeMap = ConcurrentHashMap<String, Long>()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            // Only allow known UPI apps
            val packageName = sbn.packageName
            if (!NotificationParser.isUpiApp(packageName)) return

            val notification = sbn.notification ?: return
            val extras = notification.extras ?: return

            // Avoid group summaries (super common duplicates / noise)
            if (sbn.isGroup && (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return

            // Avoid ongoing/foreground notifications unless you explicitly want them
            if ((notification.flags and Notification.FLAG_ONGOING_EVENT) != 0) return

            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
            val text = extractBestText(extras)

            if (title.isBlank() && text.isBlank()) return

            // Quick gate for "transaction-like" notifications
            if (!NotificationParser.isTransactionNotification(title, text)) return

            // Dedupe by sbn.key + content hash (apps may repost same content with different keys)
            val contentKey = buildDedupeKey(sbn, title, text)
            if (isDuplicate(contentKey)) return

            val timestamp = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(sbn.postTime),
                ZoneId.systemDefault()
            )

            val parsed = NotificationParser.parseNotification(
                title = title,
                text = text,
                packageName = packageName,
                timestamp = timestamp
            ) ?: run {
                Log.d(TAG, "Ignored: parse failed. pkg=$packageName title=$title text=$text")
                return
            }

            processTransaction(parsed)
        } catch (t: Throwable) {
            Log.e(TAG, "onNotificationPosted crash", t)
        }
    }

    private fun processTransaction(tx: ParsedSmsTransaction) {
        serviceScope.launch {
            try {
                val merchant = tx.merchantName.orEmpty()

                val categoryId =
                    when (val r = keywordMappingRepository.findCategoryForMerchant(merchant)) {
                        is Result.Success -> r.data
                        else -> null
                    }

                val category =
                    when (val r = categoryRepository.getCategoryById(categoryId ?: "other")) {
                        is Result.Success -> r.data
                        else -> null
                    }

                if (category == null) {
                    Log.w(TAG, "Category not found. merchant=$merchant categoryId=$categoryId")
                    return@launch
                }

                val expense = tx.toExpense(category)

                // IMPORTANT NEXT STEP:
                // Persist a unique sourceId (notification hash / sbn.key) in your Expense model to block duplicates across restarts.
                expenseRepository.addExpense(expense)

                Log.d(
                    TAG,
                    "Added from notification: ${expense.title} ₹${expense.amount} (${tx.bankName})"
                )
            } catch (t: Throwable) {
                Log.e(TAG, " Error processing transaction", t)
            }
        }
    }

    /**
     * Pulls best text from various notification fields.
     * Different UPI apps prefer EXTRA_BIG_TEXT or TEXT_LINES.
     */
    private fun extractBestText(extras: android.os.Bundle): String {
        extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.takeIf { it.isNotBlank() }
            ?.let { return it }
        extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.takeIf { it.isNotBlank() }
            ?.let { return it }

        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.joinToString(" ") { it.toString() }
            ?.takeIf { it.isNotBlank() }
        if (lines != null) return lines

        extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.takeIf { it.isNotBlank() }
            ?.let { return it }

        return ""
    }

    /**
     * Dedupe uses:
     * - sbn.key (stable for updates in many cases)
     * - content hash (covers reposts with new keys)
     */
    private fun buildDedupeKey(sbn: StatusBarNotification, title: String, text: String): String {
        val base = "${sbn.packageName}|$title|$text|${sbn.postTime / 1000}" // second-level bucket
        val hash = sha256(base).take(16)
        return "${sbn.key}|$hash"
    }

    private fun isDuplicate(key: String): Boolean {
        val now = System.currentTimeMillis()

        // light cleanup
        if (dedupeMap.size > 400) {
            dedupeMap.entries.removeIf { now - it.value > DEDUPE_TTL_MS }
        }

        val last = dedupeMap.putIfAbsent(key, now) ?: return false
        return if (now - last > DEDUPE_TTL_MS) {
            dedupeMap[key] = now
            false
        } else {
            true
        }
    }

    private fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
