package com.linger.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.EntitlementResponse
import com.linger.app.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val api: AppApiService,
    private val session: SessionManager,
) : ViewModel() {
    private val _entitlement = MutableStateFlow<EntitlementResponse?>(null)
    val entitlement = _entitlement.asStateFlow()

    fun refresh() = viewModelScope.launch {
        runCatching { session.withAuthRetry { api.getEntitlements() } }.onSuccess { _entitlement.value = it }
    }
}
