package com.linger.app.sync;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0082@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u0011\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\rH\u0082@\u00a2\u0006\u0002\u0010\u0012J\u001e\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\f\u001a\u00020\rH\u0082@\u00a2\u0006\u0002\u0010\u0015J\u0016\u0010\u0016\u001a\u00020\u000b2\u0006\u0010\u0017\u001a\u00020\u0018H\u0086@\u00a2\u0006\u0002\u0010\u0019J\u000e\u0010\u001a\u001a\u00020\u000b2\u0006\u0010\u0017\u001a\u00020\u0018J\u0010\u0010\u001b\u001a\u00020\u000b2\u0006\u0010\u0017\u001a\u00020\u0018H\u0002J\u000e\u0010\u001c\u001a\u00020\u000b2\u0006\u0010\u0017\u001a\u00020\u0018J\u0016\u0010\u001d\u001a\u00020\u000b2\u0006\u0010\u0017\u001a\u00020\u0018H\u0086@\u00a2\u0006\u0002\u0010\u0019J&\u0010\u001e\u001a\u00020\u000b2\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\f\u001a\u00020\rH\u0082@\u00a2\u0006\u0002\u0010!R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/linger/app/sync/SyncScheduler;", "", "()V", "DEFAULT_MINUTES", "", "FORCE_REFRESH_WORK_TAG", "", "PERIODIC_WORK_TAG", "syncMutex", "Lkotlinx/coroutines/sync/Mutex;", "establishSession", "", "dataStore", "Lcom/linger/app/data/local/DataStoreManager;", "authRepository", "Lcom/linger/app/data/repository/AuthRepositoryImpl;", "(Lcom/linger/app/data/local/DataStoreManager;Lcom/linger/app/data/repository/AuthRepositoryImpl;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "readOrCreateInstallationId", "(Lcom/linger/app/data/local/DataStoreManager;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "refreshOrReAuthenticate", "", "(Lcom/linger/app/data/repository/AuthRepositoryImpl;Lcom/linger/app/data/local/DataStoreManager;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "refreshWidget", "context", "Landroid/content/Context;", "(Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "scheduleInitialSync", "schedulePeriodicSync", "scheduleWidgetRefresh", "syncAndRefresh", "syncFeedWithAuthRecovery", "contentRepository", "Lcom/linger/app/data/repository/ContentRepository;", "(Lcom/linger/app/data/repository/ContentRepository;Lcom/linger/app/data/repository/AuthRepositoryImpl;Lcom/linger/app/data/local/DataStoreManager;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class SyncScheduler {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PERIODIC_WORK_TAG = "linger-sync";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String FORCE_REFRESH_WORK_TAG = "linger-force-refresh";
    private static final long DEFAULT_MINUTES = 30L;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.sync.Mutex syncMutex = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.linger.app.sync.SyncScheduler INSTANCE = null;
    
    private SyncScheduler() {
        super();
    }
    
    public final void scheduleInitialSync(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    private final void schedulePeriodicSync(android.content.Context context) {
    }
    
    public final void scheduleWidgetRefresh(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object syncAndRefresh(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object establishSession(com.linger.app.data.local.DataStoreManager dataStore, com.linger.app.data.repository.AuthRepositoryImpl authRepository, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object syncFeedWithAuthRecovery(com.linger.app.data.repository.ContentRepository contentRepository, com.linger.app.data.repository.AuthRepositoryImpl authRepository, com.linger.app.data.local.DataStoreManager dataStore, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object refreshOrReAuthenticate(com.linger.app.data.repository.AuthRepositoryImpl authRepository, com.linger.app.data.local.DataStoreManager dataStore, kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    private final java.lang.Object readOrCreateInstallationId(com.linger.app.data.local.DataStoreManager dataStore, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object refreshWidget(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}