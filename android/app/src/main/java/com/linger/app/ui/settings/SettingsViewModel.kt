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
import com.linger.app.data.local.db.DatabaseProvider
import com.linger.app.data.remote.DeleteAccountRequest
import com.linger.app.data.remote.EmailOtpRequest
import com.linger.app.data.remote.RetrofitClient
import com.linger.app.sync.SyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AccountActionUiState(
    val loading: Boolean = false,
    val deletionCodeSent: Boolean = false,
    val sessionReset: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val api: AppApiService,
    private val session: SessionManager,
    private val dataStore: DataStoreManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _entitlement = MutableStateFlow<EntitlementResponse?>(null)
    val entitlement = _entitlement.asStateFlow()
    private val _accountAction = MutableStateFlow(AccountActionUiState())
    val accountAction = _accountAction.asStateFlow()
    val widgetTextSize = dataStore.widgetTextSize().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "SMALL")
    val widgetOpacity = dataStore.widgetOpacity().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 78)
    val personalSystemMix = dataStore.personalSystemMix().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "BALANCED")

    fun refresh() = viewModelScope.launch {
        runCatching { session.withAuthRetry { api.getEntitlements() } }.onSuccess {
            _entitlement.value = it
            dataStore.setEntitlement(it)
        }
    }

    fun signOut() = viewModelScope.launch {
        _accountAction.value = AccountActionUiState(loading = true)
        try {
            session.withAuthRetry { api.logout() }
            resetToGuest()
        } catch (error: Throwable) {
            _accountAction.value = AccountActionUiState(error = "Sign-out could not reach PingLet. Check your connection and try again.")
        }
    }

    fun requestDeletionCode() = viewModelScope.launch {
        val email = _entitlement.value?.email ?: return@launch
        _accountAction.value = _accountAction.value.copy(loading = true, error = null)
        runCatching { session.withAuthRetry { api.requestEmailOtp(EmailOtpRequest(email)) } }
            .onSuccess { _accountAction.value = AccountActionUiState(deletionCodeSent = true) }
            .onFailure { _accountAction.value = _accountAction.value.copy(loading = false, error = it.message ?: "The verification code could not be sent.") }
    }

    fun deleteAccount(code: String) = viewModelScope.launch {
        val email = _entitlement.value?.email ?: return@launch
        _accountAction.value = _accountAction.value.copy(loading = true, error = null)
        runCatching { session.withAuthRetry { api.deleteAccount(DeleteAccountRequest(email, code.trim())) } }
            .onSuccess { resetToGuest() }
            .onFailure { _accountAction.value = _accountAction.value.copy(loading = false, error = it.message ?: "Your account could not be deleted.") }
    }

    fun clearAccountAction() { _accountAction.value = AccountActionUiState() }

    private suspend fun resetToGuest() {
        withContext(Dispatchers.IO) { DatabaseProvider.database(context).clearAllTables() }
        dataStore.clearAccountData()
        RetrofitClient.setAuthToken(null)
        session.startAnonymousSession()
        AmbientWidget().updateAll(context)
        SyncScheduler.scheduleInitialSync(context)
        _entitlement.value = runCatching { session.withAuthRetry { api.getEntitlements() } }.getOrNull()
        _accountAction.value = AccountActionUiState(sessionReset = true)
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
