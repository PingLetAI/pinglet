package com.linger.app.ui.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.linger.app.billing.PlayBillingManager
import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.GooglePlayPurchaseRequest
import com.linger.app.data.remote.EventBatchRequest
import com.linger.app.data.remote.EventPayload
import com.linger.app.data.repository.SessionManager
import com.linger.app.data.local.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.Instant

data class PaywallUiState(
    val loading: Boolean = true,
    val product: ProductDetails? = null,
    val verifying: Boolean = false,
    val purchased: Boolean = false,
    val billingEnabled: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billing: PlayBillingManager,
    private val api: AppApiService,
    private val session: SessionManager,
    private val dataStore: DataStoreManager,
) : ViewModel() {
    private val _state = MutableStateFlow(PaywallUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val entitlement = runCatching { session.withAuthRetry { api.getEntitlements() } }.getOrNull()
            entitlement?.let { dataStore.setEntitlement(it) }
            if (entitlement?.paidPlansEnabled != true) {
                _state.value = PaywallUiState(loading = false, billingEnabled = false)
            } else {
                _state.value = runCatching { billing.queryPlus().firstOrNull() }
                    .fold({ PaywallUiState(loading = false, product = it, billingEnabled = true) }, { PaywallUiState(loading = false, billingEnabled = true, error = "Google Play billing is temporarily unavailable.") })
            }
        }
        viewModelScope.launch {
            billing.purchases.collect { purchase ->
                _state.value = _state.value.copy(verifying = true, error = null)
                runCatching {
                    session.withAuthRetry {
                        api.verifyGooglePlayPurchase(GooglePlayPurchaseRequest(purchase.purchaseToken, PlayBillingManager.PRODUCT_ID))
                    }
                }.onSuccess { entitlement ->
                    billing.acknowledge(purchase)
                    dataStore.setEntitlement(entitlement)
                    track("PURCHASE_COMPLETED")
                    _state.value = _state.value.copy(verifying = false, purchased = true)
                }.onFailure {
                    _state.value = _state.value.copy(verifying = false, error = it.message ?: "Purchase verification failed.")
                }
            }
        }
    }

    fun buy(activity: Activity, basePlanId: String) {
        if (!_state.value.billingEnabled) return
        val product = _state.value.product ?: return
        track("PURCHASE_STARTED", basePlanId)
        val result = billing.launch(activity, product, basePlanId)
        if (result.responseCode != 0) _state.value = _state.value.copy(error = result.debugMessage)
    }

    fun restore() = viewModelScope.launch {
        if (!_state.value.billingEnabled) return@launch
        _state.value = _state.value.copy(verifying = true, error = null)
        runCatching { billing.restorePurchases() }
            .onSuccess { found -> if (!found) _state.value = _state.value.copy(verifying = false, error = "No active PingLet subscription was found for this Google Play account.") }
            .onFailure { _state.value = _state.value.copy(verifying = false, error = "Purchases could not be restored. Try again.") }
    }

    private fun track(type: String, metadata: String? = null) = viewModelScope.launch {
        runCatching {
            session.withAuthRetry {
                api.postEventBatch(EventBatchRequest(listOf(EventPayload(type = type, surface = "APP", timestamp = Instant.now().toString(), metadata = metadata))))
            }
        }
    }
}
