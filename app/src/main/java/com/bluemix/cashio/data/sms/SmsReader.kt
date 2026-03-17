package com.bluemix.cashio.data.sms

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.bluemix.cashio.data.sms.SmsReader.Companion.BATCH_SIZE
import com.bluemix.cashio.data.sms.SmsReader.Companion.INCREMENTAL_LIMIT
import com.bluemix.cashio.data.sms.SmsReader.Companion.SAFE_BACKFILL_MS
import com.bluemix.cashio.domain.model.ParsedSmsTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Reads bank SMS messages from the device inbox and converts them into
 * [ParsedSmsTransaction] objects.
 *
 * ## Sync strategy
 * - **First launch**: reads all SMS in batches of [BATCH_SIZE] until the inbox
 *   is exhausted. Offset-based pagination via the `LIMIT/OFFSET` sort clause.
 * - **Subsequent launches**: reads only messages newer than `lastSyncTimestamp`,
 *   with a [SAFE_BACKFILL_MS] overlap to catch edge cases around the boundary.
 *
 * ## Thread safety
 * All public functions are `suspend` and switch to [Dispatchers.IO] internally.
 * Callers do not need to manage threading.
 *
 * ## Gaps in incremental sync
 * If [INCREMENTAL_LIMIT] (500) is hit and there are more messages than that
 * since the last sync (e.g. the app was unused for months), messages beyond the
 * limit are silently skipped. The stored timestamp is advanced only to the
 * newest message actually read, so the gap will close on the next sync call.
 * For large gaps, consider triggering [forceFullResync].
 */
class SmsReader(private val context: Context) {

    companion object {
        private const val TAG = "SmsReader"
        private const val SMS_INBOX_URI = "content://sms/inbox"
        private const val BATCH_SIZE = 500
        private const val INCREMENTAL_LIMIT = 500

        /** Overlap window to avoid missing messages at the sync boundary. */
        private const val SAFE_BACKFILL_MS = 60 * 60 * 1_000L   // 1 hour

        private const val PREF_NAME = "sms_sync_prefs"
        private const val KEY_INITIAL_SYNC_DONE = "initial_sync_done"
        private const val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
    }

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // Cache zone once per instance to avoid repeated system calls across batch rows.
    private val zone: ZoneId = ZoneId.systemDefault()

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Runs either a full initial sync or an incremental sync depending on state.
     * Safe to call from a coroutine — switches to [Dispatchers.IO] internally.
     */
    suspend fun syncTransactions(): List<ParsedSmsTransaction> = withContext(Dispatchers.IO) {
        val isInitialSyncDone = prefs.getBoolean(KEY_INITIAL_SYNC_DONE, false)
        if (!isInitialSyncDone) performInitialSync() else performIncrementalSync()
    }

    fun isInitialSyncDone(): Boolean =
        prefs.getBoolean(KEY_INITIAL_SYNC_DONE, false)

