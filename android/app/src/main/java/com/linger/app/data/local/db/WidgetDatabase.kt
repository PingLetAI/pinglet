package com.linger.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.linger.app.data.local.dao.ContentDao
import com.linger.app.data.local.entity.*

@Database(
    entities = [
        ContentEntity::class,
        CategoryEntity::class,
        ContentCategoryEntity::class,
        QueueItemEntity::class,
        PreferenceEntity::class,
        PendingActionEntity::class,
        DisplayHistoryEntity::class,
        UserContentEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class WidgetDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
}
