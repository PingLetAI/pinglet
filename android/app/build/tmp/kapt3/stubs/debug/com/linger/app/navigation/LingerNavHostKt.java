package com.linger.app.navigation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000F\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\u001a2\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u00052\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00070\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00070\fH\u0003\u001a\u001a\u0010\r\u001a\u00020\u00072\u0006\u0010\u000e\u001a\u00020\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u0007\u001a0\u0010\u0012\u001a\u00020\u0007*\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00022\u0006\u0010\u0015\u001a\u00020\u00162\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00070\nH\u0003\"\u0014\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"navItems", "", "Lcom/linger/app/navigation/NavItem;", "topLevelRoutes", "", "", "LingerBottomBar", "", "route", "onNavigate", "Lkotlin/Function1;", "onAdd", "Lkotlin/Function0;", "LingerNavHost", "navController", "Landroidx/navigation/NavHostController;", "sharedText", "Lcom/linger/app/domain/model/DeepLink;", "NavDestination", "Landroidx/compose/foundation/layout/RowScope;", "item", "selected", "", "app_debug"})
public final class LingerNavHostKt {
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<com.linger.app.navigation.NavItem> navItems = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Set<java.lang.String> topLevelRoutes = null;
    
    @androidx.compose.runtime.Composable()
    public static final void LingerNavHost(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavHostController navController, @org.jetbrains.annotations.Nullable()
    com.linger.app.domain.model.DeepLink sharedText) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void LingerBottomBar(java.lang.String route, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigate, kotlin.jvm.functions.Function0<kotlin.Unit> onAdd) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void NavDestination(androidx.compose.foundation.layout.RowScope $this$NavDestination, com.linger.app.navigation.NavItem item, boolean selected, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigate) {
    }
}