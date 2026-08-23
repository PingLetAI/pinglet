package com.linger.app.data.repository

import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.AuthAnonymousRequest
import com.linger.app.data.remote.AuthAnonymousResponse
import com.linger.app.data.remote.AuthRefreshRequest
import com.linger.app.data.remote.AuthRefreshResponse

interface AuthRepository {
    suspend fun anonymous(installationId: String): AuthAnonymousResponse
    suspend fun refresh(refreshToken: String): AuthRefreshResponse
}

class AuthRepositoryImpl(private val api: AppApiService) : AuthRepository {
    override suspend fun anonymous(installationId: String): AuthAnonymousResponse {
        return api.anonymous(AuthAnonymousRequest(installationId = installationId))
    }

    override suspend fun refresh(refreshToken: String): AuthRefreshResponse {
        return api.refresh(AuthRefreshRequest(refreshToken = refreshToken))
    }
}
