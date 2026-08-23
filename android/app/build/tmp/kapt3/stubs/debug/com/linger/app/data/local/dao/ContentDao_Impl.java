package com.linger.app.data.local.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000h\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0002\b\f\b\u0007\u0018\u0000 :2\u00020\u0001:\u0001:B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\t0\u0013H\u0096@\u00a2\u0006\u0002\u0010\u0014J\u000e\u0010\u0015\u001a\u00020\u0016H\u0096@\u00a2\u0006\u0002\u0010\u0014J\u0018\u0010\u0017\u001a\u0004\u0018\u00010\t2\u0006\u0010\u0018\u001a\u00020\u0019H\u0096@\u00a2\u0006\u0002\u0010\u001aJ\u0016\u0010\u001b\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u0019H\u0096@\u00a2\u0006\u0002\u0010\u001aJ\u001c\u0010\u001c\u001a\u00020\u00162\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0013H\u0096@\u00a2\u0006\u0002\u0010\u001eJ\u0016\u0010\u001f\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u0019H\u0096@\u00a2\u0006\u0002\u0010\u001aJ\u0014\u0010 \u001a\b\u0012\u0004\u0012\u00020\r0\u0013H\u0096@\u00a2\u0006\u0002\u0010\u0014J\u001c\u0010!\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00132\u0006\u0010\"\u001a\u00020#H\u0096@\u00a2\u0006\u0002\u0010$J\u001c\u0010%\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00132\u0006\u0010\"\u001a\u00020#H\u0096@\u00a2\u0006\u0002\u0010$J*\u0010&\u001a\u00020\u00162\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\t0\u00132\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0013H\u0096@\u00a2\u0006\u0002\u0010(J.\u0010)\u001a\u00020\u00162\u0006\u0010*\u001a\u00020\u00192\u0006\u0010+\u001a\u00020\u00192\u0006\u0010,\u001a\u00020-2\u0006\u0010.\u001a\u00020/H\u0096@\u00a2\u0006\u0002\u00100J\u001c\u00101\u001a\u00020\u00162\f\u00102\u001a\b\u0012\u0004\u0012\u00020\u00070\u0013H\u0096@\u00a2\u0006\u0002\u0010\u001eJ\u001c\u00103\u001a\u00020\u00162\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\t0\u0013H\u0096@\u00a2\u0006\u0002\u0010\u001eJ\u001c\u00104\u001a\u00020\u00162\f\u00105\u001a\b\u0012\u0004\u0012\u00020\r0\u0013H\u0096@\u00a2\u0006\u0002\u0010\u001eJ\u001c\u00106\u001a\u00020\u00162\f\u00107\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0013H\u0096@\u00a2\u0006\u0002\u0010\u001eJ\u001c\u00108\u001a\u00020\u00162\f\u00107\u001a\b\u0012\u0004\u0012\u00020\u00110\u0013H\u0096@\u00a2\u0006\u0002\u0010\u001eJ\u001c\u00109\u001a\b\u0012\u0004\u0012\u00020\u00110\u00132\u0006\u0010*\u001a\u00020\u0019H\u0096@\u00a2\u0006\u0002\u0010\u001aR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006;"}, d2 = {"Lcom/linger/app/data/local/dao/ContentDao_Impl;", "Lcom/linger/app/data/local/dao/ContentDao;", "__db", "Landroidx/room/RoomDatabase;", "(Landroidx/room/RoomDatabase;)V", "__insertAdapterOfCategoryEntity", "Landroidx/room/EntityInsertAdapter;", "Lcom/linger/app/data/local/entity/CategoryEntity;", "__insertAdapterOfContentEntity", "Lcom/linger/app/data/local/entity/ContentEntity;", "__insertAdapterOfDisplayHistoryEntity", "Lcom/linger/app/data/local/entity/DisplayHistoryEntity;", "__insertAdapterOfPendingActionEntity", "Lcom/linger/app/data/local/entity/PendingActionEntity;", "__insertAdapterOfQueueItemEntity", "Lcom/linger/app/data/local/entity/QueueItemEntity;", "__insertAdapterOfUserContentEntity", "Lcom/linger/app/data/local/entity/UserContentEntity;", "allContent", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearQueue", "", "contentById", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deletePendingAction", "insertHistory", "entries", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markQueueItemDisplayed", "pendingActions", "queue", "limit", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "recentHistory", "replaceFeed", "content", "(Ljava/util/List;Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setUserContentFavorite", "userId", "contentItemId", "favorite", "", "updatedAt", "", "(Ljava/lang/String;Ljava/lang/String;ZJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upsertCategory", "categories", "upsertContent", "upsertPendingActions", "actions", "upsertQueue", "items", "upsertUserContent", "userLibrary", "Companion", "app_debug"})
@javax.annotation.processing.Generated(value = {"androidx.room.RoomProcessor"})
@kotlin.Suppress(names = {"UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"})
public final class ContentDao_Impl implements com.linger.app.data.local.dao.ContentDao {
    @org.jetbrains.annotations.NotNull()
    private final androidx.room.RoomDatabase __db = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.room.EntityInsertAdapter<com.linger.app.data.local.entity.ContentEntity> __insertAdapterOfContentEntity = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.room.EntityInsertAdapter<com.linger.app.data.local.entity.CategoryEntity> __insertAdapterOfCategoryEntity = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.room.EntityInsertAdapter<com.linger.app.data.local.entity.QueueItemEntity> __insertAdapterOfQueueItemEntity = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.room.EntityInsertAdapter<com.linger.app.data.local.entity.PendingActionEntity> __insertAdapterOfPendingActionEntity = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.room.EntityInsertAdapter<com.linger.app.data.local.entity.DisplayHistoryEntity> __insertAdapterOfDisplayHistoryEntity = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.room.EntityInsertAdapter<com.linger.app.data.local.entity.UserContentEntity> __insertAdapterOfUserContentEntity = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.linger.app.data.local.dao.ContentDao_Impl.Companion Companion = null;
    
    public ContentDao_Impl(@org.jetbrains.annotations.NotNull()
    androidx.room.RoomDatabase __db) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object upsertContent(@org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.ContentEntity> content, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object upsertCategory(@org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.CategoryEntity> categories, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object upsertQueue(@org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.QueueItemEntity> items, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object upsertPendingActions(@org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.PendingActionEntity> actions, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object insertHistory(@org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.DisplayHistoryEntity> entries, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object upsertUserContent(@org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.UserContentEntity> items, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object replaceFeed(@org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.ContentEntity> content, @org.jetbrains.annotations.NotNull()
    java.util.List<com.linger.app.data.local.entity.QueueItemEntity> queue, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object allContent(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.linger.app.data.local.entity.ContentEntity>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object contentById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.linger.app.data.local.entity.ContentEntity> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object queue(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.linger.app.data.local.entity.QueueItemEntity>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object pendingActions(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.linger.app.data.local.entity.PendingActionEntity>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object recentHistory(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.linger.app.data.local.entity.DisplayHistoryEntity>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object userLibrary(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.linger.app.data.local.entity.UserContentEntity>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object clearQueue(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object markQueueItemDisplayed(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deletePendingAction(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object setUserContentFavorite(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    java.lang.String contentItemId, boolean favorite, long updatedAt, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\u00050\u0004\u00a8\u0006\u0006"}, d2 = {"Lcom/linger/app/data/local/dao/ContentDao_Impl$Companion;", "", "()V", "getRequiredConverters", "", "Lkotlin/reflect/KClass;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<kotlin.reflect.KClass<?>> getRequiredConverters() {
            return null;
        }
    }
}