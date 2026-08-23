package com.linger.app.ui.settings;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00004\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0003\u001a<\u0010\u0000\u001a\u00020\u00012\b\u0010\u0002\u001a\u0004\u0018\u00010\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a>\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\f2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\f2\u0010\b\u0002\u0010\u000f\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0005H\u0003\u001aB\u0010\u0010\u001a\u00020\u00012\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u000e\b\u0002\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u0012\u001a\u00020\u0013H\u0007\u001a/\u0010\u0014\u001a\u00020\u00012\u0006\u0010\u0015\u001a\u00020\f2\u0006\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u00172\u0006\u0010\r\u001a\u00020\fH\u0003\u00a2\u0006\u0002\u0010\u0019\u00a8\u0006\u001a"}, d2 = {"AccountSection", "", "entitlement", "Lcom/linger/app/data/remote/EntitlementResponse;", "onCreateAccount", "Lkotlin/Function0;", "onUpgrade", "onManageSubscription", "SettingsActionRow", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "title", "", "detail", "trailing", "onClick", "SettingsScreen", "onOpenQueue", "viewModel", "Lcom/linger/app/ui/settings/SettingsViewModel;", "UsageMeter", "label", "used", "", "limit", "(Ljava/lang/String;ILjava/lang/Integer;Ljava/lang/String;)V", "app_debug"})
public final class SettingsScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void SettingsScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onCreateAccount, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onUpgrade, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onOpenQueue, @org.jetbrains.annotations.NotNull()
    com.linger.app.ui.settings.SettingsViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void AccountSection(com.linger.app.data.remote.EntitlementResponse entitlement, kotlin.jvm.functions.Function0<kotlin.Unit> onCreateAccount, kotlin.jvm.functions.Function0<kotlin.Unit> onUpgrade, kotlin.jvm.functions.Function0<kotlin.Unit> onManageSubscription) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void UsageMeter(java.lang.String label, int used, java.lang.Integer limit, java.lang.String detail) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SettingsActionRow(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String title, java.lang.String detail, java.lang.String trailing, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}