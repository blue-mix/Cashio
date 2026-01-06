//package com.bluemix.cashio.data.sms
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.provider.Telephony
//import android.util.Log
//import com.bluemix.cashio.domain.repository.ExpenseRepository
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.launch
//import org.koin.core.component.KoinComponent
//import org.koin.core.component.inject
//
///**
// * Broadcast receiver to detect incoming SMS in real-time
// */
//class SmsReceiver : BroadcastReceiver(), KoinComponent {
//
//    private val expenseRepository: ExpenseRepository by inject()
//    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
//
//    companion object {
//        private const val TAG = "SmsReceiver"
//    }
//
//    override fun onReceive(context: Context?, intent: Intent?) {
//        Log.d(TAG, "📩 SMS Received - Intent: ${intent?.action}")
//
//        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
//            Log.w(TAG, "⚠️ Not an SMS_RECEIVED action, ignoring")
//            return
//        }
//
//        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
//        Log.d(TAG, "📨 Received ${messages.size} SMS message(s)")
//
//        for ((index, smsMessage) in messages.withIndex()) {
//            val sender = smsMessage.displayOriginatingAddress
//            val messageBody = smsMessage.messageBody
//
//            Log.d(TAG, "📧 SMS #${index + 1} from: $sender")
//            Log.d(TAG, "💬 Message preview: ${messageBody.take(50)}...")
//
//            // Check if it's a bank SMS
//            if (SmsParser.isBankSms(messageBody)) {
//                Log.i(TAG, "🏦 Detected bank SMS from $sender")
//                Log.d(TAG, "📝 Full message: $messageBody")
//
//                receiverScope.launch {
//                    try {
//                        Log.d(TAG, "🔄 Starting expense refresh...")
//                        expenseRepository.refreshExpensesFromSms()
//                        Log.i(TAG, "✅ Expense refresh completed")
//                    } catch (e: Exception) {
//                        Log.e(TAG, "❌ Error refreshing expenses: ${e.message}", e)
//                    }
//                }
//            } else {
//                Log.d(TAG, "❌ Not a bank SMS, skipping")
//            }
//        }
//    }
//}
package com.bluemix.cashio.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.bluemix.cashio.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SmsReceiver : BroadcastReceiver(), KoinComponent {

    // Lazy injection because Receivers are created by the OS
    private val expenseRepository: ExpenseRepository by inject()
    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

        for (sms in messages) {
            val body = sms.messageBody ?: continue

            // Optimization: Only trigger DB refresh if it actually looks like a bank SMS
            if (SmsParser.isBankSms(body)) {
                Log.d("SmsReceiver", "🏦 Bank SMS detected! Triggering refresh.")
                receiverScope.launch {
                    expenseRepository.refreshExpensesFromSms()
                }
            }
        }
    }
}