package com.linger.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings as AndroidSettings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.linger.app.BuildConfig
import com.linger.app.data.remote.EntitlementResponse
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerPage
import com.linger.app.ui.components.SectionLabel
import com.linger.app.ui.components.StatusPill
import com.linger.app.ui.widget.WidgetInstallPrompt
import com.linger.app.ui.widget.WidgetManualInstallDialog
import com.linger.app.widget.WidgetPinning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onCreateAccount: () -> Unit = {}, onTryPlus: () -> Unit = {}, onUpgrade: () -> Unit = {}, onOpenQueue: () -> Unit = {}, onOpenWidgetSettings: () -> Unit = {}, onAccountReset: () -> Unit = {}, entitlementRefreshKey: Long = 0L, viewModel: SettingsViewModel = hiltViewModel()) {
    val entitlement by viewModel.entitlement.collectAsState()
    val accountAction by viewModel.accountAction.collectAsState()
    val textSize by viewModel.widgetTextSize.collectAsState()
    val opacity by viewModel.widgetOpacity.collectAsState()
    val mix by viewModel.personalSystemMix.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var widgetInstalled by remember { mutableStateOf(WidgetPinning.hasInstalledWidget(context)) }
    var widgetPinRequestPending by remember { mutableStateOf(false) }
    var showWidgetPrompt by remember { mutableStateOf(false) }
    var showWidgetManualHelp by remember { mutableStateOf(false) }
    var showSignOut by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var deletionCode by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { viewModel.refresh() }
    LaunchedEffect(entitlementRefreshKey) { if (entitlementRefreshKey > 0L) viewModel.refresh() }
    LaunchedEffect(accountAction.sessionReset) { if (accountAction.sessionReset) onAccountReset() }
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                widgetInstalled = WidgetPinning.hasInstalledWidget(context)
                if (widgetPinRequestPending) {
                    widgetPinRequestPending = false
                    if (!widgetInstalled) showWidgetManualHelp = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LingerPage("Settings", "Your PingLet, your rhythm.", "A quieter place for your account and experience.") {
        AccountSummary(entitlement, onCreateAccount, onTryPlus, onUpgrade, { showSignOut = true }, { showDelete = true }) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions?package=${BuildConfig.APPLICATION_ID}")))
        }

        if (!widgetInstalled) {
            WidgetSetupCard { showWidgetPrompt = true }
        } else {
            SectionLabel("EXPERIENCE")
            SettingsGroup {
                SettingsRow(Icons.Rounded.Schedule, "Every 30 minutes", "Approximate timing, optimized for battery")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("CONTENT BALANCE", style = MaterialTheme.typography.labelMedium)
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        listOf("MOSTLY_MINE" to "Mine", "BALANCED" to "Balanced", "MORE_DISCOVERY" to "Discover").forEachIndexed { index, option ->
                            SegmentedButton(mix == option.first, { viewModel.setPersonalSystemMix(option.first) }, SegmentedButtonDefaults.itemShape(index, 3), label = { Text(option.second) }, icon = {})
                        }
                    }
                    Text("Personal saves are always prioritized.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsRow(Icons.Rounded.Widgets, "Widget appearance", "Themes, schedules, typography, and controls", onClick = onOpenWidgetSettings)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsRow(Icons.Rounded.CloudQueue, "Processing queue", "Shared-post progress and history", onClick = onOpenQueue)
            }
        }

        SectionLabel("ABOUT")
        SettingsGroup {
            SettingsRow(Icons.Rounded.Info, "PingLet for Android", "Version ${BuildConfig.VERSION_NAME}")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingsRow(Icons.Rounded.PrivacyTip, "Privacy policy", "How PingLet handles your data") { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://pinglet.ai/privacy"))) }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingsRow(Icons.Rounded.Description, "Terms of service", "The terms for using PingLet") { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://pinglet.ai/terms"))) }
        }
        Text("Rotation can shift slightly while Android is conserving battery.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    if (showWidgetPrompt) {
        WidgetInstallPrompt(
            onAddWidget = {
                showWidgetPrompt = false
                coroutineScope.launch {
                    val requestSent = WidgetPinning.requestPin(context)
                    if (!requestSent) {
                        showWidgetManualHelp = true
                    } else {
                        widgetPinRequestPending = true
                        delay(2_000L)
                        if (widgetPinRequestPending && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            widgetPinRequestPending = false
                            widgetInstalled = WidgetPinning.hasInstalledWidget(context)
                            if (!widgetInstalled) showWidgetManualHelp = true
                        }
                    }
                }
            },
            onDismiss = { showWidgetPrompt = false },
        )
    }
    if (showWidgetManualHelp) {
        WidgetManualInstallDialog(
            onOpenHomeSettings = {
                runCatching { context.startActivity(Intent(AndroidSettings.ACTION_HOME_SETTINGS)) }
                    .onSuccess { showWidgetManualHelp = false }
            },
            onDismiss = { showWidgetManualHelp = false },
        )
    }

    if (showSignOut) AlertDialog(
        onDismissRequest = { if (!accountAction.loading) showSignOut = false },
        icon = { Icon(Icons.Rounded.Logout, null) },
        title = { Text("Sign out on this device?") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Your PingLets remain in your account. This device will return to a new guest profile and clear its account-specific cache.")
            accountAction.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        } },
        confirmButton = { Button(onClick = { viewModel.signOut() }, enabled = !accountAction.loading) { Text(if (accountAction.loading) "SIGNING OUT..." else "SIGN OUT") } },
        dismissButton = { TextButton(onClick = { showSignOut = false }) { Text("CANCEL") } },
    )
    if (showDelete) AlertDialog(
        onDismissRequest = { if (!accountAction.loading) { showDelete = false; viewModel.clearAccountAction() } },
        icon = { Icon(Icons.Rounded.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Delete account and data?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("This permanently deletes your PingLet account, personal saves, imports, favorites, devices, and account history. This cannot be undone.")
                if (accountAction.deletionCodeSent) {
                    Text("Enter the six-digit code sent to ${entitlement?.email} to confirm deletion.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(deletionCode, { deletionCode = it.filter(Char::isDigit).take(6) }, label = { Text("Verification code") }, singleLine = true)
                } else Text("We will send a fresh verification code to ${entitlement?.email} before deleting anything.", style = MaterialTheme.typography.bodySmall)
                accountAction.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (accountAction.deletionCodeSent) viewModel.deleteAccount(deletionCode) else viewModel.requestDeletionCode() },
                enabled = !accountAction.loading && (!accountAction.deletionCodeSent || deletionCode.length == 6),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) { Text(if (accountAction.loading) "PLEASE WAIT..." else if (accountAction.deletionCodeSent) "DELETE PERMANENTLY" else "SEND VERIFICATION CODE") }
        },
        dismissButton = { TextButton(onClick = { showDelete = false; viewModel.clearAccountAction() }) { Text("CANCEL") } },
    )
}

@Composable
private fun WidgetSetupCard(onAddWidget: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = androidx.compose.ui.graphics.Color(0xFF171914),
        contentColor = androidx.compose.ui.graphics.Color(0xFFF8F6F0),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = MaterialTheme.shapes.medium, color = androidx.compose.ui.graphics.Color(0xFFC7E6D7)) {
                    Icon(Icons.Rounded.Widgets, null, Modifier.padding(10.dp).size(23.dp), tint = androidx.compose.ui.graphics.Color(0xFF171914))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("HOME SCREEN WIDGET", style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color(0xFFDDAE3D))
                    Text("Bring PingLet Home", style = MaterialTheme.typography.titleLarge)
                }
            }
            Text("Let meaningful ideas return without opening the app.", style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color(0xFFD7D6D0))
            Button(
                onClick = onAddWidget,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFFDDAE3D),
                    contentColor = androidx.compose.ui.graphics.Color(0xFF171914),
                ),
            ) {
                Text("ADD HOME SCREEN WIDGET")
            }
        }
    }
}

