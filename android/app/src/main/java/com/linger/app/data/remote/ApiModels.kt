package com.linger.app.data.remote

import com.google.gson.annotations.SerializedName
import com.linger.app.domain.model.ContentType

enum class ApiSource {
    PERSONAL,
    SYSTEM,
}

data class FeedResponse(
    val items: List<FeedItemDto>,
)

data class FeedItemDto(
    val id: String,
    val text: String,
    val type: ContentType,
    val author: String?,
    val categories: List<String> = emptyList(),
    val source: ApiSource,
)

data class AuthAnonymousRequest(
    val installationId: String,
    val platform: String = "ANDROID",
    val timezone: String = "UTC",
    val locale: String = "en",
    val appVersion: String = "0.1.0",
)

data class AuthAnonymousResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
)

data class ContentListResponse(val items: List<UserContentResponse>?)

data class UserContentResponse(
    val id: String,
    val contentItemId: String,
    val favorite: Boolean,
    val archived: Boolean,
    val contentItem: FeedItemDto,
)

data class PreferenceResponse(
    val refreshMinutes: Int,
    val personalSystemMix: String,
    val theme: String,
)


data class EventPayload(
    val type: String,
    @SerializedName("contentItemId") val contentItemId: String? = null,
    val surface: String,
    val timestamp: String,
)


data class EventBatchRequest(val events: List<EventPayload>)
