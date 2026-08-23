package com.linger.app.data.repository

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.local.db.DatabaseProvider
import com.linger.app.data.local.entity.PendingActionEntity
import com.linger.app.sync.SyncScheduler
import com.linger.app.widget.AmbientWidget
import java.util.UUID

object FavoriteRepository {
    suspend fun setFavorite(context: Context, contentItemId: String, favorite: Boolean) {
        val appContext = context.applicationContext
        val dataStore = DataStoreManager(appContext)
        val dao = DatabaseProvider.database(appContext).contentDao()
        val now = System.currentTimeMillis()

        dataStore.setContentFavorite(contentItemId, favorite)
        dataStore.readUserId().takeIf { it.isNotBlank() }?.let { userId ->
            dao.setUserContentFavorite(userId, contentItemId, favorite, now)
        }
        dao.replacePendingFavoriteAction(
            PendingActionEntity(
                id = UUID.randomUUID().toString(),
                actionType = if (favorite) "FAVORITE" else "UNFAVORITE",
                payload = contentItemId,
                createdAt = now,
            ),
        )
        AmbientWidget().updateAll(appContext)
        SyncScheduler.scheduleFavoriteSync(appContext)
    }
}
