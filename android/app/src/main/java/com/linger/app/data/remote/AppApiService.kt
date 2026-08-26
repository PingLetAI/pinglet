package com.linger.app.data.remote

import retrofit2.http.*

interface AppApiService {
    @POST("/api/v1/auth/anonymous")
    suspend fun anonymous(@Body request: AuthAnonymousRequest): AuthAnonymousResponse

    @POST("/api/v1/auth/refresh")
    suspend fun refresh(@Body request: AuthRefreshRequest): AuthRefreshResponse

    @POST("/api/v1/auth/email/request")
    suspend fun requestEmailOtp(@Body request: EmailOtpRequest): EmailOtpResponse

    @POST("/api/v1/auth/email/verify")
    suspend fun verifyEmailOtp(@Body request: EmailOtpVerifyRequest): EmailOtpVerifyResponse

    @POST("/api/v1/auth/logout")
    suspend fun logout(): Map<String, Boolean>

    @HTTP(method = "DELETE", path = "/api/v1/auth/account", hasBody = true)
    suspend fun deleteAccount(@Body request: DeleteAccountRequest): Map<String, Boolean>

    @GET("/api/v1/me")
    suspend fun me(): Map<String, Any>

    @GET("/api/v1/me/entitlements")
    suspend fun getEntitlements(): EntitlementResponse

    @POST("/api/v1/me/entitlements/trial")
    suspend fun startPlusTrial(): EntitlementResponse

    @POST("/api/v1/me/entitlements/google-play")
    suspend fun verifyGooglePlayPurchase(@Body request: GooglePlayPurchaseRequest): EntitlementResponse

    @GET("/api/v1/me/preferences")
    suspend fun getPreferences(): PreferenceResponse

    @PATCH("/api/v1/me/preferences")
    suspend fun patchPreferences(@Body body: Map<String, String>): PreferenceResponse

    @GET("/api/v1/me/feed")
    suspend fun getFeed(@Query("limit") limit: Int = 200): FeedResponse

    @GET("/api/v1/catalogs")
    suspend fun getCatalogs(): List<CatalogResponse>

    @GET("/api/v1/me/catalogs")
    suspend fun getCatalogPreferences(): List<CatalogResponse>

    @PATCH("/api/v1/me/catalogs/{id}")
    suspend fun patchCatalogPreference(@Path("id") id: String, @Body body: Map<String, Boolean>): CatalogPreferenceResponse

    @GET("/api/v1/catalogs/{id}/items")
    suspend fun getCatalogItems(@Path("id") id: String): List<Map<String, Any>>

    @GET("/api/v1/me/catalogs/{id}/items")
    suspend fun getMyCatalogDetail(@Path("id") id: String): CatalogDetailResponse

    @GET("/api/v1/me/content")
    suspend fun getMyContent(): List<UserContentResponse>

    @GET("/api/v1/me/content/{id}/detail")
    suspend fun getContentDetail(@Path("id") id: String): ContentDetailResponse

    @POST("/api/v1/me/content")
    suspend fun createContent(@Body body: Map<String, Any>): UserContentResponse

    @POST("/api/v1/me/ingestions")
    suspend fun createIngestion(@Body request: IngestUrlRequest): IngestUrlResponse

    @GET("/api/v1/me/ingestions/{id}")
    suspend fun getIngestion(@Path("id") id: String): IngestUrlResponse

    @GET("/api/v1/me/ingestions")
    suspend fun getIngestions(): List<IngestUrlResponse>

    @PATCH("/api/v1/me/content/{id}")
    suspend fun patchContent(@Path("id") id: String, @Body body: Map<String, Any>): UserContentResponse

    @DELETE("/api/v1/me/content/{id}")
    suspend fun deleteContent(@Path("id") id: String): Map<String, Boolean>

    @POST("/api/v1/me/content/{id}/favorite")
    suspend fun favorite(@Path("id") id: String): Map<String, Boolean>

    @DELETE("/api/v1/me/content/{id}/favorite")
    suspend fun unfavorite(@Path("id") id: String): Map<String, Boolean>

    @POST("/api/v1/events/batch")
    suspend fun postEventBatch(@Body body: EventBatchRequest): Map<String, Int>
}
