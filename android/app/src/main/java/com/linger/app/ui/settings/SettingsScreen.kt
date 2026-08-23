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

@Composable
fun SettingsScreen(
    onCreateAccount: () -> Unit = {},
    onUpgrade: () -> Unit = {},
    onOpenQueue: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val entitlement by viewModel.entitlement.collectAsState()
    val widgetTextSize by viewModel.widgetTextSize.collectAsState()
    val widgetOpacity by viewModel.widgetOpacity.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.refresh() }

    LingerPage(
        eyebrow = "SETTINGS",
        title = "Make PingLet yours.",
        subtitle = "Your account, rotation, and widget at a glance.",
    ) {
        AccountSection(
            entitlement = entitlement,
            onCreateAccount = onCreateAccount,
            onUpgrade = onUpgrade,
            onManageSubscription = {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/account/subscriptions?package=${BuildConfig.APPLICATION_ID}"),
                    ),
                )
            },
        )

        SectionLabel("YOUR RHYTHM")
        LingerCard(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .78f)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface.copy(alpha = .72f)) {
                    Icon(Icons.Rounded.Schedule, null, Modifier.padding(12.dp).size(24.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Every 30 minutes", style = MaterialTheme.typography.titleLarge)
                    Text("Approximate timing, optimized for battery", style = MaterialTheme.typography.bodyMedium)
                }
                StatusPill("ACTIVE")
            }
        }

        SettingsActionRow(
            icon = Icons.Rounded.AutoAwesome,
            title = "Personal thoughts first",
            detail = "System catalogs only fill gaps in your queue",
            trailing = "BALANCED",
        )
        SettingsActionRow(
            icon = Icons.Rounded.CloudQueue,
            title = "Processing queue",
            detail = "Track shared posts while they become thoughts",
            onClick = onOpenQueue,
        )

        SectionLabel("HOME SCREEN WIDGET")
        LingerCard(color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = .68f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Widgets, null, Modifier.size(26.dp))
                Spacer(Modifier.width(12.dp))
                Text("Designed to fit your Home Screen", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                "Long-press the widget, then drag its handles. Its type and spacing adapt to the available width and height.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("TEXT SIZE", style = MaterialTheme.typography.labelMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("SMALL", "MEDIUM", "LARGE").forEach { size ->
                    FilterChip(selected = widgetTextSize == size, onClick = { viewModel.setWidgetTextSize(size) }, label = { Text(size.lowercase().replaceFirstChar { it.uppercase() }) })
                }
            }
            Text("TRANSLUCENCY", style = MaterialTheme.typography.labelMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(62 to "Light", 78 to "Balanced", 90 to "Solid").forEach { (opacity, label) ->
                    FilterChip(selected = widgetOpacity == opacity, onClick = { viewModel.setWidgetOpacity(opacity) }, label = { Text(label) })
                }
            }
        }

        SectionLabel("ABOUT")
        SettingsActionRow(
            icon = Icons.Rounded.Info,
            title = "PingLet for Android",
            detail = "Version ${BuildConfig.VERSION_NAME}",
            trailing = "AMBIENT",
        )
        Text(
            "Rotation timing can shift slightly when Android is conserving battery.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun AccountSection(
    entitlement: EntitlementResponse?,
    onCreateAccount: () -> Unit,
    onUpgrade: () -> Unit,
    onManageSubscription: () -> Unit,
) {
    val plan = entitlement?.plan ?: "GUEST"
    SectionLabel("ACCOUNT")
    LingerCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Icon(
                    if (entitlement?.isAnonymous == false) Icons.Rounded.VerifiedUser else Icons.Rounded.PersonOutline,
                    contentDescription = null,
                    modifier = Modifier.padding(13.dp).size(27.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    when (plan) { "PLUS" -> "PingLet Plus"; "FREE" -> "Free account"; else -> "Guest profile" },
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    entitlement?.email ?: "Not connected to an email",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            StatusPill(if (entitlement?.isAnonymous == false) "VERIFIED" else "LOCAL")
        }
        if (entitlement?.isAnonymous != false) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f))
            Text(
                "Connect an email before save 11 so your library can follow you to another device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onCreateAccount,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("CONNECT MY EMAIL") }
        }
    }

    SectionLabel("YOUR ALLOWANCE")
    if (entitlement == null) {
        LingerCard { LinearProgressIndicator(Modifier.fillMaxWidth()) }
    } else {
        LingerCard(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .46f)) {
            UsageMeter(
                label = "SAVED THOUGHTS",
                used = entitlement.saveCount,
                limit = entitlement.saveLimit,
                detail = entitlement.saveLimit?.let { "${entitlement.saveCount} of $it" } ?: "Unlimited",
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f))
            UsageMeter(
                label = "AI SOCIAL IMPORTS",
                used = entitlement.socialImportsUsed,
                limit = entitlement.socialImportLimit,
                detail = "${entitlement.socialImportsUsed} of ${entitlement.socialImportLimit}",
            )
            Text(
                "AI imports reset at the beginning of each month. Reused links do not count.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (plan == "PLUS") {
        LingerCard(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .78f)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.WorkspacePremium, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Plus is active", style = MaterialTheme.typography.titleLarge)
                    Text("Unlimited saves and 30 AI imports monthly")
                }
                StatusPill("PLUS")
            }
            OutlinedButton(onClick = onManageSubscription, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text("MANAGE SUBSCRIPTION")
            }
        }
    } else {
        LingerCard(color = MaterialTheme.colorScheme.inverseSurface) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "PINGLET PLUS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.weight(1f))
                Icon(Icons.Rounded.WorkspacePremium, null, tint = MaterialTheme.colorScheme.secondary)
            }
            Text(
                "Keep everything worth remembering.",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.inverseOnSurface,
            )
            Text(
                "Unlimited personal saves · 30 AI social imports each month",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = .76f),
            )
            Button(
                onClick = if (entitlement?.isAnonymous == false) onUpgrade else onCreateAccount,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
            ) {
                Text(if (entitlement?.isAnonymous == false) "EXPLORE PLUS" else "CREATE ACCOUNT TO UPGRADE")
            }
        }
    }
}

@Composable
private fun UsageMeter(label: String, used: Int, limit: Int?, detail: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(detail, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { if (limit == null) 1f else (used.toFloat() / limit.coerceAtLeast(1)).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = if (limit != null && used >= limit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    detail: String,
    trailing: String? = null,
    onClick: (() -> Unit)? = null,
) {
    LingerCard(modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (trailing != null) StatusPill(trailing)
            if (onClick != null) Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
