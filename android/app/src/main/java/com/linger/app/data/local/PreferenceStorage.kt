package com.linger.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("linger_prefs")

class DataStoreManager(private val context: Context) {
    private val prefsFlow = context.dataStore.data

    private object Keys {
        val installationId = stringKey("installation_id")
        val refreshMinutes = intPreferencesKey("refresh_minutes")
        val isThemeDark = booleanPreferencesKey("theme_dark")
        val mixPref = stringKey("personal_system_mix")
        val accessToken = stringKey("access_token")
        val refreshToken = stringKey("refresh_token")
        val userId = stringKey("user_id")
        val forcedSlot = longKey("forced_slot")
        val lastDisplayedContentId = stringKey("last_displayed_content_id")
    }

    fun installationId(): Flow<String> = prefsFlow.map { it[Keys.installationId] ?: "" }
    fun refreshMinutes(): Flow<Int> = prefsFlow.map { it[Keys.refreshMinutes] ?: 30 }
    fun isThemeDark(): Flow<Boolean> = prefsFlow.map { it[Keys.isThemeDark] ?: false }
    fun personalSystemMix(): Flow<String> = prefsFlow.map { it[Keys.mixPref] ?: "BALANCED" }
    fun accessToken(): Flow<String> = prefsFlow.map { it[Keys.accessToken] ?: "" }
    fun refreshToken(): Flow<String> = prefsFlow.map { it[Keys.refreshToken] ?: "" }
    fun userId(): Flow<String> = prefsFlow.map { it[Keys.userId] ?: "" }
    fun forcedSlot(): Flow<Long> = prefsFlow.map { it[Keys.forcedSlot] ?: -1L }
    fun lastDisplayedContentId(): Flow<String> = prefsFlow.map { it[Keys.lastDisplayedContentId] ?: "" }

    suspend fun setInstallationId(value: String) {
        context.dataStore.edit { it[Keys.installationId] = value }
    }

    suspend fun setRefreshMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.refreshMinutes] = minutes }
    }

    suspend fun setThemeDark(isDark: Boolean) {
        context.dataStore.edit { it[Keys.isThemeDark] = isDark }
    }

    suspend fun setPersonalSystemMix(mix: String) {
        context.dataStore.edit { it[Keys.mixPref] = mix }
    }

    suspend fun setAuthSession(accessToken: String, refreshToken: String, userId: String) {
        context.dataStore.edit {
            it[Keys.accessToken] = accessToken
            it[Keys.refreshToken] = refreshToken
            it[Keys.userId] = userId
        }
    }

    suspend fun clearAuthSession() {
        context.dataStore.edit {
            it.remove(Keys.accessToken)
            it.remove(Keys.refreshToken)
            it.remove(Keys.userId)
        }
    }

    suspend fun setForcedSlot(slot: Long) {
        context.dataStore.edit {
            it[Keys.forcedSlot] = slot
        }
    }

    suspend fun clearForcedSlot() {
        context.dataStore.edit { it.remove(Keys.forcedSlot) }
    }

    suspend fun setLastDisplayedContentId(contentItemId: String) {
        context.dataStore.edit { it[Keys.lastDisplayedContentId] = contentItemId }
    }

    suspend fun blockingValue(block: (DataStoreManager) -> Unit = {}) {
        block(this)
    }

    suspend fun readAccessToken(): String = accessToken().firstOrNull() ?: ""
    suspend fun readRefreshToken(): String = refreshToken().firstOrNull() ?: ""
    suspend fun readUserId(): String = userId().firstOrNull() ?: ""
    suspend fun readForcedSlot(): Long = forcedSlot().firstOrNull() ?: -1L
}

private fun stringKey(name: String): Preferences.Key<String> = androidx.datastore.preferences.core.stringPreferencesKey(name)
private fun longKey(name: String): Preferences.Key<Long> = androidx.datastore.preferences.core.longPreferencesKey(name)
