package com.linger.app.ui.library;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0007\u0018\u00002\u00020\u0001B1\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\b\b\u0001\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00160\u0015H\u0082@\u00a2\u0006\u0002\u0010\u0017J\u0006\u0010\u0018\u001a\u00020\u0019J\u000e\u0010\u001a\u001a\u00020\u00192\u0006\u0010\u001b\u001a\u00020\u001cR\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013\u00a8\u0006\u001d"}, d2 = {"Lcom/linger/app/ui/library/LibraryViewModel;", "Landroidx/lifecycle/ViewModel;", "api", "Lcom/linger/app/data/remote/AppApiService;", "sessionManager", "Lcom/linger/app/data/repository/SessionManager;", "dao", "Lcom/linger/app/data/local/dao/ContentDao;", "dataStore", "Lcom/linger/app/data/local/DataStoreManager;", "context", "Landroid/content/Context;", "(Lcom/linger/app/data/remote/AppApiService;Lcom/linger/app/data/repository/SessionManager;Lcom/linger/app/data/local/dao/ContentDao;Lcom/linger/app/data/local/DataStoreManager;Landroid/content/Context;)V", "_state", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/linger/app/ui/library/LibraryUiState;", "state", "Lkotlinx/coroutines/flow/StateFlow;", "getState", "()Lkotlinx/coroutines/flow/StateFlow;", "loadLocal", "", "Lcom/linger/app/ui/library/LibraryItemUi;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "refresh", "", "toggleFavorite", "contentItemId", "", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class LibraryViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.remote.AppApiService api = null;
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.repository.SessionManager sessionManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.local.dao.ContentDao dao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.local.DataStoreManager dataStore = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.linger.app.ui.library.LibraryUiState> _state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.linger.app.ui.library.LibraryUiState> state = null;
    
    @javax.inject.Inject()
    public LibraryViewModel(@org.jetbrains.annotations.NotNull()
    com.linger.app.data.remote.AppApiService api, @org.jetbrains.annotations.NotNull()
    com.linger.app.data.repository.SessionManager sessionManager, @org.jetbrains.annotations.NotNull()
    com.linger.app.data.local.dao.ContentDao dao, @org.jetbrains.annotations.NotNull()
    com.linger.app.data.local.DataStoreManager dataStore, @dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.linger.app.ui.library.LibraryUiState> getState() {
        return null;
    }
    
    public final void toggleFavorite(@org.jetbrains.annotations.NotNull()
    java.lang.String contentItemId) {
    }
    
    public final void refresh() {
    }
    
    private final java.lang.Object loadLocal(kotlin.coroutines.Continuation<? super java.util.List<com.linger.app.ui.library.LibraryItemUi>> $completion) {
        return null;
    }
}