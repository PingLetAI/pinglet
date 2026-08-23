package com.linger.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.linger.app.sync.SyncScheduler

class RotationWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = try {
        SyncScheduler.rotateCachedContent(applicationContext)
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }
}
