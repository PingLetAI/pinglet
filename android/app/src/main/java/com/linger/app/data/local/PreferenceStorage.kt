package com.linger.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
        val lastDisplayedSourceUrl = stringKey("last_displayed_source_url")
        val favoriteContentIds = stringSetPreferencesKey("favorite_content_ids")
        val widgetTextSize = stringKey("widget_text_size")
        val widgetOpacity = intPreferencesKey("widget_opacity")
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
    fun widgetTextSize(): Flow<String> = prefsFlow.map { it[Keys.widgetTextSize] ?: "SMALL" }
    fun widgetOpacity(): Flow<Int> = prefsFlow.map { it[Keys.widgetOpacity] ?: 78 }

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
    suspend fun readLastDisplayedSourceUrl(): String = prefsFlow.map { it[Keys.lastDisplayedSourceUrl] ?: "" }.firstOrNull() ?: ""
    suspend fun readFavoriteContentIds(): Set<String> = prefsFlow.map { it[Keys.favoriteContentIds] ?: emptySet() }.firstOrNull() ?: emptySet()

    suspend fun isContentFavorite(contentItemId: String): Boolean = contentItemId in readFavoriteContentIds()

    suspend fun setContentFavorite(contentItemId: String, favorite: Boolean) {
        context.dataStore.edit { prefs ->
            val ids = (prefs[Keys.favoriteContentIds] ?: emptySet()).toMutableSet()
            if (favorite) ids.add(contentItemId) else ids.remove(contentItemId)
            prefs[Keys.favoriteContentIds] = ids
            if (prefs[Keys.lastDisplayedContentId] == contentItemId) {
                prefs[Keys.lastDisplayedFavorite] = favorite
            }
        }
    }

    suspend fun replaceFavoriteContentIds(contentItemIds: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.favoriteContentIds] = contentItemIds
            val currentId = prefs[Keys.lastDisplayedContentId]
            if (!currentId.isNullOrBlank()) prefs[Keys.lastDisplayedFavorite] = currentId in contentItemIds
        }
    }

    suspend fun mergeFeedFavoriteContentIds(feedContentIds: Set<String>, favoriteContentIds: Set<String>) {
        context.dataStore.edit { prefs ->
            val merged = (prefs[Keys.favoriteContentIds] ?: emptySet()).toMutableSet()
            merged.removeAll(feedContentIds)
            merged.addAll(favoriteContentIds)
            prefs[Keys.favoriteContentIds] = merged
            val currentId = prefs[Keys.lastDisplayedContentId]
            if (!currentId.isNullOrBlank()) prefs[Keys.lastDisplayedFavorite] = currentId in merged
        }
    }

    suspend fun setWidgetTextSize(value: String) {
        context.dataStore.edit { it[Keys.widgetTextSize] = value }
    }

    suspend fun setWidgetOpacity(value: Int) {
        context.dataStore.edit { it[Keys.widgetOpacity] = value.coerceIn(55, 92) }
    }

    suspend fun setLastDisplayedFavorite(value: Boolean) {
        context.dataStore.edit { it[Keys.lastDisplayedFavorite] = value }
    }

    suspend fun setLastDisplayedWidgetState(contentItemId: String, text: String, author: String?, sourceUrl: String?, favorite: Boolean, shownAt: Long, nextChangeAt: Long) {
        context.dataStore.edit {
            it[Keys.lastDisplayedContentId] = contentItemId
            it[Keys.lastDisplayedContentText] = text
            it[Keys.lastDisplayedShownAt] = shownAt
            it[Keys.lastDisplayedNextChangeAt] = nextChangeAt
            it[Keys.lastDisplayedFavorite] = favorite
            if (sourceUrl.isNullOrBlank()) it.remove(Keys.lastDisplayedSourceUrl) else it[Keys.lastDisplayedSourceUrl] = sourceUrl
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
            it.remove(Keys.lastDisplayedSourceUrl)
        }
    }
}

private fun stringKey(name: String): Preferences.Key<String> = androidx.datastore.preferences.core.stringPreferencesKey(name)
private fun longKey(name: String): Preferences.Key<Long> = androidx.datastore.preferences.core.longPreferencesKey(name)
