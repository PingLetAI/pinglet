package com.linger.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content")
data class ContentEntity(
    @PrimaryKey val id: String,
    val text: String,
    val type: String,
    val author: String? = null,
    @ColumnInfo(name = "source") val source: String,
    val sourceUrl: String? = null,
    val visibility: String,
    val ownerUserId: String? = null,
    val status: String,
    val language: String,
)
