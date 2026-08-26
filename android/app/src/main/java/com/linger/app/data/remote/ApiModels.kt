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
    val sourceUrl: String? = null,
    val categories: List<String> = emptyList(),
    val catalogIds: List<String> = emptyList(),
    val source: ApiSource,
    val favorite: Boolean = false,
    val updatedAt: String? = null,
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

data class AuthRefreshRequest(
    val refreshToken: String,
)

data class AuthRefreshResponse(
    val accessToken: String,
    val expiresIn: Int,
    val tokenType: String,
)

data class ContentListResponse(val items: List<UserContentResponse>?)

data class UserContentResponse(
    val id: String,
    val contentItemId: String,
    val favorite: Boolean,
    val archived: Boolean,
    val contentItem: FeedItemDto,
)

data class IngestUrlRequest(
    val url: String,
    val contextText: String? = null,
)

data class IngestUrlResponse(
    val id: String,
    val status: String,
    val processingStage: String? = null,
    val caption: String? = null,
    val transcript: String? = null,
    val ocrText: String? = null,
    val takeaways: List<DerivedTakeawayDto>? = null,
    val extractionConfidence: Double? = null,
    val moderationStatus: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val contentItem: IngestedContentDto? = null,
)

data class DerivedTakeawayDto(
    val text: String,
    val type: String,
    val confidence: Double,
)

data class IngestedContentDto(
    val id: String,
    val text: String,
    val type: ContentType,
    val author: String?,
    val sourceUrl: String?,
    val sourcePlatform: String?,
)

data class ContentDetailResponse(
    val content: ContentDetailItemDto,
    val overview: String? = null,
    val insights: List<ContentInsightDto> = emptyList(),
    val comprehensiveSummary: String? = null,
    val actions: List<String> = emptyList(),
    val themes: List<String> = emptyList(),
    val takeaways: List<DerivedTakeawayDto> = emptyList(),
    val transcript: String? = null,
    val visibleText: String? = null,
    val caption: String? = null,
    val access: ContentDetailAccessDto,
)

data class ContentDetailItemDto(
    val id: String,
    val text: String,
    val type: ContentType,
    val author: String? = null,
    val sourceUrl: String? = null,
    val sourcePlatform: String? = null,
    val favorite: Boolean = false,
    val categories: List<String> = emptyList(),
)

data class ContentInsightDto(val title: String, val explanation: String, val evidence: String)

data class ContentDetailAccessDto(
    val plan: String,
    val hasAnalysis: Boolean,
    val fullDetailsUnlocked: Boolean,
    val lockedSections: List<String> = emptyList(),
    val isAnonymous: Boolean = true,
    val entitlementSource: String = "NONE",
    val accessExpiresAt: String? = null,
    val trialStatus: String = "INELIGIBLE",
    val trialEligible: Boolean = false,
    val trialEndsAt: String? = null,
    val trialDaysRemaining: Int = 0,
    val paidPlansEnabled: Boolean = false,
)

data class PreferenceResponse(
    val refreshMinutes: Int,
    val personalSystemMix: String,
    val theme: String,
)

data class TermsStatusResponse(
    val currentVersion: String,
    val accepted: Boolean,
    val acceptedAt: String? = null,
)

data class CatalogResponse(
    val id: String,
    val slug: String,
    val name: String,
    val description: String? = null,
    val enabled: Boolean = true,
    val itemCount: Int = 0,
    val previewItems: List<CatalogItemResponse> = emptyList(),
)

data class CatalogItemResponse(
    val id: String,
    val text: String,
    val type: String,
    val author: String? = null,
    val sourceUrl: String? = null,
)

data class CatalogDetailResponse(
    val id: String,
    val slug: String,
    val name: String,
    val description: String? = null,
    val enabled: Boolean,
    val itemCount: Int,
    val items: List<CatalogItemResponse>,
)

data class CatalogPreferenceResponse(val catalogId: String, val enabled: Boolean)

data class ExploreActionResponse(
    val success: Boolean,
    val hiddenContentIds: List<String> = emptyList(),
)


data class EventPayload(
    val type: String,
    @SerializedName("contentItemId") val contentItemId: String? = null,
    val surface: String,
    val timestamp: String,
    val metadata: String? = null,
)


data class EventBatchRequest(val events: List<EventPayload>)

data class EntitlementResponse(
    val plan: String,
    val isAnonymous: Boolean,
    val email: String? = null,
    val saveCount: Int,
    val saveLimit: Int? = null,
    val socialImportsUsed: Int,
    val socialImportLimit: Int,
    val accountPromptRecommended: Boolean,
    val plusExpiresAt: String? = null,
    val entitlementSource: String = "NONE",
    val accessExpiresAt: String? = null,
    val trialStatus: String = "INELIGIBLE",
    val trialEligible: Boolean = false,
    val trialStartedAt: String? = null,
    val trialEndsAt: String? = null,
    val trialDaysRemaining: Int = 0,
    val paidPlansEnabled: Boolean = false,
)

data class EmailOtpRequest(val email: String)
data class EmailOtpVerifyRequest(val email: String, val code: String)
data class EmailOtpResponse(
    val sent: Boolean,
    val expiresInSeconds: Int,
    val devCode: String? = null,
)
data class EmailOtpVerifyResponse(
    val verified: Boolean,
    val email: String,
    val plan: String,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val userId: String? = null,
)
data class DeleteAccountRequest(val email: String, val code: String)
data class GooglePlayPurchaseRequest(val purchaseToken: String, val productId: String)
