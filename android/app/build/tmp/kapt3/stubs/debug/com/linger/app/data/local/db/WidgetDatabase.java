package com.linger.app.data.local.db;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&\u00a8\u0006\u0005"}, d2 = {"Lcom/linger/app/data/local/db/WidgetDatabase;", "Landroidx/room/RoomDatabase;", "()V", "contentDao", "Lcom/linger/app/data/local/dao/ContentDao;", "app_debug"})
@androidx.room.Database(entities = {com.linger.app.data.local.entity.ContentEntity.class, com.linger.app.data.local.entity.CategoryEntity.class, com.linger.app.data.local.entity.ContentCategoryEntity.class, com.linger.app.data.local.entity.QueueItemEntity.class, com.linger.app.data.local.entity.PreferenceEntity.class, com.linger.app.data.local.entity.PendingActionEntity.class, com.linger.app.data.local.entity.DisplayHistoryEntity.class, com.linger.app.data.local.entity.UserContentEntity.class}, version = 1, exportSchema = false)
public abstract class WidgetDatabase extends androidx.room.RoomDatabase {
    
    public WidgetDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.linger.app.data.local.dao.ContentDao contentDao();
}