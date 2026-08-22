package com.linger.app

import android.app.Application
import com.linger.app.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LingerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SyncScheduler.scheduleInitialSync(this)
    }
}
