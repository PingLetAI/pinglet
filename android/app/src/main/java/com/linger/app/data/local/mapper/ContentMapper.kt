package com.linger.app.data.local.mapper

import com.linger.app.data.local.entity.ContentEntity
import com.linger.app.domain.model.ContentItem
import com.linger.app.domain.model.ContentSource
import com.linger.app.domain.model.ContentType

fun ContentEntity.toDomain(contentType: String = this.type): ContentItem = ContentItem(
    id = id,
    text = text,
    type = try {
        ContentType.valueOf(contentType)
    } catch (_: Exception) {
        ContentType.QUOTE
    },
    author = author,
    sourceUrl = sourceUrl,
    categories = categoriesCsv.split(',').filter(String::isNotBlank),
    catalogIds = catalogIdsCsv.split(',').filter(String::isNotBlank),
    source = if (source == "SYSTEM") ContentSource.SYSTEM else ContentSource.PERSONAL,
)

fun ContentItem.toEntity(source: ContentSource): ContentEntity = ContentEntity(
    id = id,
    text = text,
    type = type.name,
    author = author,
    source = source.name,
    visibility = "SYSTEM",
    status = "ACTIVE",
    language = "en",
    sourceUrl = sourceUrl,
    ownerUserId = null,
    categoriesCsv = categories.joinToString(","),
    catalogIdsCsv = catalogIds.joinToString(","),
)
