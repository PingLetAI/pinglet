package com.linger.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.EmailOtpRequest
import com.linger.app.data.remote.EmailOtpVerifyRequest
import com.linger.app.data.repository.SessionManager
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.local.db.DatabaseProvider
import com.linger.app.data.remote.RetrofitClient
import com.linger.app.sync.SyncScheduler
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

data class AccountUiState(
    val email: String = "",
    val codeSent: Boolean = false,
    val loading: Boolean = false,
    val verified: Boolean = false,
    val devCode: String? = null,
    val error: String? = null,
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val api: AppApiService,
    private val session: SessionManager,
    private val dataStore: DataStoreManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(AccountUiState())
    val state = _state.asStateFlow()

    fun requestCode(email: String) = viewModelScope.launch {
        _state.value = AccountUiState(email = email.trim(), loading = true)
        runCatching { session.withAuthRetry { api.requestEmailOtp(EmailOtpRequest(email.trim())) } }
            .onSuccess { _state.value = _state.value.copy(loading = false, codeSent = true, devCode = it.devCode) }
            .onFailure { _state.value = _state.value.copy(loading = false, error = message(it)) }
    }

    fun verify(code: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        runCatching {
            session.withAuthRetry { api.verifyEmailOtp(EmailOtpVerifyRequest(_state.value.email, code.trim())) }
        }.onSuccess { response ->
            if (!response.accessToken.isNullOrBlank() && !response.refreshToken.isNullOrBlank() && !response.userId.isNullOrBlank()) {
                withContext(Dispatchers.IO) { DatabaseProvider.database(context).clearAllTables() }
                dataStore.clearAccountData()
                dataStore.setAuthSession(response.accessToken, response.refreshToken, response.userId)
                RetrofitClient.setAuthToken(response.accessToken)
                SyncScheduler.scheduleInitialSync(context)
            }
            _state.value = _state.value.copy(loading = false, verified = true)
        }.onFailure {
            _state.value = _state.value.copy(loading = false, error = message(it))
        }
    }

    private fun message(error: Throwable): String {
        val body = (error as? HttpException)?.response()?.errorBody()?.string()
        return body?.substringAfter("\"message\":\"")?.substringBefore('"')
            ?: error.message ?: "Something went wrong. Try again."
    }
}
