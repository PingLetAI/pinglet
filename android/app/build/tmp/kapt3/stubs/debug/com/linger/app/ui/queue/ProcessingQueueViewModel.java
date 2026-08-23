package com.linger.app.ui.queue;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010#\n\u0002\u0010\u000e\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bR\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000b0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/linger/app/ui/queue/ProcessingQueueViewModel;", "Landroidx/lifecycle/ViewModel;", "api", "Lcom/linger/app/data/remote/AppApiService;", "sessionManager", "Lcom/linger/app/data/repository/SessionManager;", "contentRepository", "Lcom/linger/app/data/repository/ContentRepository;", "(Lcom/linger/app/data/remote/AppApiService;Lcom/linger/app/data/repository/SessionManager;Lcom/linger/app/data/repository/ContentRepository;)V", "_state", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/linger/app/ui/queue/ProcessingQueueUiState;", "state", "Lkotlinx/coroutines/flow/StateFlow;", "getState", "()Lkotlinx/coroutines/flow/StateFlow;", "syncedReadyIds", "", "", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ProcessingQueueViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.remote.AppApiService api = null;
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.repository.SessionManager sessionManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.repository.ContentRepository contentRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.linger.app.ui.queue.ProcessingQueueUiState> _state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.linger.app.ui.queue.ProcessingQueueUiState> state = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<java.lang.String> syncedReadyIds = null;
    
    @javax.inject.Inject()
    public ProcessingQueueViewModel(@org.jetbrains.annotations.NotNull()
    com.linger.app.data.remote.AppApiService api, @org.jetbrains.annotations.NotNull()
    com.linger.app.data.repository.SessionManager sessionManager, @org.jetbrains.annotations.NotNull()
    com.linger.app.data.repository.ContentRepository contentRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.linger.app.ui.queue.ProcessingQueueUiState> getState() {
        return null;
    }
}