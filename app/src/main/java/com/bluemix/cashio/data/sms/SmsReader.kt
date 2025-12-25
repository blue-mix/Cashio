//
//package com.bluemix.cashio.data.sms
//
//import android.content.Context
//import android.net.Uri
//import android.provider.Telephony
//import android.util.Log
//import com.bluemix.cashio.domain.model.ParsedSmsTransaction
//import java.time.Instant
//import java.time.LocalDateTime
//import java.time.ZoneId
//
///**
// * Read SMS messages from device
// */
//class SmsReader(private val context: Context) {
//
//    companion object {
//        private const val TAG = "SmsReader"
//        private const val DEFAULT_LIMIT = 100
//        private const val SMS_INBOX_URI = "content://sms/inbox"
//    }
//
//    fun readRecentSms(
//        limit: Int = DEFAULT_LIMIT,
//        sinceTimestamp: Long? = null
//    ): List<ParsedSmsTransaction> {
//        Log.d(TAG, "📖 Starting SMS read - Limit: $limit, Since: $sinceTimestamp")
//
//        val parsedTransactions = mutableListOf<ParsedSmsTransaction>()
//        var totalRead = 0
//        var bankSmsFound = 0
//        var successfullyParsed = 0
//
//        try {
//            val uri = Uri.parse(SMS_INBOX_URI)
//            val projection = arrayOf(
//                Telephony.Sms.BODY,
//                Telephony.Sms.DATE,
//                Telephony.Sms.ADDRESS
//            )
//
//            val selection = if (sinceTimestamp != null) {
//                "${Telephony.Sms.DATE} > ?"
//            } else {
//                null
//            }
//
//            val selectionArgs = if (sinceTimestamp != null) {
//                arrayOf(sinceTimestamp.toString())
//            } else {
//                null
//            }
//
//            val sortOrder = "${Telephony.Sms.DATE} DESC LIMIT $limit"
//
//            context.contentResolver.query(
//                uri,
//                projection,
//                selection,
//                selectionArgs,
//                sortOrder
//            )?.use { cursor ->
//                val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
//                val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
//                val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
//
//                Log.d(TAG, "📨 Found ${cursor.count} total SMS messages")
//
//                while (cursor.moveToNext()) {
//                    totalRead++
//                    try {
//                        val body = cursor.getString(bodyIndex)
//                        val dateMillis = cursor.getLong(dateIndex)
//                        val sender = cursor.getString(addressIndex)
//                        val dateTime = LocalDateTime.ofInstant(
//                            Instant.ofEpochMilli(dateMillis),
//                            ZoneId.systemDefault()
//                        )
//
//                        Log.v(TAG, "📧 SMS #$totalRead from $sender")
//                        // ✅ ADD THIS: Print SBI SMS content
//                        if (sender.contains("SBIUPI", ignoreCase = true)) {
//                            Log.i(TAG, "🏦 SBI UPI SMS from $sender:")
//                            Log.i(TAG, "   Content: $body")
//                        }
//                        if (SmsParser.isBankSms(body)) {
//                            bankSmsFound++
//                            Log.d(TAG, "🏦 Bank SMS #$bankSmsFound detected from $sender")
//                            Log.v(TAG, "💬 Message: ${body.take(100)}...")
//
//                            val parsed = SmsParser.parseSms(body, dateTime)
//                            if (parsed != null) {
//                                successfullyParsed++
//                                parsedTransactions.add(parsed)
//                                Log.i(TAG, "✅ Successfully parsed: ${parsed.merchantName ?: "Unknown"} - ₹${parsed.amount}")
//                            } else {
//                                Log.w(TAG, "⚠️ Bank SMS detected but parsing failed")
//                            }
//                        }
//                    } catch (e: Exception) {
//                        Log.e(TAG, "❌ Error processing SMS #$totalRead: ${e.message}", e)
//                    }
//                }
//            }
//
//            Log.i(TAG, "📊 Summary: Read $totalRead SMS, Found $bankSmsFound bank SMS, Parsed $successfullyParsed transactions")
//
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ Fatal error reading SMS: ${e.message}", e)
//        }
//
//        return parsedTransactions
//    }
//
//    fun getLastSmsTimestamp(): Long? {
//        return try {
//            val uri = Uri.parse(SMS_INBOX_URI)
//            val projection = arrayOf(Telephony.Sms.DATE)
//            val sortOrder = "${Telephony.Sms.DATE} DESC LIMIT 1"
//
//            context.contentResolver.query(
//                uri,
//                projection,
//                null,
//                null,
//                sortOrder
//            )?.use { cursor ->
//                if (cursor.moveToFirst()) {
//                    val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))
//                    Log.d(TAG, "🕐 Last SMS timestamp: $timestamp")
//                    timestamp
//                } else {
//                    Log.d(TAG, "📭 No SMS found")
//                    null
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ Error getting last SMS timestamp: ${e.message}", e)
//            null
//        }
//    }
//}
package com.bluemix.cashio.data.sms

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.bluemix.cashio.domain.model.ParsedSmsTransaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Read SMS messages with smart sync:
 * - First time: Parse ALL SMS (no limit)
 * - Next times: Only parse NEW SMS (incremental)
 */
