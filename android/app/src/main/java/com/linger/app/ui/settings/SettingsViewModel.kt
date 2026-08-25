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
import android.content.Context
import androidx.glance.appwidget.updateAll
import com.linger.app.data.local.DataStoreManager
import com.linger.app.widget.AmbientWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val api: AppApiService,
    private val session: SessionManager,
    private val dataStore: DataStoreManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _entitlement = MutableStateFlow<EntitlementResponse?>(null)
    val entitlement = _entitlement.asStateFlow()
    val widgetTextSize = dataStore.widgetTextSize().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "SMALL")
    val widgetOpacity = dataStore.widgetOpacity().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 78)
    val personalSystemMix = dataStore.personalSystemMix().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "BALANCED")

    fun refresh() = viewModelScope.launch {
        runCatching { session.withAuthRetry { api.getEntitlements() } }.onSuccess { _entitlement.value = it }
    }

    fun setWidgetTextSize(value: String) = viewModelScope.launch {
        dataStore.setWidgetTextSize(value)
        AmbientWidget().updateAll(context)
    }

    fun setWidgetOpacity(value: Int) = viewModelScope.launch {
        dataStore.setWidgetOpacity(value)
        AmbientWidget().updateAll(context)
    }

    fun setPersonalSystemMix(value: String) = viewModelScope.launch {
        dataStore.setPersonalSystemMix(value)
        runCatching { session.withAuthRetry { api.patchPreferences(mapOf("personalSystemMix" to value)) } }
    }
}
