package com.linger.app.domain.model

enum class ContentType {
    QUOTE,
    REMINDER,
    AFFIRMATION,
    GOAL,
    MESSAGE,
    NOTE,
    PASSAGE,
}

enum class Visibility { SYSTEM, PRIVATE, COMMUNITY }

enum class ContentSource { PERSONAL, SYSTEM }

data class Category(
    val id: String,
    val slug: String,
    val name: String,
)

data class ContentItem(
    val id: String,
    val text: String,
    val type: ContentType,
    val author: String? = null,
    val categories: List<String> = emptyList(),
    val source: ContentSource,
    val favorite: Boolean = false,
    val updatedAt: String? = null,
)

data class WidgetState(
    val contentId: String,
    val text: String,
    val author: String?,
    val shownAt: Long,
    val nextChangeAt: Long,
)

