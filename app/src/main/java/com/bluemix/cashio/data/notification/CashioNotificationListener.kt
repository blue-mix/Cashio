package com.bluemix.cashio.data.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.bluemix.cashio.core.common.Result
import com.bluemix.cashio.data.notification.CashioNotificationListener.Companion.DEDUPE_MAX_SIZE
import com.bluemix.cashio.data.notification.CashioNotificationListener.Companion.DEDUPE_TTL_MS
import com.bluemix.cashio.domain.model.Category
import com.bluemix.cashio.domain.model.ParsedNotificationTransaction
import com.bluemix.cashio.domain.repository.CategoryRepository
import com.bluemix.cashio.domain.repository.ExpenseRepository
import com.bluemix.cashio.domain.repository.KeywordMappingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

/**
 * Captures UPI transaction notifications and persists them as expenses.
 *
 * ## Koin injection
 * Repositories are resolved lazily via [getKoin] inside [onListenerConnected]
 * (not at field initialisation time) to ensure Koin is fully started before
 * we attempt a lookup. [NotificationListenerService] can be bound by the system
 * before [Application.onCreate] completes in rare boot scenarios.
 *
 * ## Scope lifecycle
 * [serviceScope] is created in [onListenerConnected] and cancelled in
 * [onListenerDisconnected] to prevent coroutine leaks.
 *
 * ## Deduplication
 * Short-lived in-memory map keyed by stable [ParsedNotificationTransaction.notifId]
 * fingerprint. Permanent dedup is handled by Realm's primary key on [Expense.id].
 */
class CashioNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "CashioNotifListener"
        private const val DEDUPE_TTL_MS = 2 * 60 * 1_000L    // 2 minutes
        private const val DEDUPE_MAX_SIZE = 100                  // Compact before reaching this
    }

    // Repositories injected lazily in onListenerConnected to avoid early Koin access.
    private var expenseRepository: ExpenseRepository? = null
    private var categoryRepository: CategoryRepository? = null
    private var keywordRepository: KeywordMappingRepository? = null

    private var serviceScope: CoroutineScope? = null

    // notifId → last-seen epoch-millis
    private val dedupeMap = ConcurrentHashMap<String, Long>()

    private val zone: ZoneId = ZoneId.systemDefault()

    // ── Service lifecycle ──────────────────────────────────────────────────

    override fun onListenerConnected() {
        super.onListenerConnected()
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        // Resolve now — Koin is guaranteed to be ready by the time the listener binds.
        expenseRepository = getKoin().get()
        categoryRepository = getKoin().get()
        keywordRepository = getKoin().get()
        Log.i(TAG, "Notification listener connected")
    }

    override fun onListenerDisconnected() {
        serviceScope?.cancel()
        serviceScope = null
        Log.i(TAG, "Notification listener disconnected")
        super.onListenerDisconnected()
    }

    // ── Notification events ────────────────────────────────────────────────

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            if (!NotificationParser.isUpiApp(sbn.packageName)) return

            val notification = sbn.notification ?: return
            val extras = notification.extras ?: return

            // Skip group summaries and foreground service notifications — both are noisy duplicates.
            if (sbn.isGroup && (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return
            if ((notification.flags and Notification.FLAG_ONGOING_EVENT) != 0) return

            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
            val text = extractBestText(extras)

            if (title.isBlank() && text.isBlank()) return
            if (!NotificationParser.isTransactionNotification(title, text)) return

            val postTimeMillis = sbn.postTime
            val timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(postTimeMillis), zone)

            val tx = NotificationParser.parseNotification(
                title = title,
                text = text,
                packageName = sbn.packageName,
                postTimeMillis = postTimeMillis,
                sbnKey = sbn.key,
                timestamp = timestamp
            ) ?: run {
                Log.d(TAG, "Parse failed — pkg=${sbn.packageName} title=$title")
                return
            }

            if (isDuplicate(tx.notifId)) return

            processTransaction(tx)
        } catch (t: Throwable) {
            Log.e(TAG, "Crash in onNotificationPosted", t)
        }
    }

    // ── Transaction processing ─────────────────────────────────────────────

    private fun processTransaction(tx: ParsedNotificationTransaction) {
        val scope = serviceScope ?: return   // Listener already disconnected

        scope.launch {
            try {
                val expRepo = expenseRepository ?: return@launch
                val catRepo = categoryRepository ?: return@launch
                val kwRepo = keywordRepository ?: return@launch

                val merchant = tx.merchantName.orEmpty()

                // Step 1: Find category via keyword matching.
                val categoryId = when (val r = kwRepo.findCategoryForMerchant(merchant)) {
                    is Result.Success -> r.data
                    else -> null
                }

                // Step 2: Resolve the Category object. Fall back to "other" — never drop the expense.
                val category: Category = if (!categoryId.isNullOrBlank()) {
                    when (val r = catRepo.getCategoryById(categoryId)) {
                        is Result.Success -> r.data ?: Category.default()
                        else -> Category.default()
                    }
                } else {
                    Category.default()
                }

                val expense = tx.toExpense(category)

                // Realm PK provides permanent dedup even if the in-memory map has been evicted.
                val result = expRepo.addExpense(expense)
                if (result is Result.Error) {
                    Log.w(TAG, "addExpense failed: ${result.message}")
                } else {
                    Log.d(TAG, "Saved: ${expense.title} ₹${expense.amountAsDouble} (${tx.appName})")
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Error processing notification transaction", t)
            }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun extractBestText(extras: android.os.Bundle): String {
        extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?.takeIf { it.isNotBlank() }?.let { return it }

        extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?.takeIf { it.isNotBlank() }?.let { return it }

        extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.joinToString(" ") { it.toString() }
            ?.takeIf { it.isNotBlank() }?.let { return it }

        extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            ?.takeIf { it.isNotBlank() }?.let { return it }

        return ""
    }

    /**
     * Returns `true` if [notifId] was seen within [DEDUPE_TTL_MS].
     * Compact the map before it grows beyond [DEDUPE_MAX_SIZE] to bound memory use.
     */
    private fun isDuplicate(notifId: String): Boolean {
        val now = System.currentTimeMillis()

        if (dedupeMap.size >= DEDUPE_MAX_SIZE) {
            dedupeMap.entries.removeIf { now - it.value > DEDUPE_TTL_MS }
        }

        val previous = dedupeMap.putIfAbsent(notifId, now)
        if (previous == null) return false   // First time seen

        return if (now - previous <= DEDUPE_TTL_MS) {
            true    // Duplicate within TTL
        } else {
            dedupeMap[notifId] = now
            false   // TTL expired — treat as fresh
        }
    }
}