package com.linger.app.data.repository

import com.linger.app.data.local.dao.ContentDao
import com.linger.app.data.local.entity.QueueItemEntity
import com.linger.app.data.local.mapper.toEntity
import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.IngestUrlRequest
import com.linger.app.data.remote.IngestedContentDto
import com.linger.app.data.remote.mapper.toDomain
import com.linger.app.domain.model.ContentItem
import com.linger.app.domain.model.ContentSource
import com.linger.app.domain.model.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class ContentRepository(
    private val api: AppApiService,
    private val dao: ContentDao,
) {
    private val seededItems = listOf(
        ContentItem(
            id = "seed-1",
            text = "Keep what matters in mind. Your next thought is one tap away.",
            type = ContentType.QUOTE,
            author = "PingLet",
            categories = emptyList(),
            source = ContentSource.SYSTEM,
        ),
        ContentItem(
            id = "seed-2",
            text = "A small return keeps momentum. Your message will live here for a while.",
            type = ContentType.QUOTE,
            author = "PingLet",
            categories = emptyList(),
            source = ContentSource.SYSTEM,
        ),
        ContentItem(
            id = "seed-3",
            text = "When attention is scarce, consistency matters more than perfection.",
            type = ContentType.AFFIRMATION,
            author = "PingLet",
            categories = emptyList(),
            source = ContentSource.SYSTEM,
        ),
    )

    suspend fun syncFeed(limit: Int = 200): List<ContentItem> = withContext(Dispatchers.IO) {
        val feed = api.getFeed(limit)
        val domains = feed.items.map { it.toDomain() }
        val queue = domains.mapIndexed { index, item ->
                QueueItemEntity(
                    id = UUID.randomUUID().toString(),
                    contentItemId = item.id,
                    slotIndex = index,
                    source = item.source.name,
                    validFrom = null,
                    validUntil = null,
                    displayed = false,
                )
            }
        dao.replaceFeed(domains.map { it.toEntity(it.source) }, queue)
        domains
    }

    suspend fun seedBootstrapItemsIfQueueEmpty(): List<ContentItem> = withContext(Dispatchers.IO) {
        val queue = dao.queue(200)
        if (queue.isNotEmpty()) return@withContext emptyList()

        dao.upsertContent(seededItems.map { it.toEntity(it.source) })
        dao.upsertQueue(
            seededItems.mapIndexed { index, item ->
                QueueItemEntity(
                    id = UUID.randomUUID().toString(),
                    contentItemId = item.id,
                    slotIndex = index,
                    source = item.source.name,
                    validFrom = null,
                    validUntil = null,
                    displayed = false,
                )
            },
        )

        seededItems
    }

    suspend fun saveContent(text: String, type: String, categorySlug: String?): String {
        val body = mapOf("text" to text, "type" to type, "categories" to listOfNotNull(categorySlug))
        api.createContent(body)
        return UUID.randomUUID().toString()
    }

    suspend fun enqueueUrl(
        url: String,
        contextText: String?,
    ) = withContext(Dispatchers.IO) { api.createIngestion(IngestUrlRequest(url, contextText)) }
}
