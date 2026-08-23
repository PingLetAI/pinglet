package com.linger.app.widget;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 \f2\u00020\u0001:\u0001\fB\u0005\u00a2\u0006\u0002\u0010\u0002J&\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0096@\u00a2\u0006\u0002\u0010\u000b\u00a8\u0006\r"}, d2 = {"Lcom/linger/app/widget/FavoriteContentAction;", "Landroidx/glance/appwidget/action/ActionCallback;", "()V", "onAction", "", "context", "Landroid/content/Context;", "glanceId", "Landroidx/glance/GlanceId;", "parameters", "Landroidx/glance/action/ActionParameters;", "(Landroid/content/Context;Landroidx/glance/GlanceId;Landroidx/glance/action/ActionParameters;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"})
public final class FavoriteContentAction implements androidx.glance.appwidget.action.ActionCallback {
    @org.jetbrains.annotations.NotNull()
    private static final androidx.glance.action.ActionParameters.Key<java.lang.String> contentIdKey = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.sync.Mutex toggleMutex = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.linger.app.widget.FavoriteContentAction.Companion Companion = null;
    
    public FavoriteContentAction() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object onAction(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    androidx.glance.GlanceId glanceId, @org.jetbrains.annotations.NotNull()
    androidx.glance.action.ActionParameters parameters, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/linger/app/widget/FavoriteContentAction$Companion;", "", "()V", "contentIdKey", "Landroidx/glance/action/ActionParameters$Key;", "", "getContentIdKey", "()Landroidx/glance/action/ActionParameters$Key;", "toggleMutex", "Lkotlinx/coroutines/sync/Mutex;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final androidx.glance.action.ActionParameters.Key<java.lang.String> getContentIdKey() {
            return null;
        }
    }
}