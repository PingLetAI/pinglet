package com.linger.app.ui.account;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u000e\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u000fJ\u000e\u0010\u0015\u001a\u00020\u00132\u0006\u0010\u0016\u001a\u00020\u000fR\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006\u0017"}, d2 = {"Lcom/linger/app/ui/account/AccountViewModel;", "Landroidx/lifecycle/ViewModel;", "api", "Lcom/linger/app/data/remote/AppApiService;", "session", "Lcom/linger/app/data/repository/SessionManager;", "(Lcom/linger/app/data/remote/AppApiService;Lcom/linger/app/data/repository/SessionManager;)V", "_state", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/linger/app/ui/account/AccountUiState;", "state", "Lkotlinx/coroutines/flow/StateFlow;", "getState", "()Lkotlinx/coroutines/flow/StateFlow;", "message", "", "error", "", "requestCode", "Lkotlinx/coroutines/Job;", "email", "verify", "code", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class AccountViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.remote.AppApiService api = null;
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.repository.SessionManager session = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.linger.app.ui.account.AccountUiState> _state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.linger.app.ui.account.AccountUiState> state = null;
    
    @javax.inject.Inject()
    public AccountViewModel(@org.jetbrains.annotations.NotNull()
    com.linger.app.data.remote.AppApiService api, @org.jetbrains.annotations.NotNull()
    com.linger.app.data.repository.SessionManager session) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.linger.app.ui.account.AccountUiState> getState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job requestCode(@org.jetbrains.annotations.NotNull()
    java.lang.String email) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job verify(@org.jetbrains.annotations.NotNull()
    java.lang.String code) {
        return null;
    }
    
    private final java.lang.String message(java.lang.Throwable error) {
        return null;
    }
}