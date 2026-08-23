package com.linger.app.data.local.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\bg\u0018\u00002\u00020\u0001J\u0014\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0005J\u000e\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u0005J\u0018\u0010\b\u001a\u0004\u0018\u00010\u00042\u0006\u0010\t\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u0016\u0010\f\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u001c\u0010\r\u001a\u00020\u00072\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u0011\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0005J\u001e\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\u00032\b\b\u0002\u0010\u0016\u001a\u00020\u0017H\u00a7@\u00a2\u0006\u0002\u0010\u0018J\u001e\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00032\b\b\u0002\u0010\u0016\u001a\u00020\u0017H\u00a7@\u00a2\u0006\u0002\u0010\u0018J*\u0010\u001a\u001a\u00020\u00072\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\u0003H\u0097@\u00a2\u0006\u0002\u0010\u001cJ.\u0010\u001d\u001a\u00020\u00072\u0006\u0010\u001e\u001a\u00020\n2\u0006\u0010\u001f\u001a\u00020\n2\u0006\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020#H\u00a7@\u00a2\u0006\u0002\u0010$J\u001c\u0010%\u001a\u00020\u00072\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\'0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u001c\u0010(\u001a\u00020\u00072\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u001c\u0010)\u001a\u00020\u00072\f\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00130\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u001c\u0010+\u001a\u00020\u00072\f\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00150\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u001c\u0010-\u001a\u00020\u00072\f\u0010,\u001a\b\u0012\u0004\u0012\u00020.0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u001c\u0010/\u001a\b\u0012\u0004\u0012\u00020.0\u00032\u0006\u0010\u001e\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000b\u00a8\u00060"}, d2 = {"Lcom/linger/app/data/local/dao/ContentDao;", "", "allContent", "", "Lcom/linger/app/data/local/entity/ContentEntity;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearQueue", "", "contentById", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deletePendingAction", "insertHistory", "entries", "Lcom/linger/app/data/local/entity/DisplayHistoryEntity;", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markQueueItemDisplayed", "pendingActions", "Lcom/linger/app/data/local/entity/PendingActionEntity;", "queue", "Lcom/linger/app/data/local/entity/QueueItemEntity;", "limit", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "recentHistory", "replaceFeed", "content", "(Ljava/util/List;Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setUserContentFavorite", "userId", "contentItemId", "favorite", "", "updatedAt", "", "(Ljava/lang/String;Ljava/lang/String;ZJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upsertCategory", "categories", "Lcom/linger/app/data/local/entity/CategoryEntity;", "upsertContent", "upsertPendingActions", "actions", "upsertQueue", "items", "upsertUserContent", "Lcom/linger/app/data/local/entity/UserContentEntity;", "userLibrary", "app_debug"})
@androidx.room.Dao()
public abstract interface ContentDao {
    
    @androidx.room.Query(value = "SELECT * FROM content")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object allContent(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.linger.app.data.local.entity.ContentEntity>> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upsertContent(@org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.ContentEntity> content, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM content WHERE id = :id LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object contentById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.linger.app.data.local.entity.ContentEntity> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upsertCategory(@org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.CategoryEntity> categories, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upsertQueue(@org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.QueueItemEntity> items, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM queue_items ORDER BY slotIndex LIMIT :limit")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object queue(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.linger.app.data.local.entity.QueueItemEntity>> $completion);
    
    @androidx.room.Query(value = "DELETE FROM queue_items")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object clearQueue(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Transaction()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object replaceFeed(@org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.ContentEntity> content, @org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.QueueItemEntity> queue, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE queue_items SET displayed = 1 WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markQueueItemDisplayed(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM pending_actions")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object pendingActions(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.linger.app.data.local.entity.PendingActionEntity>> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upsertPendingActions(@org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.PendingActionEntity> actions, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM pending_actions WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deletePendingAction(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertHistory(@org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.DisplayHistoryEntity> entries, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM display_history ORDER BY shownAt DESC LIMIT :limit")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object recentHistory(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.linger.app.data.local.entity.DisplayHistoryEntity>> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upsertUserContent(@org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.UserContentEntity> items, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM user_content WHERE userId = :userId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object userLibrary(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.linger.app.data.local.entity.UserContentEntity>> $completion);
    
    @androidx.room.Query(value = "UPDATE user_content SET favorite = :favorite, updatedAt = :updatedAt WHERE userId = :userId AND contentItemId = :contentItemId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object setUserContentFavorite(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    java.lang.String contentItemId, boolean favorite, long updatedAt, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
        
        @androidx.room.Transaction()
        @org.jetbrains.annotations.Nullable()
        public static java.lang.Object replaceFeed(@org.jetbrains.annotations.NotNull()
        com.linger.app.data.local.dao.ContentDao $this, @org.jetbrains.annotations.NotNull()
        java.util.List<com.linger.app.data.local.entity.ContentEntity> content, @org.jetbrains.annotations.NotNull()
        java.util.List<com.linger.app.data.local.entity.QueueItemEntity> queue, @org.jetbrains.annotations.NotNull()
        kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
            return null;
        }
    }
}