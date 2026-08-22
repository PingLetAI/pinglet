package com.linger.app.data.repository

import com.linger.app.data.local.dao.ContentDao
import com.linger.app.data.local.entity.QueueItemEntity
import com.linger.app.domain.model.ContentItem
import com.linger.app.domain.model.ContentSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class FeedRepository(private val dao: ContentDao) {
    suspend fun selectNextItem(slotIndex: Int): ContentItem? = withContext(Dispatchers.IO) {
        val queue = dao.queue(200)
        if (queue.isEmpty()) return@withContext null

        val candidate = queue.firstOrNull { it.slotIndex == slotIndex } ?: queue.first()
        dao.markQueueItemDisplayed(candidate.id)

        ContentItem(
            id = candidate.contentItemId,
            text = "placeholder",
            type = com.linger.app.domain.model.ContentType.QUOTE,
            author = null,
            categories = emptyList(),
            source = if (candidate.source == "SYSTEM") ContentSource.SYSTEM else ContentSource.PERSONAL,
        )
    }

    suspend fun addQueueItem(contentItemId: String, slotIndex: Int, source: String) = withContext(Dispatchers.IO) {
        dao.upsertQueue(
            listOf(
                QueueItemEntity(
                    id = UUID.randomUUID().toString(),
                    contentItemId = contentItemId,
                    slotIndex = slotIndex,
                    source = source,
                    validFrom = null,
                    validUntil = null,
                    displayed = false,
                )
            )
        )
    }
}
