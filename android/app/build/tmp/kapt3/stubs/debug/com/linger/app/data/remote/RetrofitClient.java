package com.linger.app.data.remote;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\u0004J\u0010\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\u0004R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/linger/app/data/remote/RetrofitClient;", "", "()V", "authToken", "", "instance", "Lcom/linger/app/data/remote/AppApiService;", "build", "baseUrl", "setAuthToken", "", "token", "app_debug"})
public final class RetrofitClient {
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.linger.app.data.remote.AppApiService instance;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile java.lang.String authToken;
    @org.jetbrains.annotations.NotNull()
    public static final com.linger.app.data.remote.RetrofitClient INSTANCE = null;
    
    private RetrofitClient() {
        super();
    }
    
    public final void setAuthToken(@org.jetbrains.annotations.Nullable()
    java.lang.String token) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.linger.app.data.remote.AppApiService build(@org.jetbrains.annotations.NotNull()
    java.lang.String baseUrl) {
        return null;
    }
}