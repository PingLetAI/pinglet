package com.linger.app.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linger.app.data.repository.ContentRepository
import com.linger.app.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.EntitlementResponse
import org.json.JSONObject
import retrofit2.HttpException

enum class SaveGate { ACCOUNT, PLUS }

data class AddContentUiState(
    val saving: Boolean = false,
    val saved: Boolean = false,
    val queuedIngestionId: String? = null,
    val extractedText: String? = null,
    val error: String? = null,
    val entitlement: EntitlementResponse? = null,
    val gate: SaveGate? = null,
    val termsAccepted: Boolean? = null,
    val showTermsPrompt: Boolean = false,
    val acceptingTerms: Boolean = false,
)

private data class PendingSave(val text: String, val type: String, val url: String, val author: String?)

@HiltViewModel
class AddContentViewModel @Inject constructor(
    private val repository: ContentRepository,
    private val sessionManager: SessionManager,
    private val api: AppApiService,
) : ViewModel() {
    private var pendingSave: PendingSave? = null
    private val _state = MutableStateFlow(AddContentUiState())
    val state: StateFlow<AddContentUiState> = _state.asStateFlow()

    init {
        refreshEntitlements()
        refreshTermsStatus()
    }

    private fun refreshTermsStatus() {
        viewModelScope.launch {
            runCatching { sessionManager.withAuthRetry { api.getTermsStatus() } }
                .onSuccess { response -> _state.value = _state.value.copy(termsAccepted = response.accepted) }
        }
    }

    fun refreshEntitlements() {
        viewModelScope.launch {
            runCatching { sessionManager.withAuthRetry { api.getEntitlements() } }
                .onSuccess { entitlement ->
                    _state.value = _state.value.copy(
                        entitlement = entitlement,
                        error = null,
                        gate = null,
                    )
                }
        }
    }

    fun consumeGate() {
        _state.value = _state.value.copy(gate = null)
    }

    fun showSubscriptionsPending() {
        _state.value = _state.value.copy(error = "You have reached the current limit. PingLet Plus subscriptions are coming soon.", gate = null)
    }

    fun save(text: String, type: String, url: String?, author: String? = null) {
        if (_state.value.saving) return
        if (url != null && _state.value.termsAccepted != true) {
            pendingSave = PendingSave(text, type, url, author)
            _state.value = _state.value.copy(showTermsPrompt = true, error = null)
            return
        }
        performSave(text, type, url, author)
    }

    fun dismissTermsPrompt() {
        pendingSave = null
        _state.value = _state.value.copy(showTermsPrompt = false, acceptingTerms = false)
    }

    fun acceptTermsAndContinue() {
        if (_state.value.acceptingTerms) return
        viewModelScope.launch {
            _state.value = _state.value.copy(acceptingTerms = true, error = null)
            runCatching { sessionManager.withAuthRetry { api.acceptTerms() } }
                .onSuccess {
                    val pending = pendingSave
                    pendingSave = null
                    _state.value = _state.value.copy(
                        termsAccepted = true,
                        showTermsPrompt = false,
                        acceptingTerms = false,
                    )
                    pending?.let { performSave(it.text, it.type, it.url, it.author) }
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        acceptingTerms = false,
                        error = "Could not record your agreement. Check your connection and try again.",
                    )
                }
        }
    }

    private fun performSave(text: String, type: String, url: String?, author: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true, saved = false, error = null, gate = null)
            runCatching {
                sessionManager.withAuthRetry { saveContent(text, type, url, author) }
            }.onSuccess { queuedId ->
                _state.value = _state.value.copy(saving = false, saved = true, queuedIngestionId = queuedId)
            }.onFailure { error ->
                val apiError = parseApiError(error)
                if (apiError.first == "TERMS_ACCEPTANCE_REQUIRED" && url != null) {
                    pendingSave = PendingSave(text, type, url, author)
                    _state.value = _state.value.copy(
                        saving = false,
                        termsAccepted = false,
                        showTermsPrompt = true,
                        error = null,
                    )
                    return@onFailure
                }
                val gate = when (apiError.first) {
                    "ACCOUNT_REQUIRED" -> SaveGate.ACCOUNT
                    "UPGRADE_REQUIRED", "SOCIAL_IMPORT_LIMIT" -> SaveGate.PLUS
                    else -> null
                }
                _state.value = _state.value.copy(
                    saving = false,
                    error = apiError.second ?: "Could not save this post. Check the link and try again.",
                    gate = gate,
                )
            }
        }
    }

    private suspend fun saveContent(text: String, type: String, url: String?, author: String?): String? =
        if (url != null) {
            repository.enqueueUrl(url, text.replace(url, "").trim().ifBlank { null }).id
        } else {
            repository.saveContent(text.trim(), type.uppercase(), author)
            repository.syncFeed()
            null
        }

    private fun parseApiError(error: Throwable): Pair<String?, String?> {
        val body = (error as? HttpException)?.response()?.errorBody()?.string() ?: return null to error.message
        return runCatching {
            val json = JSONObject(body)
            json.optString("code").takeIf(String::isNotBlank) to
                json.optString("message").takeIf(String::isNotBlank)
        }.getOrDefault(null to error.message)
    }
}
