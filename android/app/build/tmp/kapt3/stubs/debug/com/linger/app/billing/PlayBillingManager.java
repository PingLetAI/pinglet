package com.linger.app.billing;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010!\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\b\u0007\u0018\u0000  2\u00020\u0001:\u0001 B\u0011\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0007J\u000e\u0010\u0011\u001a\u00020\u000fH\u0082@\u00a2\u0006\u0002\u0010\u0012J\u001e\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001aJ \u0010\u001b\u001a\u00020\u000f2\u0006\u0010\u001c\u001a\u00020\u00142\u000e\u0010\n\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u001dH\u0016J\u0014\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00180\u001fH\u0086@\u00a2\u0006\u0002\u0010\u0012R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00070\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006!"}, d2 = {"Lcom/linger/app/billing/PlayBillingManager;", "Lcom/android/billingclient/api/PurchasesUpdatedListener;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "_purchases", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/android/billingclient/api/Purchase;", "client", "Lcom/android/billingclient/api/BillingClient;", "purchases", "Lkotlinx/coroutines/flow/SharedFlow;", "getPurchases", "()Lkotlinx/coroutines/flow/SharedFlow;", "acknowledge", "", "purchase", "connect", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "launch", "Lcom/android/billingclient/api/BillingResult;", "activity", "Landroid/app/Activity;", "details", "Lcom/android/billingclient/api/ProductDetails;", "basePlanId", "", "onPurchasesUpdated", "result", "", "queryPlus", "", "Companion", "app_debug"})
public final class PlayBillingManager implements com.android.billingclient.api.PurchasesUpdatedListener {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.android.billingclient.api.Purchase> _purchases = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.android.billingclient.api.Purchase> purchases = null;
    @org.jetbrains.annotations.NotNull()
    private final com.android.billingclient.api.BillingClient client = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PRODUCT_ID = "linger_plus";
    @org.jetbrains.annotations.NotNull()
    public static final com.linger.app.billing.PlayBillingManager.Companion Companion = null;
    
    @javax.inject.Inject()
    public PlayBillingManager(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.android.billingclient.api.Purchase> getPurchases() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object queryPlus(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.android.billingclient.api.ProductDetails>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.android.billingclient.api.BillingResult launch(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity, @org.jetbrains.annotations.NotNull()
    com.android.billingclient.api.ProductDetails details, @org.jetbrains.annotations.NotNull()
    java.lang.String basePlanId) {
        return null;
    }
    
    public final void acknowledge(@org.jetbrains.annotations.NotNull()
    com.android.billingclient.api.Purchase purchase) {
    }
    
    @java.lang.Override()
    public void onPurchasesUpdated(@org.jetbrains.annotations.NotNull()
    com.android.billingclient.api.BillingResult result, @org.jetbrains.annotations.Nullable()
    java.util.List<com.android.billingclient.api.Purchase> purchases) {
    }
    
    private final java.lang.Object connect(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/linger/app/billing/PlayBillingManager$Companion;", "", "()V", "PRODUCT_ID", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}