package com.linger.app.ui.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.glance.appwidget.updateAll
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.local.dao.ContentDao
import com.linger.app.data.local.entity.ContentEntity
import com.linger.app.data.local.entity.UserContentEntity
import com.linger.app.data.remote.AppApiService
import com.linger.app.data.repository.SessionManager
import com.linger.app.widget.AmbientWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryItemUi(val contentItemId: String, val text: String, val type: String, val author: String?, val sourceUrl: String?, val favorite: Boolean)
data class LibraryUiState(
    val loading: Boolean = true,
    val items: List<LibraryItemUi> = emptyList(),
    val error: String? = null,
    val updatingFavoriteIds: Set<String> = emptySet(),
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val api: AppApiService,
    private val sessionManager: SessionManager,
    private val dao: ContentDao,
    private val dataStore: DataStoreManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()

    fun toggleFavorite(contentItemId: String) {
        val current = _state.value
        if (contentItemId in current.updatingFavoriteIds) return
        val item = current.items.firstOrNull { it.contentItemId == contentItemId } ?: return
        val target = !item.favorite
        _state.value = current.copy(
            items = current.items.map { if (it.contentItemId == contentItemId) it.copy(favorite = target) else it },
            updatingFavoriteIds = current.updatingFavoriteIds + contentItemId,
        )
        viewModelScope.launch {
            val success = runCatching {
                sessionManager.withAuthRetry {
                    if (target) api.favorite(contentItemId) else api.unfavorite(contentItemId)
                }
            }.isSuccess
            if (success) {
                val userId = dataStore.readUserId()
                dao.setUserContentFavorite(userId, contentItemId, target, System.currentTimeMillis())
                if (dataStore.readLastDisplayedContentId() == contentItemId) {
                    dataStore.setLastDisplayedFavorite(target)
                    AmbientWidget().updateAll(context)
                }
            }
            _state.value = _state.value.copy(
                items = if (success) _state.value.items else _state.value.items.map {
                    if (it.contentItemId == contentItemId) it.copy(favorite = !target) else it
                },
                updatingFavoriteIds = _state.value.updatingFavoriteIds - contentItemId,
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { sessionManager.withAuthRetry { api.getMyContent() } }
                .onSuccess { rows ->
                    val userId = dataStore.readUserId()
                    val now = System.currentTimeMillis()
                    dao.upsertContent(rows.map { row ->
                        ContentEntity(row.contentItem.id, row.contentItem.text, row.contentItem.type.name, row.contentItem.author, "PERSONAL", row.contentItem.sourceUrl, "PRIVATE", userId, "ACTIVE", "en")
                    })
                    dao.upsertUserContent(rows.map { row ->
                        UserContentEntity(row.id, userId, row.contentItemId, row.favorite, row.archived, 1f, now, now)
                    })
                    _state.value = LibraryUiState(false, rows.filterNot { it.archived }.map { row ->
                        LibraryItemUi(row.contentItemId, row.contentItem.text, row.contentItem.type.name, row.contentItem.author, row.contentItem.sourceUrl, row.favorite)
                    })
                }
                .onFailure {
                    val local = loadLocal()
                    _state.value = LibraryUiState(false, local, if (local.isEmpty()) "Your library could not be loaded. Check your connection and try again." else null)
                }
        }
    }

    private suspend fun loadLocal(): List<LibraryItemUi> {
        val userId = dataStore.readUserId()
        if (userId.isBlank()) return emptyList()
        return dao.userLibrary(userId).filterNot { it.archived }.mapNotNull { relation ->
            dao.contentById(relation.contentItemId)?.let { content ->
                LibraryItemUi(relation.contentItemId, content.text, content.type, content.author, content.sourceUrl, relation.favorite)
            }
        }
    }
}
