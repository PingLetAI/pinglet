package com.linger.app.di

import android.content.Context
import androidx.room.Room
import com.linger.app.data.local.dao.ContentDao
import com.linger.app.data.local.db.WidgetDatabase
import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.ApiConfig
import com.linger.app.data.remote.RetrofitClient
import com.linger.app.data.repository.AuthRepository
import com.linger.app.data.repository.AuthRepositoryImpl
import com.linger.app.data.repository.ContentRepository
import com.linger.app.data.repository.FeedRepository
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.local.db.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApi(): AppApiService = RetrofitClient.build(ApiConfig.apiBaseUrl())

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WidgetDatabase {
        return Room.databaseBuilder(context, WidgetDatabase::class.java, "linger-db")
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideContentDao(db: WidgetDatabase): ContentDao = db.contentDao()

    @Provides
    @Singleton
    fun provideFeedRepository(dao: ContentDao): FeedRepository = FeedRepository(dao)

    @Provides
    @Singleton
    fun provideAuthRepository(api: AppApiService): AuthRepository = AuthRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideContentRepository(api: AppApiService, dao: ContentDao) = ContentRepository(api, dao)

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStoreManager = DataStoreManager(context)
}
