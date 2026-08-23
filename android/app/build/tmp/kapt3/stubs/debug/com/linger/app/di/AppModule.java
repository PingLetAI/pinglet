package com.linger.app.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0004H\u0007J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0007J\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u000e\u001a\u00020\tH\u0007J\u0012\u0010\u000f\u001a\u00020\u00102\b\b\u0001\u0010\u0011\u001a\u00020\u0012H\u0007J\u0012\u0010\u0013\u001a\u00020\u000b2\b\b\u0001\u0010\u0011\u001a\u00020\u0012H\u0007J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u000e\u001a\u00020\tH\u0007\u00a8\u0006\u0016"}, d2 = {"Lcom/linger/app/di/AppModule;", "", "()V", "provideApi", "Lcom/linger/app/data/remote/AppApiService;", "provideAuthRepository", "Lcom/linger/app/data/repository/AuthRepository;", "api", "provideContentDao", "Lcom/linger/app/data/local/dao/ContentDao;", "db", "Lcom/linger/app/data/local/db/WidgetDatabase;", "provideContentRepository", "Lcom/linger/app/data/repository/ContentRepository;", "dao", "provideDataStore", "Lcom/linger/app/data/local/DataStoreManager;", "context", "Landroid/content/Context;", "provideDatabase", "provideFeedRepository", "Lcom/linger/app/data/repository/FeedRepository;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class AppModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.linger.app.di.AppModule INSTANCE = null;
    
    private AppModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.linger.app.data.remote.AppApiService provideApi() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.linger.app.data.local.db.WidgetDatabase provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.linger.app.data.local.dao.ContentDao provideContentDao(@org.jetbrains.annotations.NotNull()
    com.linger.app.data.local.db.WidgetDatabase db) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.linger.app.data.repository.FeedRepository provideFeedRepository(@org.jetbrains.annotations.NotNull()
    com.linger.app.data.local.dao.ContentDao dao) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.linger.app.data.repository.AuthRepository provideAuthRepository(@org.jetbrains.annotations.NotNull()
    com.linger.app.data.remote.AppApiService api) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.linger.app.data.repository.ContentRepository provideContentRepository(@org.jetbrains.annotations.NotNull()
    com.linger.app.data.remote.AppApiService api, @org.jetbrains.annotations.NotNull()
    com.linger.app.data.local.dao.ContentDao dao) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.linger.app.data.local.DataStoreManager provideDataStore(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
}