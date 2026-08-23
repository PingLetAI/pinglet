package com.linger.app.ui.paywall;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0015R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000b0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u0016"}, d2 = {"Lcom/linger/app/ui/paywall/PaywallViewModel;", "Landroidx/lifecycle/ViewModel;", "billing", "Lcom/linger/app/billing/PlayBillingManager;", "api", "Lcom/linger/app/data/remote/AppApiService;", "session", "Lcom/linger/app/data/repository/SessionManager;", "(Lcom/linger/app/billing/PlayBillingManager;Lcom/linger/app/data/remote/AppApiService;Lcom/linger/app/data/repository/SessionManager;)V", "_state", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/linger/app/ui/paywall/PaywallUiState;", "state", "Lkotlinx/coroutines/flow/StateFlow;", "getState", "()Lkotlinx/coroutines/flow/StateFlow;", "buy", "", "activity", "Landroid/app/Activity;", "basePlanId", "", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class PaywallViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.billing.PlayBillingManager billing = null;
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.remote.AppApiService api = null;
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.repository.SessionManager session = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.linger.app.ui.paywall.PaywallUiState> _state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.linger.app.ui.paywall.PaywallUiState> state = null;
    
    @javax.inject.Inject()
    public PaywallViewModel(@org.jetbrains.annotations.NotNull()
    com.linger.app.billing.PlayBillingManager billing, @org.jetbrains.annotations.NotNull()
    com.linger.app.data.remote.AppApiService api, @org.jetbrains.annotations.NotNull()
    com.linger.app.data.repository.SessionManager session) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.linger.app.ui.paywall.PaywallUiState> getState() {
        return null;
    }
    
    public final void buy(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity, @org.jetbrains.annotations.NotNull()
    java.lang.String basePlanId) {
    }
}