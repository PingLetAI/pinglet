package com.linger.app.ui.settings;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u000e\u001a\u00020\u000fR\u0016\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/linger/app/ui/settings/SettingsViewModel;", "Landroidx/lifecycle/ViewModel;", "api", "Lcom/linger/app/data/remote/AppApiService;", "session", "Lcom/linger/app/data/repository/SessionManager;", "(Lcom/linger/app/data/remote/AppApiService;Lcom/linger/app/data/repository/SessionManager;)V", "_entitlement", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/linger/app/data/remote/EntitlementResponse;", "entitlement", "Lkotlinx/coroutines/flow/StateFlow;", "getEntitlement", "()Lkotlinx/coroutines/flow/StateFlow;", "refresh", "Lkotlinx/coroutines/Job;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class SettingsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.remote.AppApiService api = null;
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.repository.SessionManager session = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.linger.app.data.remote.EntitlementResponse> _entitlement = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.linger.app.data.remote.EntitlementResponse> entitlement = null;
    
    @javax.inject.Inject()
    public SettingsViewModel(@org.jetbrains.annotations.NotNull()
    com.linger.app.data.remote.AppApiService api, @org.jetbrains.annotations.NotNull()
    com.linger.app.data.repository.SessionManager session) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.linger.app.data.remote.EntitlementResponse> getEntitlement() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job refresh() {
        return null;
    }
}