package com.linger.app.data.remote.mapper

import com.linger.app.data.remote.FeedItemDto
import com.linger.app.domain.model.ContentItem
import com.linger.app.domain.model.ContentSource

fun FeedItemDto.toDomain(): ContentItem = ContentItem(
    id = id,
    text = text,
    type = type,
    author = author,
    sourceUrl = sourceUrl,
    categories = categories,
    source = if (source.name == "SYSTEM") ContentSource.SYSTEM else ContentSource.PERSONAL,
    favorite = favorite,
    updatedAt = updatedAt,
)
