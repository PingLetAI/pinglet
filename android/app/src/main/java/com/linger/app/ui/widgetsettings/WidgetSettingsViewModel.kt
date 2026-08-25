package com.linger.app.ui.widgetsettings

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.CatalogResponse
import com.linger.app.data.repository.SessionManager
import com.linger.app.widget.AmbientWidget
import com.linger.app.widget.WidgetProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WidgetSettingsUiState(
    val loading: Boolean = true,
    val widgetKeys: List<String> = emptyList(),
    val selectedKey: String? = null,
    val profiles: Map<String, WidgetProfile> = emptyMap(),
    val catalogs: List<CatalogResponse> = emptyList(),
    val isPlus: Boolean = false,
)

@HiltViewModel
class WidgetSettingsViewModel @Inject constructor(
    private val api: AppApiService,
    private val session: SessionManager,
    private val store: DataStoreManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(WidgetSettingsUiState())
    val state = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        val keys = GlanceAppWidgetManager(context).getGlanceIds<AmbientWidget>(AmbientWidget::class.java).map { it.toString() }
        val profiles = keys.associateWith { store.readWidgetProfile(it) }
        val entitlement = runCatching { session.withAuthRetry { api.getEntitlements() } }.getOrNull()
        entitlement?.let { store.setEntitlementPlan(it.plan) }
        val catalogs = runCatching { session.withAuthRetry { api.getCatalogPreferences() } }.getOrDefault(emptyList())
        _state.value = WidgetSettingsUiState(false, keys, keys.firstOrNull(), profiles, catalogs, entitlement?.plan == "PLUS" || store.readEntitlementPlan() == "PLUS")
    }

    fun select(key: String) { _state.value = _state.value.copy(selectedKey = key) }

    fun update(premium: Boolean = true, transform: (WidgetProfile) -> WidgetProfile) {
        val current = _state.value
        val key = current.selectedKey ?: return
        if (premium && !current.isPlus) return
        val profile = transform(current.profiles[key] ?: WidgetProfile())
        _state.value = current.copy(profiles = current.profiles + (key to profile))
        viewModelScope.launch {
            store.setWidgetProfile(key, profile)
            AmbientWidget().updateAll(context)
        }
    }
}
