package com.linger.app.data.repository

import android.content.Context
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.local.db.DatabaseProvider
import com.linger.app.data.remote.RetrofitClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.UUID
import org.json.JSONObject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataStore: DataStoreManager,
    @ApplicationContext private val context: Context,
) {
    private companion object { val sessionMutex = Mutex() }
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

    suspend fun startAnonymousSession() {
        RetrofitClient.setAuthToken(null)
        ensureSession()
    }

    private suspend fun ensureSession(forceRefresh: Boolean = false) = sessionMutex.withLock {
        establishSession(forceRefresh)
    }

    private suspend fun establishSession(forceRefresh: Boolean) {
        if (!forceRefresh) {
            dataStore.readAccessToken().takeIf { it.isNotBlank() }?.let {
                RetrofitClient.setAuthToken(it)
                return
            }
        }
        val refreshToken = dataStore.readRefreshToken()
        if (refreshToken.isNotBlank()) {
            try {
                val refreshed = authRepository.refresh(refreshToken)
                dataStore.setAuthSession(refreshed.accessToken, refreshToken, dataStore.readUserId())
                RetrofitClient.setAuthToken(refreshed.accessToken)
                return
            } catch (error: HttpException) {
                // Transient failures must not discard a valid account session.
                if (error.code() != 400 && error.code() != 401) throw error
            }
            dataStore.clearAuthSession()
        }
        val installationId = dataStore.readInstallationId().ifBlank {
            UUID.randomUUID().toString().also { dataStore.setInstallationId(it) }
        }
        val auth = try {
            authRepository.anonymous(installationId)
        } catch (error: HttpException) {
            val body = error.response()?.errorBody()?.string()
            val code = runCatching { JSONObject(body.orEmpty()).optString("code") }.getOrNull()
            if (error.code() != 409 || code != "INSTALLATION_ID_IN_USE") throw error
            val replacementId = UUID.randomUUID().toString()
            dataStore.setInstallationId(replacementId)
            authRepository.anonymous(replacementId)
        }
        withContext(Dispatchers.IO) { DatabaseProvider.database(context).clearAllTables() }
        dataStore.clearAccountData()
        dataStore.setAuthSession(auth.accessToken, auth.refreshToken, auth.userId)
        RetrofitClient.setAuthToken(auth.accessToken)
    }
}
