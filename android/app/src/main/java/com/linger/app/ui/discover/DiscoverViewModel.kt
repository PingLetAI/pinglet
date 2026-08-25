package com.linger.app.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.CatalogResponse
import com.linger.app.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val loading: Boolean = true,
    val catalogs: List<CatalogResponse> = emptyList(),
    val updatingIds: Set<String> = emptySet(),
    val error: String? = null,
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val api: AppApiService,
    private val session: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow(DiscoverUiState())
    val state = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        runCatching { session.withAuthRetry { api.getCatalogPreferences() } }
            .onSuccess { _state.value = DiscoverUiState(loading = false, catalogs = it) }
            .onFailure { _state.value = _state.value.copy(loading = false, error = "Check your connection and try again.") }
    }

    fun toggle(id: String) {
        val catalog = _state.value.catalogs.firstOrNull { it.id == id } ?: return
        if (id in _state.value.updatingIds) return
        val target = !catalog.enabled
        _state.value = _state.value.copy(
            catalogs = _state.value.catalogs.map { if (it.id == id) it.copy(enabled = target) else it },
            updatingIds = _state.value.updatingIds + id,
        )
        viewModelScope.launch {
            val success = runCatching { session.withAuthRetry { api.patchCatalogPreference(id, mapOf("enabled" to target)) } }.isSuccess
            _state.value = _state.value.copy(
                catalogs = if (success) _state.value.catalogs else _state.value.catalogs.map { if (it.id == id) it.copy(enabled = !target) else it },
                updatingIds = _state.value.updatingIds - id,
                error = if (success) null else "That collection could not be updated.",
            )
        }
    }
}
