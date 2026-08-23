package com.linger.app.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/linger/app/data/repository/AuthRepositoryImpl;", "Lcom/linger/app/data/repository/AuthRepository;", "api", "Lcom/linger/app/data/remote/AppApiService;", "(Lcom/linger/app/data/remote/AppApiService;)V", "anonymous", "Lcom/linger/app/data/remote/AuthAnonymousResponse;", "installationId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "refresh", "Lcom/linger/app/data/remote/AuthRefreshResponse;", "refreshToken", "app_debug"})
public final class AuthRepositoryImpl implements com.linger.app.data.repository.AuthRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.remote.AppApiService api = null;
    
    public AuthRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.linger.app.data.remote.AppApiService api) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object anonymous(@org.jetbrains.annotations.NotNull()
    java.lang.String installationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.linger.app.data.remote.AuthAnonymousResponse> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object refresh(@org.jetbrains.annotations.NotNull()
    java.lang.String refreshToken, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.linger.app.data.remote.AuthRefreshResponse> $completion) {
        return null;
    }
}