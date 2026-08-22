package com.linger.app.data.local.entity

import androidx.room.Entity

@Entity(primaryKeys = ["contentItemId", "categoryId"], tableName = "content_categories")
data class ContentCategoryEntity(
    val contentItemId: String,
    val categoryId: String,
)
