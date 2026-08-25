package com.linger.app.data.local.db

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private const val DB_NAME = "linger-db"

    @Volatile
    private var instance: WidgetDatabase? = null

    fun database(context: Context): WidgetDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                WidgetDatabase::class.java,
                DB_NAME,
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }
    }
}
