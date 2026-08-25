package com.linger.app.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE content ADD COLUMN categoriesCsv TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE content ADD COLUMN catalogIdsCsv TEXT NOT NULL DEFAULT ''")
    }
}
