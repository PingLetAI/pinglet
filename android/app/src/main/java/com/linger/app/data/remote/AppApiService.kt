package com.linger.app.data.remote

import retrofit2.Response
import retrofit2.http.*

interface AppApiService {
    @POST("/api/v1/auth/anonymous")
    suspend fun anonymous(@Body request: AuthAnonymousRequest): AuthAnonymousResponse

    @GET("/api/v1/me")
    suspend fun me(): Map<String, Any>

    @GET("/api/v1/me/preferences")
    suspend fun getPreferences(): PreferenceResponse

    @PATCH("/api/v1/me/preferences")
    suspend fun patchPreferences(@Body body: Map<String, String>): PreferenceResponse

    @GET("/api/v1/me/feed")
    suspend fun getFeed(@Query("limit") limit: Int = 200): FeedResponse

    @GET("/api/v1/catalogs")
    suspend fun getCatalogs(): List<Map<String, Any>>

    @GET("/api/v1/catalogs/{id}/items")
    suspend fun getCatalogItems(@Path("id") id: String): List<Map<String, Any>>

    @GET("/api/v1/me/content")
    suspend fun getMyContent(): List<UserContentResponse>

    @POST("/api/v1/me/content")
    suspend fun createContent(@Body body: Map<String, Any>): UserContentResponse

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
