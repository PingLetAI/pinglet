package com.linger.app.ui.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.IngestUrlResponse
import com.linger.app.data.repository.SessionManager
import com.linger.app.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProcessingQueueUiState(
    val loading: Boolean = true,
    val items: List<IngestUrlResponse> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class ProcessingQueueViewModel @Inject constructor(
    private val api: AppApiService,
    private val sessionManager: SessionManager,
    private val contentRepository: ContentRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ProcessingQueueUiState())
    val state: StateFlow<ProcessingQueueUiState> = _state.asStateFlow()
    private val syncedReadyIds = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            while (isActive) {
                runCatching { sessionManager.withAuthRetry { api.getIngestions() } }
                    .onSuccess { items ->
                        _state.value = ProcessingQueueUiState(loading = false, items = items)
                        val newlyReady = items.filter { it.status == "READY" && it.id !in syncedReadyIds }
                        if (newlyReady.isNotEmpty()) {
                            runCatching { sessionManager.withAuthRetry { contentRepository.syncFeed() } }
                                .onSuccess { syncedReadyIds += newlyReady.map { it.id } }
                        }
                    }
                    .onFailure { _state.value = _state.value.copy(loading = false, error = it.message) }
                delay(3_000)
            }
        }
    }
}
