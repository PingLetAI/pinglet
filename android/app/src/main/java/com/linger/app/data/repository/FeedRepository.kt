package com.linger.app.data.repository

import com.linger.app.data.local.dao.ContentDao
import com.linger.app.data.local.entity.ContentEntity
import com.linger.app.data.local.entity.QueueItemEntity
import com.linger.app.domain.model.ContentItem
import com.linger.app.domain.model.ContentType
import com.linger.app.domain.model.ContentSource
import com.linger.app.domain.usecase.SlotCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class FeedRepository(private val dao: ContentDao) {
    suspend fun selectNextItem(slotAtMillis: Long, intervalMinutes: Int = 30): ContentItem? = withContext(Dispatchers.IO) {
        val queue = dao.queue(200)
        if (queue.isEmpty()) return@withContext null

        val normalizedSlot = (SlotCalculator.slotForMillis(slotAtMillis, intervalMinutes) % queue.size.toLong()).toInt()
        val candidate = queue.firstOrNull { it.slotIndex == normalizedSlot } ?: queue.firstOrNull { !it.displayed } ?: queue.first()
        dao.markQueueItemDisplayed(candidate.id)

        val content = dao.contentById(candidate.contentItemId) ?: return@withContext null
        content.toDomainFromEntity()
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

    private fun ContentEntity.toDomainFromEntity(): ContentItem = ContentItem(
        id = id,
        text = text,
        type = try { ContentType.valueOf(type) } catch (_: Exception) { ContentType.QUOTE },
        author = author,
        categories = emptyList(),
        source = if (source == "SYSTEM") ContentSource.SYSTEM else ContentSource.PERSONAL,
    )
}
