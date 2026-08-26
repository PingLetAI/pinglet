package com.linger.app.ui.trial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.EntitlementResponse
import com.linger.app.data.remote.EventBatchRequest
import com.linger.app.data.remote.EventPayload
import com.linger.app.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrialOfferUiState(
    val loading: Boolean = true,
    val entitlement: EntitlementResponse? = null,
    val activating: Boolean = false,
    val activated: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class TrialOfferViewModel @Inject constructor(
    private val api: AppApiService,
    private val session: SessionManager,
    private val dataStore: DataStoreManager,
) : ViewModel() {
    private val _state = MutableStateFlow(TrialOfferUiState())
    val state = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        runCatching { session.withAuthRetry { api.getEntitlements() } }
            .onSuccess {
                dataStore.setEntitlement(it)
                _state.value = _state.value.copy(loading = false, entitlement = it)
            }
            .onFailure { _state.value = _state.value.copy(loading = false, error = "Plus details could not be loaded. Try again.") }
    }

    fun startTrial(entrySource: String) = viewModelScope.launch {
        if (_state.value.activating) return@launch
        _state.value = _state.value.copy(activating = true, error = null)
        runCatching { session.withAuthRetry { api.startPlusTrial() } }
            .onSuccess {
                dataStore.setEntitlement(it)
                track("TRIAL_STARTED", entrySource)
                _state.value = _state.value.copy(activating = false, activated = true, entitlement = it)
            }
            .onFailure {
                _state.value = _state.value.copy(activating = false, error = "Your free trial could not be started. Refresh and try again.")
            }
    }

    fun track(type: String, entrySource: String) = viewModelScope.launch {
        runCatching {
            session.withAuthRetry {
                api.postEventBatch(EventBatchRequest(listOf(EventPayload(type = type, surface = "APP", timestamp = Instant.now().toString(), metadata = entrySource))))
            }
        }
    }
}
