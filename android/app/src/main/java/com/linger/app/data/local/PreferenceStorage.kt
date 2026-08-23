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
        val lastDisplayedContentText = stringKey("last_displayed_content_text")
        val lastDisplayedContentAuthor = stringKey("last_displayed_content_author")
        val lastDisplayedShownAt = longKey("last_displayed_shown_at")
        val lastDisplayedNextChangeAt = longKey("last_displayed_next_change_at")
        val lastDisplayedFavorite = booleanPreferencesKey("last_displayed_favorite")
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
    fun lastDisplayedContentText(): Flow<String> = prefsFlow.map { it[Keys.lastDisplayedContentText] ?: "" }
    fun lastDisplayedContentAuthor(): Flow<String> = prefsFlow.map { it[Keys.lastDisplayedContentAuthor] ?: "" }
    fun lastDisplayedShownAt(): Flow<Long> = prefsFlow.map { it[Keys.lastDisplayedShownAt] ?: 0L }
    fun lastDisplayedNextChangeAt(): Flow<Long> = prefsFlow.map { it[Keys.lastDisplayedNextChangeAt] ?: 0L }
    fun lastDisplayedFavorite(): Flow<Boolean> = prefsFlow.map { it[Keys.lastDisplayedFavorite] ?: false }

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

    suspend fun setLastDisplayedContentText(value: String) {
        context.dataStore.edit { it[Keys.lastDisplayedContentText] = value }
    }

    suspend fun setLastDisplayedContentAuthor(value: String?) {
        context.dataStore.edit { prefs ->
            if (value.isNullOrBlank()) {
                prefs.remove(Keys.lastDisplayedContentAuthor)
            } else {
                prefs[Keys.lastDisplayedContentAuthor] = value
            }
        }
    }

    suspend fun setLastDisplayedShownAt(value: Long) {
        context.dataStore.edit { it[Keys.lastDisplayedShownAt] = value }
    }

    suspend fun setLastDisplayedNextChangeAt(value: Long) {
        context.dataStore.edit { it[Keys.lastDisplayedNextChangeAt] = value }
    }

    suspend fun blockingValue(block: (DataStoreManager) -> Unit = {}) {
        block(this)
    }

    suspend fun readInstallationId(): String = installationId().firstOrNull() ?: ""
    suspend fun readAccessToken(): String = accessToken().firstOrNull() ?: ""
    suspend fun readRefreshToken(): String = refreshToken().firstOrNull() ?: ""
    suspend fun readUserId(): String = userId().firstOrNull() ?: ""
    suspend fun readForcedSlot(): Long = forcedSlot().firstOrNull() ?: -1L
    suspend fun readLastDisplayedContentId(): String = lastDisplayedContentId().firstOrNull() ?: ""
    suspend fun readLastDisplayedContentText(): String = lastDisplayedContentText().firstOrNull() ?: ""
    suspend fun readLastDisplayedContentAuthor(): String = lastDisplayedContentAuthor().firstOrNull() ?: ""
    suspend fun readLastDisplayedShownAt(): Long = lastDisplayedShownAt().firstOrNull() ?: 0L
    suspend fun readLastDisplayedNextChangeAt(): Long = lastDisplayedNextChangeAt().firstOrNull() ?: 0L
    suspend fun readLastDisplayedFavorite(): Boolean = lastDisplayedFavorite().firstOrNull() ?: false

    suspend fun setLastDisplayedFavorite(value: Boolean) {
        context.dataStore.edit { it[Keys.lastDisplayedFavorite] = value }
    }

    suspend fun setLastDisplayedWidgetState(contentItemId: String, text: String, author: String?, shownAt: Long, nextChangeAt: Long) {
        context.dataStore.edit {
            it[Keys.lastDisplayedContentId] = contentItemId
            it[Keys.lastDisplayedContentText] = text
            it[Keys.lastDisplayedShownAt] = shownAt
            it[Keys.lastDisplayedNextChangeAt] = nextChangeAt
            it[Keys.lastDisplayedFavorite] = false
            if (author.isNullOrBlank()) {
                it.remove(Keys.lastDisplayedContentAuthor)
            } else {
                it[Keys.lastDisplayedContentAuthor] = author
            }
        }
    }

    suspend fun clearWidgetState() {
        context.dataStore.edit {
            it.remove(Keys.lastDisplayedContentId)
            it.remove(Keys.lastDisplayedContentText)
            it.remove(Keys.lastDisplayedContentAuthor)
            it.remove(Keys.lastDisplayedShownAt)
            it.remove(Keys.lastDisplayedNextChangeAt)
            it.remove(Keys.lastDisplayedFavorite)
        }
    }
}

private fun stringKey(name: String): Preferences.Key<String> = androidx.datastore.preferences.core.stringPreferencesKey(name)
private fun longKey(name: String): Preferences.Key<Long> = androidx.datastore.preferences.core.longPreferencesKey(name)
