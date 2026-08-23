package com.linger.app.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0010\b\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J \u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\rH\u0086@\u00a2\u0006\u0002\u0010\u000fJ(\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\r2\u0006\u0010\u0012\u001a\u00020\r2\b\u0010\u0013\u001a\u0004\u0018\u00010\rH\u0086@\u00a2\u0006\u0002\u0010\u0014J\u0014\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u0086@\u00a2\u0006\u0002\u0010\u0016J\u001e\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\t0\b2\b\b\u0002\u0010\u0018\u001a\u00020\u0019H\u0086@\u00a2\u0006\u0002\u0010\u001aR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2 = {"Lcom/linger/app/data/repository/ContentRepository;", "", "api", "Lcom/linger/app/data/remote/AppApiService;", "dao", "Lcom/linger/app/data/local/dao/ContentDao;", "(Lcom/linger/app/data/remote/AppApiService;Lcom/linger/app/data/local/dao/ContentDao;)V", "seededItems", "", "Lcom/linger/app/domain/model/ContentItem;", "enqueueUrl", "Lcom/linger/app/data/remote/IngestUrlResponse;", "url", "", "contextText", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveContent", "text", "type", "categorySlug", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "seedBootstrapItemsIfQueueEmpty", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncFeed", "limit", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class ContentRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.remote.AppApiService api = null;
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.local.dao.ContentDao dao = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.linger.app.domain.model.ContentItem> seededItems = null;
    
    public ContentRepository(@org.jetbrains.annotations.NotNull()
    com.linger.app.data.remote.AppApiService api, @org.jetbrains.annotations.NotNull()
    com.linger.app.data.local.dao.ContentDao dao) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object syncFeed(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.linger.app.domain.model.ContentItem>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object seedBootstrapItemsIfQueueEmpty(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.linger.app.domain.model.ContentItem>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object saveContent(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    java.lang.String type, @org.jetbrains.annotations.Nullable()
    java.lang.String categorySlug, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object enqueueUrl(@org.jetbrains.annotations.NotNull()
    java.lang.String url, @org.jetbrains.annotations.Nullable()
    java.lang.String contextText, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.linger.app.data.remote.IngestUrlResponse> $completion) {
        return null;
    }
}