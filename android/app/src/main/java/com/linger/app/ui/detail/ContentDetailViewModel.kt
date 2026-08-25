package com.linger.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.ContentDetailResponse
import com.linger.app.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContentDetailUiState(
    val loading: Boolean = true,
    val detail: ContentDetailResponse? = null,
    val loadFailed: Boolean = false,
)

@HiltViewModel
class ContentDetailViewModel @Inject constructor(
    private val api: AppApiService,
    private val session: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val contentId: String = savedStateHandle["contentId"] ?: ""
    private val _state = MutableStateFlow(ContentDetailUiState())
    val state = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, loadFailed = false)
        runCatching { session.withAuthRetry { api.getContentDetail(contentId) } }
            .onSuccess { _state.value = ContentDetailUiState(loading = false, detail = it) }
            .onFailure { _state.value = ContentDetailUiState(loading = false, loadFailed = true) }
    }
}
