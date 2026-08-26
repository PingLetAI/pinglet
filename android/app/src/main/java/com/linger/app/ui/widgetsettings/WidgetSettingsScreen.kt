package com.linger.app.ui.widgetsettings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerPage
import com.linger.app.ui.components.SectionLabel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WidgetSettingsScreen(onBack: () -> Unit, onTryPlus: () -> Unit, onUpgrade: () -> Unit, viewModel: WidgetSettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val profile = state.selectedKey?.let(state.profiles::get)
    LingerPage("Widgets", "A different rhythm for every space.", "Configure each installed Home Screen widget independently.", onBack) {
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (!state.loading && state.widgetKeys.isEmpty()) LingerCard {
            Icon(Icons.Rounded.Widgets, null)
            Text("No PingLet widgets installed", style = MaterialTheme.typography.titleLarge)
            Text("Add a PingLet widget from your Home Screen, then return here to personalize it.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (state.widgetKeys.size > 1) {
            SectionLabel("CHOOSE WIDGET")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.widgetKeys.forEachIndexed { index, key -> FilterChip(state.selectedKey == key, { viewModel.select(key) }, label = { Text("Widget ${index + 1}") }) }
            }
        }
        profile?.let { current ->
            if (!state.isPlus) LingerCard(color = MaterialTheme.colorScheme.secondaryContainer) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.AutoAwesome, null); Spacer(Modifier.width(9.dp)); Text("Make every widget your own", style = MaterialTheme.typography.titleLarge) }
                Text("Plus unlocks themes, independent content profiles, schedules, typography, spacing, and manual rotation.", style = MaterialTheme.typography.bodyMedium)
                when {
                    state.trialEligible -> Button(onTryPlus, Modifier.fillMaxWidth()) { Text("TRY PINGLET PLUS - 7 DAYS FREE") }
                    state.paidPlansEnabled -> Button(onUpgrade, Modifier.fillMaxWidth()) { Text("EXPLORE PINGLET PLUS") }
                    else -> Text("PingLet Plus subscriptions are coming soon.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            SectionLabel("FREE CONTROLS")
            OptionGroup("TEXT SCALE", listOf("SMALL", "MEDIUM", "LARGE"), current.textScale) { value -> viewModel.update(premium = false) { profile -> profile.copy(textScale = value) } }
            OptionGroup("TRANSLUCENCY", listOf("62", "78", "90"), current.opacity.toString(), labels = listOf("Light", "Blend", "Solid")) { value -> viewModel.update(premium = false) { it.copy(opacity = value.toInt()) } }

            SectionLabel("PLUS APPEARANCE")
            OptionGroup("SURFACE", listOf("BLEND", "INK", "FOREST", "CLAY"), current.theme, enabled = state.isPlus) { value -> viewModel.update { it.copy(theme = value) } }
            OptionGroup("TYPOGRAPHY", listOf("EDITORIAL", "CLEAN", "COMPACT"), current.typography, enabled = state.isPlus) { value -> viewModel.update { it.copy(typography = value) } }
            OptionGroup("SPACING", listOf("COMPACT", "COMFORTABLE", "AIRY"), current.spacing, enabled = state.isPlus) { value -> viewModel.update { it.copy(spacing = value) } }

            SectionLabel("PLUS CONTENT PROFILE")
            OptionGroup("SOURCE", listOf("MIXED", "PERSONAL", "COLLECTIONS"), current.contentMode, enabled = state.isPlus) { value -> viewModel.update { it.copy(contentMode = value) } }
            if (current.contentMode == "COLLECTIONS") {
                Text("COLLECTIONS", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.catalogs.forEach { catalog ->
                        FilterChip(
                            selected = catalog.id in current.catalogIds,
                            onClick = { viewModel.update { profile -> profile.copy(catalogIds = profile.catalogIds.toMutableSet().apply { if (!add(catalog.id)) remove(catalog.id) }) } },
                            enabled = state.isPlus,
                            label = { Text(catalog.name) },
                        )
                    }
                }
            }
            OptionGroup("SCHEDULE", listOf("ANYTIME", "DAY_RHYTHM", "CONTEXTUAL"), current.scheduleMode, labels = listOf("Anytime", "Morning + evening", "Contextual"), enabled = state.isPlus) { value -> viewModel.update { it.copy(scheduleMode = value) } }
            LingerCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Show another", style = MaterialTheme.typography.titleMedium); Text("Add an optional next control to this widget.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Switch(current.manualNext, { value -> viewModel.update { it.copy(manualNext = value) } }, enabled = state.isPlus)
                }
            }
        }
    }
}

@Composable
private fun OptionGroup(title: String, values: List<String>, selected: String, labels: List<String> = values.map { it.lowercase().replaceFirstChar(Char::uppercase) }, enabled: Boolean = true, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            values.forEachIndexed { index, value -> SegmentedButton(selected == value, { onSelect(value) }, SegmentedButtonDefaults.itemShape(index, values.size), enabled = enabled, label = { Text(labels[index]) }, icon = {}) }
        }
    }
}
