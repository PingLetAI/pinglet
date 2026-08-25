package com.linger.app.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Constraints
import androidx.work.NetworkType
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.local.db.DatabaseProvider
import com.linger.app.data.repository.AuthRepositoryImpl
import com.linger.app.data.repository.ContentRepository
import com.linger.app.data.repository.FeedRepository
import com.linger.app.data.repository.SessionManager
import com.linger.app.data.remote.ApiConfig
import com.linger.app.data.remote.RetrofitClient
import com.linger.app.widget.AmbientWidget
import com.linger.app.widget.rotation.RotationManager
import com.linger.app.widget.WidgetRotationSelector
import com.linger.app.widget.freeDefaults
import com.linger.app.worker.SyncWorker
import com.linger.app.worker.RotationWorker
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.GlanceId
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit

object SyncScheduler {
    private const val PERIODIC_WORK_TAG = "linger-sync"
    private const val FORCE_REFRESH_WORK_TAG = "linger-force-refresh"
    private const val ROTATION_WORK_TAG = "linger-rotation"
    private const val FAVORITE_SYNC_WORK_TAG = "linger-favorite-sync"
    private const val DEFAULT_MINUTES = 30L
    private val syncMutex = Mutex()

    fun scheduleInitialSync(context: Context) {
        schedulePeriodicSync(context)
        schedulePeriodicRotation(context)
        scheduleWidgetRefresh(context)
        scheduleImmediateNetworkSync(context)
    }

    private fun schedulePeriodicSync(context: Context) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(DEFAULT_MINUTES, TimeUnit.MINUTES)
            .setConstraints(networkConstraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    private fun schedulePeriodicRotation(context: Context) {
        val request = PeriodicWorkRequestBuilder<RotationWorker>(DEFAULT_MINUTES, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ROTATION_WORK_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun scheduleWidgetRefresh(context: Context) {
        val request = OneTimeWorkRequestBuilder<RotationWorker>().build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            FORCE_REFRESH_WORK_TAG,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun scheduleImmediateNetworkSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "$PERIODIC_WORK_TAG-immediate",
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    fun scheduleFavoriteSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            FAVORITE_SYNC_WORK_TAG,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request,
        )
    }

    fun scheduleWidgetAdded(context: Context) {
        scheduleWidgetRefresh(context)
        scheduleImmediateNetworkSync(context)
    }

    private fun networkConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    suspend fun syncAndRefresh(context: Context) = syncMutex.withLock {
        val dao = DatabaseProvider.database(context).contentDao()
        val dataStore = DataStoreManager(context)
        val api = RetrofitClient.build(ApiConfig.apiBaseUrl())
        val authRepository = AuthRepositoryImpl(api)
        val contentRepository = ContentRepository(api, dao)

        val session = SessionManager(authRepository, dataStore)
        val feed = session.withAuthRetry {
            flushPendingFavorites(dao, api)
            dataStore.setEntitlementPlan(api.getEntitlements().plan)
            contentRepository.syncFeed()
        }
        dataStore.mergeFeedFavoriteContentIds(
            feedContentIds = feed.mapTo(mutableSetOf()) { it.id },
            favoriteContentIds = feed.filter { it.favorite }.mapTo(mutableSetOf()) { it.id },
        )
        updateWidgets(context)
    }

    private suspend fun flushPendingFavorites(dao: com.linger.app.data.local.dao.ContentDao, api: com.linger.app.data.remote.AppApiService) {
        dao.pendingActions()
            .filter { it.actionType == "FAVORITE" || it.actionType == "UNFAVORITE" }
            .sortedBy { it.createdAt }
            .forEach { action ->
            try {
                when (action.actionType) {
                    "FAVORITE" -> api.favorite(action.payload)
                    "UNFAVORITE" -> api.unfavorite(action.payload)
                    else -> Unit
                }
                dao.deletePendingAction(action.id)
            } catch (error: Exception) {
                dao.incrementPendingActionAttempts(action.id)
                throw error
            }
        }
    }

    suspend fun rotateCachedContent(context: Context) = syncMutex.withLock {
        val dao = DatabaseProvider.database(context).contentDao()
        val api = RetrofitClient.build(ApiConfig.apiBaseUrl())
        ContentRepository(api, dao).seedBootstrapItemsIfQueueEmpty()
        refreshWidget(context)
    }

    suspend fun refreshWidget(context: Context) {
        val dataStore = DataStoreManager(context)
        val intervalMinutes = dataStore.refreshMinutes().firstOrNull() ?: DEFAULT_MINUTES.toInt()
        val dao = DatabaseProvider.database(context).contentDao()
        val now = System.currentTimeMillis()
        val widget = AmbientWidget()
        val ids = GlanceAppWidgetManager(context).getGlanceIds<AmbientWidget>(AmbientWidget::class.java)
        val favoriteIds = dataStore.readFavoriteContentIds()
        var first = true
        for (id in ids) {
            val key = id.toString()
            val stored = dataStore.readWidgetProfile(key)
            val effective = if (dataStore.readEntitlementPlan() == "PLUS") stored else stored.freeDefaults()
            val item = WidgetRotationSelector.select(dao, effective, key, favoriteIds) ?: continue
            val updated = stored.copy(
                currentContentId = item.id, currentText = item.text, currentAuthor = item.author,
                currentSourceUrl = item.sourceUrl, currentFavorite = item.id in favoriteIds,
                shownAt = now, nextChangeAt = RotationManager.nextChangeAt(now, intervalMinutes),
            )
            dataStore.setWidgetProfile(key, updated)
            if (first) {
                dataStore.setLastDisplayedWidgetState(item.id, item.text, item.author, item.sourceUrl, item.id in favoriteIds, now, updated.nextChangeAt)
                first = false
            }
            widget.update(context, id)
        }
        if (ids.isEmpty()) {
            FeedRepository(dao).selectNextItem(now, intervalMinutes)?.let { item ->
                dataStore.setLastDisplayedWidgetState(item.id, item.text, item.author, item.sourceUrl, item.id in favoriteIds, now, RotationManager.nextChangeAt(now, intervalMinutes))
            }
        }
    }

    suspend fun rotateWidget(context: Context, widgetKey: String) = syncMutex.withLock {
        val dataStore = DataStoreManager(context)
        val profile = dataStore.readWidgetProfile(widgetKey)
        dataStore.setWidgetProfile(widgetKey, profile.copy(manualOffset = profile.manualOffset + 1))
        val dao = DatabaseProvider.database(context).contentDao()
        val effective = if (dataStore.readEntitlementPlan() == "PLUS") profile.copy(manualOffset = profile.manualOffset + 1) else profile.freeDefaults()
        val item = WidgetRotationSelector.select(dao, effective, widgetKey, dataStore.readFavoriteContentIds()) ?: return@withLock
        val now = System.currentTimeMillis()
        val updated = profile.copy(
            manualOffset = profile.manualOffset + 1,
            currentContentId = item.id, currentText = item.text, currentAuthor = item.author,
            currentSourceUrl = item.sourceUrl, currentFavorite = dataStore.isContentFavorite(item.id),
            shownAt = now, nextChangeAt = RotationManager.nextChangeAt(now),
        )
        dataStore.setWidgetProfile(widgetKey, updated)
        updateWidgets(context)
    }

    private suspend fun updateWidgets(context: Context) {
        val glanceAppWidget = AmbientWidget()
        val widgetIds = GlanceAppWidgetManager(context).getGlanceIds<AmbientWidget>(
            AmbientWidget::class.java,
        )
        for (id in widgetIds) {
            glanceAppWidget.update(context, id)
        }
    }
}
