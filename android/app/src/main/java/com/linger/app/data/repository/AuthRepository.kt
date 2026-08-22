package com.linger.app.data.repository

import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.AuthAnonymousRequest
import com.linger.app.data.remote.AuthAnonymousResponse

interface AuthRepository {
    suspend fun anonymous(installationId: String): AuthAnonymousResponse
}

class AuthRepositoryImpl(private val api: AppApiService) : AuthRepository {
    override suspend fun anonymous(installationId: String): AuthAnonymousResponse {
        return api.anonymous(AuthAnonymousRequest(installationId = installationId))
    }
}