class SmsReader(private val context: Context) {

    companion object {
        private const val TAG = "SmsReader"
        private const val SMS_INBOX_URI = "content://sms/inbox"
        private const val BATCH_SIZE = 500 // Read in batches to avoid memory issues

        // SharedPreferences keys
        private const val PREF_NAME = "sms_sync_prefs"
        private const val KEY_INITIAL_SYNC_DONE = "initial_sync_done"
        private const val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
    }

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Smart sync: Full sync first time, then only new SMS
     */
    fun syncTransactions(): List<ParsedSmsTransaction> {
        val isInitialSyncDone = prefs.getBoolean(KEY_INITIAL_SYNC_DONE, false)

        return if (!isInitialSyncDone) {
            Log.i(TAG, "🔄 INITIAL SYNC - Reading ALL SMS (first time)")
            performInitialSync()
        } else {
            Log.i(TAG, "🔄 INCREMENTAL SYNC - Only new SMS")
            performIncrementalSync()
        }
    }

    /**
     * First time: Read ALL SMS in batches
     */
    private fun performInitialSync(): List<ParsedSmsTransaction> {
        val allTransactions = mutableListOf<ParsedSmsTransaction>()
        var offset = 0

        while (true) {
            Log.d(TAG, "📦 Reading batch: offset=$offset, size=$BATCH_SIZE")
            val batch = readSmsBatch(offset, BATCH_SIZE)

            if (batch.isEmpty()) {
                Log.d(TAG, "✅ No more SMS to read")
                break
            }

            allTransactions.addAll(batch)
            offset += BATCH_SIZE

            Log.d(TAG, "📊 Progress: ${allTransactions.size} transactions parsed so far")
        }

        // Mark initial sync as complete
        prefs.edit()
            .putBoolean(KEY_INITIAL_SYNC_DONE, true)
            .putLong(KEY_LAST_SYNC_TIMESTAMP, System.currentTimeMillis())
            .apply()

        Log.i(TAG, "✅ Initial sync complete: ${allTransactions.size} total transactions")
        return allTransactions
    }

    /**
     * Subsequent syncs: Only read new SMS
     */
    private fun performIncrementalSync(): List<ParsedSmsTransaction> {
        val lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC_TIMESTAMP, 0)
        Log.d(TAG, "📅 Last sync: ${java.util.Date(lastSyncTimestamp)}")

        val newTransactions = readRecentSms(
            limit = 500, // Reasonable limit for new SMS
            sinceTimestamp = lastSyncTimestamp
        )

        // Update last sync timestamp
        prefs.edit()
            .putLong(KEY_LAST_SYNC_TIMESTAMP, System.currentTimeMillis())
            .apply()

