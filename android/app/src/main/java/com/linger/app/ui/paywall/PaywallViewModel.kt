package com.linger.app.ui.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.linger.app.billing.PlayBillingManager
import com.linger.app.data.remote.AppApiService
import com.linger.app.data.remote.GooglePlayPurchaseRequest
import com.linger.app.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaywallUiState(
    val loading: Boolean = true,
    val product: ProductDetails? = null,
    val verifying: Boolean = false,
    val purchased: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billing: PlayBillingManager,
    private val api: AppApiService,
    private val session: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow(PaywallUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = runCatching { billing.queryPlus().firstOrNull() }
                .fold({ PaywallUiState(loading = false, product = it) }, { PaywallUiState(loading = false, error = "Google Play billing is unavailable in this build.") })
        }
        viewModelScope.launch {
            billing.purchases.collect { purchase ->
                _state.value = _state.value.copy(verifying = true, error = null)
                runCatching {
                    session.withAuthRetry {
                        api.verifyGooglePlayPurchase(GooglePlayPurchaseRequest(purchase.purchaseToken, PlayBillingManager.PRODUCT_ID))
                    }
                }.onSuccess {
                    billing.acknowledge(purchase)
                    _state.value = _state.value.copy(verifying = false, purchased = true)
                }.onFailure {
                    _state.value = _state.value.copy(verifying = false, error = it.message ?: "Purchase verification failed.")
                }
            }
        }
    }

    fun buy(activity: Activity, basePlanId: String) {
        val product = _state.value.product ?: return
        val result = billing.launch(activity, product, basePlanId)
        if (result.responseCode != 0) _state.value = _state.value.copy(error = result.debugMessage)
    }

    fun restore() = viewModelScope.launch {
        _state.value = _state.value.copy(verifying = true, error = null)
        runCatching { billing.restorePurchases() }
            .onSuccess { found -> if (!found) _state.value = _state.value.copy(verifying = false, error = "No active PingLet subscription was found for this Google Play account.") }
            .onFailure { _state.value = _state.value.copy(verifying = false, error = "Purchases could not be restored. Try again.") }
    }
}
