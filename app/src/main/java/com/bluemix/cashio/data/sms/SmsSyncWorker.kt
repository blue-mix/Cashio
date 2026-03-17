package com.bluemix.cashio.data.sms

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bluemix.cashio.domain.repository.ExpenseRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * [CoroutineWorker] that performs SMS-based expense sync in a guaranteed background execution context.
 *
 * ## Why this exists
 * Running a coroutine directly from [android.content.BroadcastReceiver.onReceive] is unsafe —
 * the OS destroys the receiver immediately after [onReceive] returns and can kill the process
 * before the coroutine completes. [WorkManager] provides guaranteed execution with automatic
 * retry on failure.
 *
 * ## Debounce via unique work
 * Enqueued with [ExistingWorkPolicy.KEEP] so that a burst of bank notifications
 * (card confirmation + bank debit SMS arriving simultaneously) collapses into a
 * single sync job rather than triggering redundant parallel imports.
 */
class SmsSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val expenseRepository: ExpenseRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            expenseRepository.refreshExpensesFromSms()
            Log.i(TAG, "SMS sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SMS sync failed: ${e.message}", e)
            // Retry up to the WorkManager default (3 attempts with backoff).
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "SmsSyncWorker"
        private const val UNIQUE_NAME = "sms_sync"

        /**
         * Enqueues a sync job. If one is already pending or running, the existing
         * job is kept and the new request is dropped ([ExistingWorkPolicy.KEEP]).
         */
        fun enqueue(workManager: WorkManager) {
            val request = OneTimeWorkRequestBuilder<SmsSyncWorker>().build()
            workManager.enqueueUniqueWork(UNIQUE_NAME, ExistingWorkPolicy.KEEP, request)
        }
    }
}