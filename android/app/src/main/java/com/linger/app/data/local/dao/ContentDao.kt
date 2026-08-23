package com.linger.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.linger.app.data.local.entity.*

@Dao
interface ContentDao {
    @Query("SELECT * FROM content")
    suspend fun allContent(): List<ContentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContent(content: List<ContentEntity>)

    @Query("SELECT * FROM content WHERE id = :id LIMIT 1")
    suspend fun contentById(id: String): ContentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategory(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQueue(items: List<QueueItemEntity>)

    @Query("SELECT * FROM queue_items ORDER BY slotIndex LIMIT :limit")
    suspend fun queue(limit: Int = 200): List<QueueItemEntity>

    @Query("DELETE FROM queue_items")
    suspend fun clearQueue()

    @Transaction
    suspend fun replaceFeed(content: List<ContentEntity>, queue: List<QueueItemEntity>) {
        upsertContent(content)
        clearQueue()
        upsertQueue(queue)
    }

    @Query("UPDATE queue_items SET displayed = 1 WHERE id = :id")
    suspend fun markQueueItemDisplayed(id: String)

    @Query("SELECT * FROM pending_actions")
    suspend fun pendingActions(): List<PendingActionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPendingActions(actions: List<PendingActionEntity>)

    @Query("DELETE FROM pending_actions WHERE id = :id")
    suspend fun deletePendingAction(id: String)

    @Query("DELETE FROM pending_actions WHERE payload = :contentItemId")
    suspend fun deletePendingActionsForContent(contentItemId: String)

    @Query("UPDATE pending_actions SET attempts = attempts + 1 WHERE id = :id")
    suspend fun incrementPendingActionAttempts(id: String)

    @Transaction
    suspend fun replacePendingFavoriteAction(action: PendingActionEntity) {
        deletePendingActionsForContent(action.payload)
        upsertPendingActions(listOf(action))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entries: List<DisplayHistoryEntity>)

    @Query("SELECT * FROM display_history ORDER BY shownAt DESC LIMIT :limit")
    suspend fun recentHistory(limit: Int = 40): List<DisplayHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserContent(items: List<UserContentEntity>)

    @Query("SELECT * FROM user_content WHERE userId = :userId")
    suspend fun userLibrary(userId: String): List<UserContentEntity>

    @Query("UPDATE user_content SET favorite = :favorite, updatedAt = :updatedAt WHERE userId = :userId AND contentItemId = :contentItemId")
    suspend fun setUserContentFavorite(userId: String, contentItemId: String, favorite: Boolean, updatedAt: Long)
}
