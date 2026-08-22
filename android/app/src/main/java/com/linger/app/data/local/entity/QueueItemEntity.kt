package com.linger.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queue_items")
data class QueueItemEntity(
    @PrimaryKey val id: String,
    val contentItemId: String,
    val slotIndex: Int,
    val source: String,
    val validFrom: Long? = null,
    val validUntil: Long? = null,
    val displayed: Boolean = false,
)