@Composable
private fun AccountSummary(entitlement: EntitlementResponse?, onCreateAccount: () -> Unit, onTryPlus: () -> Unit, onUpgrade: () -> Unit, onSignOut: () -> Unit, onDeleteAccount: () -> Unit, onManageSubscription: () -> Unit) {
    val plan = entitlement?.plan ?: "GUEST"
    SectionLabel("ACCOUNT")
    LingerCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) { Icon(if (entitlement?.isAnonymous == false) Icons.Rounded.VerifiedUser else Icons.Rounded.PersonOutline, null, Modifier.padding(11.dp).size(24.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(when { entitlement?.trialStatus == "ACTIVE" -> "PingLet Plus trial"; plan == "PLUS" -> "PingLet Plus"; plan == "FREE" -> "Free account"; else -> "Guest profile" }, style = MaterialTheme.typography.titleLarge)
                Text(entitlement?.email ?: "Not connected to an email", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            StatusPill(if (entitlement?.trialStatus == "ACTIVE") "TRIAL" else if (plan == "PLUS") "PLUS" else if (entitlement?.isAnonymous == false) "VERIFIED" else "LOCAL")
        }
        if (entitlement != null) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Usage("SAVES", entitlement.saveCount, entitlement.saveLimit)
                Usage("AI IMPORTS", entitlement.socialImportsUsed, entitlement.socialImportLimit)
            }
        } else LinearProgressIndicator(Modifier.fillMaxWidth())
        if (entitlement?.trialStatus == "ACTIVE") Text("${entitlement.trialDaysRemaining} days of Plus remaining. Your account returns to Free automatically; you will not be charged.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        when {
            entitlement?.isAnonymous != false -> Button(onCreateAccount, Modifier.fillMaxWidth()) { Text("CONNECT EMAIL") }
            entitlement.trialStatus == "ACTIVE" && entitlement.paidPlansEnabled -> Button(onUpgrade, Modifier.fillMaxWidth()) { Text("KEEP PINGLET PLUS") }
            entitlement.trialStatus == "ACTIVE" -> Text("Paid plans are coming soon. Your trial still ends automatically with no charge.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            plan != "PLUS" && entitlement.trialEligible -> {
                Button(onTryPlus, Modifier.fillMaxWidth()) { Text("TRY PINGLET PLUS - 7 DAYS FREE") }
                if (entitlement.paidPlansEnabled) TextButton(onUpgrade, Modifier.fillMaxWidth()) { Text("VIEW PAID PLANS") }
            }
            plan != "PLUS" && entitlement.paidPlansEnabled -> Button(onUpgrade, Modifier.fillMaxWidth()) { Text("EXPLORE PINGLET PLUS") }
            plan != "PLUS" -> Text("PingLet Plus subscriptions are coming soon.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            entitlement.entitlementSource == "GOOGLE_PLAY" -> OutlinedButton(onManageSubscription, Modifier.fillMaxWidth()) { Text("MANAGE SUBSCRIPTION") }
            entitlement.paidPlansEnabled -> OutlinedButton(onUpgrade, Modifier.fillMaxWidth()) { Text("VIEW PLUS PLANS") }
        }
        if (entitlement?.isAnonymous == false) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onSignOut) { Icon(Icons.Rounded.Logout, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("SIGN OUT") }
                TextButton(onClick = onDeleteAccount, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("DELETE ACCOUNT") }
            }
        }
    }
}

@Composable private fun Usage(label: String, used: Int, limit: Int?) { Column { Text(label, style = MaterialTheme.typography.labelMedium); Text(limit?.let { "$used of $it" } ?: "Unlimited", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) { Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) { Column(content = content) } }

@Composable
private fun SettingsRow(icon: ImageVector, title: String, detail: String, onClick: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.tertiary); Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (onClick != null) Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
