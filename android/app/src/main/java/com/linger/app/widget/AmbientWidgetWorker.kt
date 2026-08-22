package com.linger.app.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.linger.app.sync.SyncScheduler

class AmbientWidgetWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        SyncScheduler.scheduleWidgetRefresh(applicationContext)
        return Result.success()
    }
}

