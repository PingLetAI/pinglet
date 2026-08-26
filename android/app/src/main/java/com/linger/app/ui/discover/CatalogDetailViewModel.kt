package com.linger.app.ui.discover

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.CatalogDetailResponse
import com.linger.app.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogDetailUiState(
    val loading: Boolean = true,
    val catalog: CatalogDetailResponse? = null,
    val updating: Boolean = false,
    val actingOnItemId: String? = null,
    val notice: String? = null,
    val error: String? = null,
)

@HiltViewModel
class CatalogDetailViewModel @Inject constructor(
    private val api: AppApiService,
    private val session: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val id: String = savedStateHandle["catalogId"] ?: ""
    private val _state = MutableStateFlow(CatalogDetailUiState())
    val state = _state.asStateFlow()
    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        runCatching { session.withAuthRetry { api.getMyCatalogDetail(id) } }
            .onSuccess { _state.value = CatalogDetailUiState(loading = false, catalog = it) }
            .onFailure { _state.value = CatalogDetailUiState(loading = false, error = "This collection could not be loaded.") }
    }

    fun toggle() {
        val catalog = _state.value.catalog ?: return
        if (_state.value.updating) return
        val target = !catalog.enabled
        _state.value = _state.value.copy(catalog = catalog.copy(enabled = target), updating = true)
        viewModelScope.launch {
            runCatching { session.withAuthRetry { api.patchCatalogPreference(id, mapOf("enabled" to target)) } }
                .onSuccess { _state.value = _state.value.copy(updating = false) }
                .onFailure { _state.value = _state.value.copy(catalog = catalog, updating = false, error = "Collection preference could not be updated.") }
        }
    }

    fun report(contentItemId: String, reason: String) = actOnItem(contentItemId, "Report received. This PingLet is now hidden.") {
        api.reportExploreItem(contentItemId, mapOf("reason" to reason))
    }

    fun hideSource(contentItemId: String) = actOnItem(contentItemId, "This source is now hidden from Explore.") {
        api.hideExploreSource(contentItemId)
    }

    private fun actOnItem(contentItemId: String, successMessage: String, request: suspend () -> com.linger.app.data.remote.ExploreActionResponse) {
        if (_state.value.actingOnItemId != null) return
        _state.value = _state.value.copy(actingOnItemId = contentItemId, error = null, notice = null)
        viewModelScope.launch {
            runCatching { session.withAuthRetry { request() } }
                .onSuccess { response ->
                    val hidden = response.hiddenContentIds.toSet()
                    val catalog = _state.value.catalog
                    val items = catalog?.items?.filterNot { it.id in hidden }.orEmpty()
                    _state.value = _state.value.copy(
                        catalog = catalog?.copy(items = items, itemCount = items.size),
                        actingOnItemId = null,
                        notice = successMessage,
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(actingOnItemId = null, error = "That action could not be completed. Try again.")
                }
        }
    }
}
