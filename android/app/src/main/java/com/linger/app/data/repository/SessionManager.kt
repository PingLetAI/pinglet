package com.linger.app.data.repository

import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.remote.RetrofitClient
import retrofit2.HttpException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataStore: DataStoreManager,
) {
    suspend fun <T> withAuthRetry(block: suspend () -> T): T {
        ensureSession()
        return try {
            block()
        } catch (error: HttpException) {
            if (error.code() != 401) throw error
            ensureSession(forceRefresh = true)
            block()
        }
    }

    private suspend fun ensureSession(forceRefresh: Boolean = false) {
        if (!forceRefresh) {
            dataStore.readAccessToken().takeIf { it.isNotBlank() }?.let {
                RetrofitClient.setAuthToken(it)
                return
            }
        }
        val refreshToken = dataStore.readRefreshToken()
        if (refreshToken.isNotBlank()) {
            runCatching { authRepository.refresh(refreshToken) }.getOrNull()?.let { refreshed ->
                dataStore.setAuthSession(refreshed.accessToken, refreshToken, dataStore.readUserId())
                RetrofitClient.setAuthToken(refreshed.accessToken)
                return
            }
            dataStore.clearAuthSession()
        }
        val installationId = dataStore.readInstallationId().ifBlank {
            UUID.randomUUID().toString().also { dataStore.setInstallationId(it) }
        }
        val auth = authRepository.anonymous(installationId)
        dataStore.setAuthSession(auth.accessToken, auth.refreshToken, auth.userId)
        RetrofitClient.setAuthToken(auth.accessToken)
    }
}
