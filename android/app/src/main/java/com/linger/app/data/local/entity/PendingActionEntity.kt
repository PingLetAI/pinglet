package com.linger.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_actions")
data class PendingActionEntity(
    @PrimaryKey val id: String,
    val actionType: String,
    val payload: String,
    val attempts: Int = 0,
    val createdAt: Long,
)
