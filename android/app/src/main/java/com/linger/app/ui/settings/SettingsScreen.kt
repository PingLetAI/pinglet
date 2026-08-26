package com.linger.app.ui.settings

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.BuildConfig
import com.linger.app.data.remote.EntitlementResponse
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerPage
import com.linger.app.ui.components.SectionLabel
import com.linger.app.ui.components.StatusPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onCreateAccount: () -> Unit = {}, onTryPlus: () -> Unit = {}, onUpgrade: () -> Unit = {}, onOpenQueue: () -> Unit = {}, onOpenWidgetSettings: () -> Unit = {}, onAccountReset: () -> Unit = {}, entitlementRefreshKey: Long = 0L, viewModel: SettingsViewModel = hiltViewModel()) {
    val entitlement by viewModel.entitlement.collectAsState()
    val accountAction by viewModel.accountAction.collectAsState()
    val textSize by viewModel.widgetTextSize.collectAsState()
    val opacity by viewModel.widgetOpacity.collectAsState()
    val mix by viewModel.personalSystemMix.collectAsState()
    val context = LocalContext.current
    var showSignOut by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var deletionCode by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { viewModel.refresh() }
    LaunchedEffect(entitlementRefreshKey) { if (entitlementRefreshKey > 0L) viewModel.refresh() }
    LaunchedEffect(accountAction.sessionReset) { if (accountAction.sessionReset) onAccountReset() }

    LingerPage("Settings", "Make PingLet yours.", "Account, rotation, and widget preferences.") {
        AccountSummary(entitlement, onCreateAccount, onTryPlus, onUpgrade, { showSignOut = true }, { showDelete = true }) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions?package=${BuildConfig.APPLICATION_ID}")))
        }

        SectionLabel("ROTATION")
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
            SettingsRow(Icons.Rounded.CloudQueue, "Processing queue", "See shared-post progress", onClick = onOpenQueue)
        }

        SectionLabel("HOME SCREEN WIDGET")
        SettingsGroup {
            SettingsRow(Icons.Rounded.Widgets, "Manage Home Screen widgets", "Independent themes, content, schedules, and controls", onClick = onOpenWidgetSettings)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Resize any widget directly from your Home Screen.", style = MaterialTheme.typography.bodyMedium)
                Text("Basic readability and translucency remain available to everyone.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
