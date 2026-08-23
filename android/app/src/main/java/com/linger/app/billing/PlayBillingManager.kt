package com.linger.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class PlayBillingManager @Inject constructor(@ApplicationContext context: Context) : PurchasesUpdatedListener {
    private val _purchases = MutableSharedFlow<Purchase>(extraBufferCapacity = 4)
    val purchases = _purchases.asSharedFlow()
    private val client = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    suspend fun queryPlus(): List<ProductDetails> {
        connect()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                ),
            ).build()
        return suspendCancellableCoroutine { continuation ->
            client.queryProductDetailsAsync(params) { result, details ->
                continuation.resume(if (result.responseCode == BillingClient.BillingResponseCode.OK) details.productDetailsList else emptyList())
            }
        }
    }

    fun launch(activity: Activity, details: ProductDetails, basePlanId: String): BillingResult {
        val offer = details.subscriptionOfferDetails?.firstOrNull { it.basePlanId == basePlanId }
            ?: return BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.ITEM_UNAVAILABLE).setDebugMessage("Plan unavailable").build()
        val product = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offer.offerToken)
            .build()
        return client.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(product)).build())
    }

    fun acknowledge(purchase: Purchase) {
        if (purchase.isAcknowledged) return
        client.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build(),
        ) { }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases.orEmpty().filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }.forEach(_purchases::tryEmit)
        }
    }

    private suspend fun connect() {
        if (client.isReady) return
        suspendCancellableCoroutine { continuation ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() = Unit
                override fun onBillingSetupFinished(result: BillingResult) {
                    continuation.resume(Unit)
                }
            })
        }
    }

    companion object { const val PRODUCT_ID = "pinglet_plus" }
}
