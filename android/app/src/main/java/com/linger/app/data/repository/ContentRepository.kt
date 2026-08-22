package com.linger.app.data.repository

import com.linger.app.data.local.dao.ContentDao
import com.linger.app.data.local.entity.ContentEntity
import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.mapper.toDomain
import com.linger.app.domain.model.ContentItem
import com.linger.app.domain.model.ContentSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class ContentRepository(
    private val api: AppApiService,
    private val dao: ContentDao,
) {

    suspend fun syncFeed(limit: Int = 200): List<ContentItem> = withContext(Dispatchers.IO) {
        val feed = api.getFeed(limit)
        val domains = feed.items.map { it.toDomain() }
        dao.upsertContent(domains.map { it.toEntity(it.source) })
        domains
    }

    fun toEntity(item: ContentItem) = ContentEntity(
        id = item.id,
        text = item.text,
        type = item.type.name,
        author = item.author,
        source = item.source.name,
        sourceUrl = null,
        visibility = "SYSTEM",
        ownerUserId = null,
        status = "ACTIVE",
        language = "en",
    )

    suspend fun saveContent(text: String, type: String, categorySlug: String?): String {
        val body = mapOf("text" to text, "type" to type, "categories" to listOfNotNull(categorySlug))
        api.createContent(body)
        return UUID.randomUUID().toString()
    }
}
