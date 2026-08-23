package com.linger.app.ui.add;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\b\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0006\u0010\u0010\u001a\u00020\u0011J \u0010\u0012\u001a\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u0014\u0012\u0006\u0012\u0004\u0018\u00010\u00140\u00132\u0006\u0010\u0015\u001a\u00020\u0016H\u0002J\u0006\u0010\u0017\u001a\u00020\u0011J \u0010\u0018\u001a\u00020\u00112\u0006\u0010\u0019\u001a\u00020\u00142\u0006\u0010\u001a\u001a\u00020\u00142\b\u0010\u001b\u001a\u0004\u0018\u00010\u0014J*\u0010\u001c\u001a\u0004\u0018\u00010\u00142\u0006\u0010\u0019\u001a\u00020\u00142\u0006\u0010\u001a\u001a\u00020\u00142\b\u0010\u001b\u001a\u0004\u0018\u00010\u0014H\u0082@\u00a2\u0006\u0002\u0010\u001dR\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000b0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u001e"}, d2 = {"Lcom/linger/app/ui/add/AddContentViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/linger/app/data/repository/ContentRepository;", "sessionManager", "Lcom/linger/app/data/repository/SessionManager;", "api", "Lcom/linger/app/data/remote/AppApiService;", "(Lcom/linger/app/data/repository/ContentRepository;Lcom/linger/app/data/repository/SessionManager;Lcom/linger/app/data/remote/AppApiService;)V", "_state", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/linger/app/ui/add/AddContentUiState;", "state", "Lkotlinx/coroutines/flow/StateFlow;", "getState", "()Lkotlinx/coroutines/flow/StateFlow;", "consumeGate", "", "parseApiError", "Lkotlin/Pair;", "", "error", "", "refreshEntitlements", "save", "text", "type", "url", "saveContent", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class AddContentViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.repository.ContentRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.repository.SessionManager sessionManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.remote.AppApiService api = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.linger.app.ui.add.AddContentUiState> _state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.linger.app.ui.add.AddContentUiState> state = null;
    
    @javax.inject.Inject()
    public AddContentViewModel(@org.jetbrains.annotations.NotNull()
    com.linger.app.data.repository.ContentRepository repository, @org.jetbrains.annotations.NotNull()
    com.linger.app.data.repository.SessionManager sessionManager, @org.jetbrains.annotations.NotNull()
    com.linger.app.data.remote.AppApiService api) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.linger.app.ui.add.AddContentUiState> getState() {
        return null;
    }
    
    public final void refreshEntitlements() {
    }
    
    public final void consumeGate() {
    }
    
    public final void save(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    java.lang.String type, @org.jetbrains.annotations.Nullable()
    java.lang.String url) {
    }
    
    private final java.lang.Object saveContent(java.lang.String text, java.lang.String type, java.lang.String url, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final kotlin.Pair<java.lang.String, java.lang.String> parseApiError(java.lang.Throwable error) {
        return null;
    }
}