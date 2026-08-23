package com.linger.app.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.local.db.DatabaseProvider
import com.linger.app.data.repository.AuthRepositoryImpl
import com.linger.app.data.repository.ContentRepository
import com.linger.app.data.repository.FeedRepository
import com.linger.app.data.remote.ApiConfig
import com.linger.app.data.remote.RetrofitClient
import com.linger.app.widget.AmbientWidget
import com.linger.app.widget.rotation.RotationManager
import com.linger.app.worker.SyncWorker
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.GlanceId
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.util.UUID
import java.util.concurrent.TimeUnit

object SyncScheduler {
    private const val PERIODIC_WORK_TAG = "linger-sync"
    private const val FORCE_REFRESH_WORK_TAG = "linger-force-refresh"
    private const val DEFAULT_MINUTES = 30L
    private val syncMutex = Mutex()

    fun scheduleInitialSync(context: Context) {
        schedulePeriodicSync(context)
    }

    private fun schedulePeriodicSync(context: Context) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(DEFAULT_MINUTES, TimeUnit.MINUTES)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun scheduleWidgetRefresh(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            FORCE_REFRESH_WORK_TAG,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    suspend fun syncAndRefresh(context: Context) = syncMutex.withLock {
        val dao = DatabaseProvider.database(context).contentDao()
        val dataStore = DataStoreManager(context)
        val api = RetrofitClient.build(ApiConfig.apiBaseUrl())
        val authRepository = AuthRepositoryImpl(api)
        val contentRepository = ContentRepository(api, dao)

        runCatching {
            establishSession(dataStore, authRepository)
            syncFeedWithAuthRecovery(contentRepository, authRepository, dataStore)
        }
        contentRepository.seedBootstrapItemsIfQueueEmpty()

        refreshWidget(context)
    }

    private suspend fun establishSession(dataStore: DataStoreManager, authRepository: AuthRepositoryImpl) {
        val installationId = readOrCreateInstallationId(dataStore)
        val existingAccessToken = dataStore.readAccessToken()

        if (existingAccessToken.isNotBlank()) {
            RetrofitClient.setAuthToken(existingAccessToken)
            return
        }

        val auth = authRepository.anonymous(installationId)
        dataStore.setAuthSession(auth.accessToken, auth.refreshToken, auth.userId)
        RetrofitClient.setAuthToken(auth.accessToken)
    }

    private suspend fun syncFeedWithAuthRecovery(
        contentRepository: ContentRepository,
        authRepository: AuthRepositoryImpl,
        dataStore: DataStoreManager,
    ) {
        try {
            contentRepository.syncFeed()
            return
        } catch (e: HttpException) {
            if (e.code() != 401) return
        } catch (_: Exception) {
            return
        }

        if (!refreshOrReAuthenticate(authRepository, dataStore)) {
            return
        }

        runCatching { contentRepository.syncFeed() }
    }

    private suspend fun refreshOrReAuthenticate(
        authRepository: AuthRepositoryImpl,
        dataStore: DataStoreManager,
    ): Boolean {
        val installationId = readOrCreateInstallationId(dataStore)
        val refreshToken = dataStore.readRefreshToken()

        if (refreshToken.isBlank()) {
            return try {
                val auth = authRepository.anonymous(installationId)
                dataStore.setAuthSession(auth.accessToken, auth.refreshToken, auth.userId)
                RetrofitClient.setAuthToken(auth.accessToken)
                true
            } catch (_: Exception) {
                false
            }
        }

        return try {
            val refreshed = authRepository.refresh(refreshToken)
            dataStore.setAuthSession(refreshed.accessToken, refreshToken, dataStore.readUserId())
            RetrofitClient.setAuthToken(refreshed.accessToken)
            true
        } catch (_: Exception) {
            dataStore.clearAuthSession()
            try {
                val auth = authRepository.anonymous(installationId)
                dataStore.setAuthSession(auth.accessToken, auth.refreshToken, auth.userId)
                RetrofitClient.setAuthToken(auth.accessToken)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    private suspend fun readOrCreateInstallationId(dataStore: DataStoreManager): String {
        val existing = dataStore.readInstallationId()
        if (existing.isNotBlank()) return existing

        val generated = UUID.randomUUID().toString()
        dataStore.setInstallationId(generated)
        return generated
    }

    suspend fun refreshWidget(context: Context) {
        val dataStore = DataStoreManager(context)
        val intervalMinutes = dataStore.refreshMinutes().firstOrNull() ?: DEFAULT_MINUTES.toInt()
        val dao = DatabaseProvider.database(context).contentDao()
        val feedRepository = FeedRepository(dao)

        val now = System.currentTimeMillis()
        val item = feedRepository.selectNextItem(now, intervalMinutes)

        if (item != null) {
            dataStore.setLastDisplayedWidgetState(
                contentItemId = item.id,
                text = item.text,
                author = item.author,
                shownAt = now,
                nextChangeAt = RotationManager.nextChangeAt(now, intervalMinutes),
            )
        }

        val glanceAppWidget = AmbientWidget()
        val widgetIds = GlanceAppWidgetManager(context).getGlanceIds<AmbientWidget>(
            AmbientWidget::class.java,
        )
        for (id in widgetIds) {
            glanceAppWidget.update(context, id)
        }
    }
}
