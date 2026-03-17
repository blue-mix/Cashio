package com.bluemix.cashio.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.work.WorkManager

/**
 * Receives incoming SMS broadcasts and schedules an SMS sync via [WorkManager].
 *
 * ## Why WorkManager instead of a direct coroutine launch
 * [BroadcastReceiver] instances are destroyed immediately after [onReceive] returns.
 * The OS is free to kill the process at that point — any coroutine launched from
 * [onReceive] may never complete. [WorkManager] guarantees execution even if the
 * process is killed mid-way.
 *
 * ## Debounce
 * [WorkManager]'s [UniqueWork] with [ExistingWorkPolicy.KEEP] (set in [SmsSyncWorker])
 * ensures that a burst of bank SMS (e.g. card + bank confirmation for one payment)
 * does not enqueue multiple redundant sync jobs.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return

        // Fast pre-filter: only trigger a sync job if at least one message looks like a bank SMS.
        val hasBankSms = messages.any { SmsParser.isBankSms(it.messageBody ?: "") }
        if (!hasBankSms) return

        Log.d("SmsReceiver", "Bank SMS detected — scheduling sync")
        SmsSyncWorker.enqueue(WorkManager.getInstance(context))
    }
}