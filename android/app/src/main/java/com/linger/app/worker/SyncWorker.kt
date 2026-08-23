package com.linger.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.linger.app.sync.SyncScheduler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        try {
            SyncScheduler.syncAndRefresh(applicationContext)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
