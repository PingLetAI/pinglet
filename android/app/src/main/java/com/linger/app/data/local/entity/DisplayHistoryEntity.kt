package com.linger.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "display_history")
data class DisplayHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contentItemId: String,
    val shownAt: Long,
    val surface: String,
    val reaction: String,
    val synced: Boolean = false,
)
