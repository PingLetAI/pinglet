package com.linger.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_content")
data class UserContentEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val contentItemId: String,
    val favorite: Boolean,
    val archived: Boolean,
    val priority: Float,
    val createdAt: Long,
    val updatedAt: Long,
)