    fun getLastSyncInfo(): SyncInfo = SyncInfo(
        isInitialSyncDone = prefs.getBoolean(KEY_INITIAL_SYNC_DONE, false),
        lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC_TIMESTAMP, 0L)
    )

    /**
     * Resets sync state — next call to [syncTransactions] will re-scan all SMS.
     * Use when the user wants to re-import history (e.g. after a data clear).
     */
    fun forceFullResync() {
        Log.i(TAG, "Force full resync requested")
        prefs.edit()
            .putBoolean(KEY_INITIAL_SYNC_DONE, false)
            .remove(KEY_LAST_SYNC_TIMESTAMP)
            .apply()
    }

    // ── Sync strategies ────────────────────────────────────────────────────

    private fun performInitialSync(): List<ParsedSmsTransaction> {
        val allTransactions = mutableListOf<ParsedSmsTransaction>()
        var offset = 0

        while (true) {
            val batch = readSmsBatch(offset, BATCH_SIZE)
            if (batch.isEmpty()) break
            allTransactions.addAll(batch)
            offset += BATCH_SIZE
        }

        val maxMillis = allTransactions.maxOfOrNull { it.smsDateMillis }
            ?: System.currentTimeMillis()

        prefs.edit()
            .putBoolean(KEY_INITIAL_SYNC_DONE, true)
            .putLong(KEY_LAST_SYNC_TIMESTAMP, maxMillis)
            .apply()

        Log.i(TAG, "Initial sync complete: ${allTransactions.size} transactions")
        return allTransactions
    }

    private fun performIncrementalSync(): List<ParsedSmsTransaction> {
        val lastSync = prefs.getLong(KEY_LAST_SYNC_TIMESTAMP, 0L)
        val sinceMillis = if (lastSync > 0L) lastSync - SAFE_BACKFILL_MS else 0L

        val transactions = readRecentSms(limit = INCREMENTAL_LIMIT, sinceMillis = sinceMillis)

        val maxMillis = transactions.maxOfOrNull { it.smsDateMillis } ?: lastSync
        prefs.edit().putLong(KEY_LAST_SYNC_TIMESTAMP, maxMillis).apply()

        Log.i(TAG, "Incremental sync: ${transactions.size} transactions since ${lastSync}")
        return transactions
    }

    // ── Low-level readers ──────────────────────────────────────────────────

    private fun readSmsBatch(offset: Int, limit: Int): List<ParsedSmsTransaction> {
        val results = mutableListOf<ParsedSmsTransaction>()
        try {
            val uri = Uri.parse(SMS_INBOX_URI)
            val projection = arrayOf(Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.ADDRESS)
            // LIMIT/OFFSET injected into sort — works on AOSP; may fail on some OEM ROMs.
            // If this causes issues on specific devices, switch to cursor-based pagination.
            val sortOrder = "${Telephony.Sms.DATE} DESC LIMIT $limit OFFSET $offset"

            context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
                val bodyIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
                val addrIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)

                while (cursor.moveToNext()) {
                    val body = cursor.getString(bodyIdx) ?: continue
                    val dateMillis = cursor.getLong(dateIdx)
                    val address = cursor.getString(addrIdx)

                    if (!SmsParser.isBankSms(body)) continue

                    // Reuse cached zone — not ZoneId.systemDefault() per row.
                    val dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(dateMillis), zone
                    )

                    SmsParser.parseSms(body, dateTime, dateMillis, address)
                        ?.let { results.add(it) }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading SMS batch at offset=$offset: ${e.message}", e)
        }
        return results
    }

    private fun readRecentSms(limit: Int, sinceMillis: Long): List<ParsedSmsTransaction> {
        val results = mutableListOf<ParsedSmsTransaction>()
        try {
            val uri = Uri.parse(SMS_INBOX_URI)
            val projection = arrayOf(Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.ADDRESS)
            val selection = if (sinceMillis > 0L) "${Telephony.Sms.DATE} > ?" else null
            val selArgs = if (sinceMillis > 0L) arrayOf(sinceMillis.toString()) else null
            val sortOrder = "${Telephony.Sms.DATE} DESC LIMIT $limit"

            context.contentResolver.query(uri, projection, selection, selArgs, sortOrder)
                ?.use { cursor ->
                    val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                    val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)
                    val addrIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)

                    if (bodyIdx == -1 || dateIdx == -1) return@use

                    while (cursor.moveToNext()) {
                        val body = cursor.getString(bodyIdx) ?: continue
                        val dateMillis = cursor.getLong(dateIdx)
                        val address = if (addrIdx != -1) cursor.getString(addrIdx) else null

                        if (!SmsParser.isBankSms(body)) continue

                        val dateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(dateMillis), zone
                        )

                        SmsParser.parseSms(body, dateTime, dateMillis, address)
                            ?.let { results.add(it) }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading recent SMS: ${e.message}", e)
        }
        return results
    }
}

data class SyncInfo(
    val isInitialSyncDone: Boolean,
    val lastSyncTimestamp: Long
)