        Log.i(TAG, "✅ Incremental sync: ${newTransactions.size} new transactions")
        return newTransactions
    }

    /**
     * Read SMS in batches (for initial sync)
     */
    private fun readSmsBatch(offset: Int, limit: Int): List<ParsedSmsTransaction> {
        val transactions = mutableListOf<ParsedSmsTransaction>()
        var totalRead = 0
        var bankSmsFound = 0
        var successfullyParsed = 0

        try {
            val uri = Uri.parse(SMS_INBOX_URI)
            val projection = arrayOf(
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.ADDRESS
            )

            // Sort by date DESC, with offset and limit
            val sortOrder = "${Telephony.Sms.DATE} DESC LIMIT $limit OFFSET $offset"

            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
                val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)

                Log.v(TAG, "📨 Batch found ${cursor.count} SMS")

                while (cursor.moveToNext()) {
                    totalRead++
                    try {
                        val body = cursor.getString(bodyIndex)
                        val dateMillis = cursor.getLong(dateIndex)
                        cursor.getString(addressIndex)
                        val dateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(dateMillis),
                            ZoneId.systemDefault()
                        )

                        if (SmsParser.isBankSms(body)) {
                            bankSmsFound++
                            val parsed = SmsParser.parseSms(body, dateTime)
                            if (parsed != null) {
                                successfullyParsed++
                                transactions.add(parsed)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error processing SMS: ${e.message}")
                    }
                }
            }

            Log.v(
                TAG,
                "📊 Batch summary: Read $totalRead, Found $bankSmsFound bank SMS, Parsed $successfullyParsed"
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error reading batch: ${e.message}", e)
        }

        return transactions
    }

    /**
     * Read recent SMS (for incremental sync)
     */
    fun readRecentSms(
        limit: Int = 500,
        sinceTimestamp: Long? = null
    ): List<ParsedSmsTransaction> {
        Log.d(TAG, "📖 Reading recent SMS - Limit: $limit, Since: $sinceTimestamp")

        val parsedTransactions = mutableListOf<ParsedSmsTransaction>()
        var totalRead = 0
        var bankSmsFound = 0
        var successfullyParsed = 0

        try {
            val uri = Uri.parse(SMS_INBOX_URI)
            val projection = arrayOf(
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.ADDRESS
            )

            val selection = if (sinceTimestamp != null) {
                "${Telephony.Sms.DATE} > ?"
            } else {
                null
            }

            val selectionArgs = if (sinceTimestamp != null) {
                arrayOf(sinceTimestamp.toString())
            } else {
                null
            }

            val sortOrder = "${Telephony.Sms.DATE} DESC LIMIT $limit"

            context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
                val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)

                Log.d(TAG, "📨 Found ${cursor.count} SMS messages")

                while (cursor.moveToNext()) {
                    totalRead++
                    try {
                        val body = cursor.getString(bodyIndex)
                        val dateMillis = cursor.getLong(dateIndex)
                        val sender = cursor.getString(addressIndex)
                        val dateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(dateMillis),
                            ZoneId.systemDefault()
                        )

                        Log.v(TAG, "📧 SMS #$totalRead from $sender")

                        if (SmsParser.isBankSms(body)) {
                            bankSmsFound++
                            Log.d(TAG, "🏦 Bank SMS #$bankSmsFound from $sender")
                            Log.v(TAG, "💬 ${body.take(100)}...")

                            val parsed = SmsParser.parseSms(body, dateTime)
                            if (parsed != null) {
                                successfullyParsed++
                                parsedTransactions.add(parsed)
                                Log.i(
                                    TAG,
                                    "✅ Parsed: ${parsed.merchantName ?: "Unknown"} - ₹${parsed.amount}"
                                )
                            } else {
                                Log.w(TAG, "⚠️ Parsing failed")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error: ${e.message}")
                    }
                }
            }

            Log.i(
                TAG,
                "📊 Summary: Read $totalRead, Found $bankSmsFound bank SMS, Parsed $successfullyParsed"
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ Fatal error: ${e.message}", e)
        }

        return parsedTransactions
    }

    /**
     * Force full resync (for settings/debug)
     */
    fun forceFullResync() {
        Log.i(TAG, "🔄 Force full resync requested")
        prefs.edit()
            .putBoolean(KEY_INITIAL_SYNC_DONE, false)
            .remove(KEY_LAST_SYNC_TIMESTAMP)
            .apply()
    }

    /**
     * Check if initial sync is done
     */
    fun isInitialSyncDone(): Boolean {
        return prefs.getBoolean(KEY_INITIAL_SYNC_DONE, false)
    }

    /**
     * Get last sync info for UI display
     */
    fun getLastSyncInfo(): SyncInfo {
        return SyncInfo(
            isInitialSyncDone = prefs.getBoolean(KEY_INITIAL_SYNC_DONE, false),
            lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC_TIMESTAMP, 0)
        )
    }

    fun getLastSmsTimestamp(): Long? {
        return try {
            val uri = Uri.parse(SMS_INBOX_URI)
            val projection = arrayOf(Telephony.Sms.DATE)
            val sortOrder = "${Telephony.Sms.DATE} DESC LIMIT 1"

            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting timestamp: ${e.message}")
            null
        }
    }
}

/**
 * Sync status info for UI
 */
data class SyncInfo(
    val isInitialSyncDone: Boolean,
    val lastSyncTimestamp: Long
)
