package com.linger.app.widget

import com.linger.app.data.local.dao.ContentDao
import com.linger.app.data.local.entity.ContentEntity
import java.time.LocalDateTime
import kotlin.math.absoluteValue

object WidgetRotationSelector {
    suspend fun select(
        dao: ContentDao,
        profile: WidgetProfile,
        widgetKey: String,
        favoriteIds: Set<String>,
        now: LocalDateTime = LocalDateTime.now(),
    ): ContentEntity? {
        val queued = dao.queue(500).mapNotNull { dao.contentById(it.contentItemId) }
        if (queued.isEmpty()) return null

        val byMode = queued.filter { item ->
            val catalogs = item.catalogIdsCsv.csvSet()
            when (profile.contentMode) {
                "PERSONAL" -> item.source == "PERSONAL"
                "COLLECTIONS" -> item.source == "SYSTEM" && (profile.catalogIds.isEmpty() || catalogs.any(profile.catalogIds::contains))
                else -> profile.catalogIds.isEmpty() || item.source == "PERSONAL" || catalogs.any(profile.catalogIds::contains)
            }
        }.ifEmpty { queued }

        val contextualTerms = contextualTerms(profile.scheduleMode, now)
        val contextual = if (contextualTerms.isEmpty()) byMode else byMode.filter { item ->
            val searchable = "${item.categoriesCsv} ${item.author.orEmpty()} ${item.text}".lowercase()
            contextualTerms.any(searchable::contains)
        }.ifEmpty { byMode }

        val ranked = contextual.sortedByDescending { item ->
            (if (item.id in favoriteIds) 8 else 0) +
                (if (profile.contentMode != "COLLECTIONS" && item.source == "PERSONAL") 4 else 0) +
                contextualTerms.count("${item.categoriesCsv} ${item.text}".lowercase()::contains)
        }
        val withoutCurrent = ranked.filterNot { it.id == profile.currentContentId }.ifEmpty { ranked }
        val seed = (widgetKey.hashCode().toLong() + profile.manualOffset + now.toLocalDate().toEpochDay()).absoluteValue
        return withoutCurrent[(seed % withoutCurrent.size).toInt()]
    }

    private fun contextualTerms(mode: String, now: LocalDateTime): List<String> = when (mode) {
        "DAY_RHYTHM" -> when (now.hour) {
            in 5..11 -> listOf("morning", "motivation", "discipline", "drive", "focus")
            in 18..23 -> listOf("reflection", "calm", "gratitude", "affirmation", "faith")
            else -> emptyList()
        }
        "CONTEXTUAL" -> when {
            now.dayOfWeek.value >= 6 -> listOf("life", "family", "calm", "fitness", "reflection")
            now.hour in 8..17 -> listOf("business", "focus", "discipline", "confidence", "goal")
            else -> listOf("reflection", "affirmation", "gratitude", "calm")
        }
        else -> emptyList()
    }

    private fun String.csvSet() = split(',').filter(String::isNotBlank).toSet()
}
