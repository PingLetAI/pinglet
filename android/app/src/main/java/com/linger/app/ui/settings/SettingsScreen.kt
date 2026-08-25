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
fun SettingsScreen(onCreateAccount: () -> Unit = {}, onUpgrade: () -> Unit = {}, onOpenQueue: () -> Unit = {}, viewModel: SettingsViewModel = hiltViewModel()) {
    val entitlement by viewModel.entitlement.collectAsState()
    val textSize by viewModel.widgetTextSize.collectAsState()
    val opacity by viewModel.widgetOpacity.collectAsState()
    val mix by viewModel.personalSystemMix.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.refresh() }

    LingerPage("Settings", "Make PingLet yours.", "Account, rotation, and widget preferences.") {
        AccountSummary(entitlement, onCreateAccount, onUpgrade) {
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
        LingerCard(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .58f)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.Widgets, null); Spacer(Modifier.width(10.dp)); Text("Widget appearance", style = MaterialTheme.typography.titleMedium) }
            Text("TEXT SIZE", style = MaterialTheme.typography.labelMedium)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf("SMALL", "MEDIUM", "LARGE").forEachIndexed { index, value -> SegmentedButton(textSize == value, { viewModel.setWidgetTextSize(value) }, SegmentedButtonDefaults.itemShape(index, 3), label = { Text(value.lowercase().replaceFirstChar(Char::uppercase)) }, icon = {}) }
            }
            Text("TRANSLUCENCY", style = MaterialTheme.typography.labelMedium)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf(62 to "Light", 78 to "Blend", 90 to "Solid").forEachIndexed { index, value -> SegmentedButton(opacity == value.first, { viewModel.setWidgetOpacity(value.first) }, SegmentedButtonDefaults.itemShape(index, 3), label = { Text(value.second) }, icon = {}) }
            }
            Text("Long-press the widget to resize it. Text and spacing adapt automatically.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
}

@Composable
private fun AccountSummary(entitlement: EntitlementResponse?, onCreateAccount: () -> Unit, onUpgrade: () -> Unit, onManageSubscription: () -> Unit) {
    val plan = entitlement?.plan ?: "GUEST"
    SectionLabel("ACCOUNT")
    LingerCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) { Icon(if (entitlement?.isAnonymous == false) Icons.Rounded.VerifiedUser else Icons.Rounded.PersonOutline, null, Modifier.padding(11.dp).size(24.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(when (plan) { "PLUS" -> "PingLet Plus"; "FREE" -> "Free account"; else -> "Guest profile" }, style = MaterialTheme.typography.titleLarge)
                Text(entitlement?.email ?: "Not connected to an email", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            StatusPill(if (plan == "PLUS") "PLUS" else if (entitlement?.isAnonymous == false) "VERIFIED" else "LOCAL")
        }
        if (entitlement != null) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Usage("SAVES", entitlement.saveCount, entitlement.saveLimit)
                Usage("AI IMPORTS", entitlement.socialImportsUsed, entitlement.socialImportLimit)
            }
        } else LinearProgressIndicator(Modifier.fillMaxWidth())
        when {
            entitlement?.isAnonymous != false -> Button(onCreateAccount, Modifier.fillMaxWidth()) { Text("CONNECT EMAIL") }
            plan != "PLUS" -> Button(onUpgrade, Modifier.fillMaxWidth()) { Text("EXPLORE PINGLET PLUS") }
            else -> OutlinedButton(onManageSubscription, Modifier.fillMaxWidth()) { Text("MANAGE SUBSCRIPTION") }
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
