package com.linger.app.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J&\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\fJ\"\u0010\r\u001a\u0004\u0018\u00010\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u0012J\f\u0010\u0013\u001a\u00020\u000e*\u00020\u0014H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/linger/app/data/repository/FeedRepository;", "", "dao", "Lcom/linger/app/data/local/dao/ContentDao;", "(Lcom/linger/app/data/local/dao/ContentDao;)V", "addQueueItem", "", "contentItemId", "", "slotIndex", "", "source", "(Ljava/lang/String;ILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "selectNextItem", "Lcom/linger/app/domain/model/ContentItem;", "slotAtMillis", "", "intervalMinutes", "(JILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "toDomainFromEntity", "Lcom/linger/app/data/local/entity/ContentEntity;", "app_debug"})
public final class FeedRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.linger.app.data.local.dao.ContentDao dao = null;
    
    public FeedRepository(@org.jetbrains.annotations.NotNull()
    com.linger.app.data.local.dao.ContentDao dao) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object selectNextItem(long slotAtMillis, int intervalMinutes, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.linger.app.domain.model.ContentItem> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object addQueueItem(@org.jetbrains.annotations.NotNull()
    java.lang.String contentItemId, int slotIndex, @org.jetbrains.annotations.NotNull()
    java.lang.String source, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final com.linger.app.domain.model.ContentItem toDomainFromEntity(com.linger.app.data.local.entity.ContentEntity $this$toDomainFromEntity) {
        return null;
    }
}