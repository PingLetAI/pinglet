package com.linger.app.ui.discover

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.data.remote.CatalogResponse
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerLazyPage

@Composable
fun DiscoverScreen(viewModel: DiscoverViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    LingerLazyPage("Explore", "Choose what finds you.", "Your saves always come first. Collections only fill quiet spaces in your rotation.") {
        item {
            LingerCard(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .72f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Curated, never noisy", style = MaterialTheme.typography.titleMedium)
                        Text("Turn collections on or off at any time.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.error?.let { message -> item {
            LingerCard {
                Text("Explore could not be loaded", style = MaterialTheme.typography.titleLarge)
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = viewModel::refresh) { Text("TRY AGAIN") }
            }
        } }
        if (!state.loading && state.catalogs.isEmpty() && state.error == null) item {
            Text("New collections are being prepared.", style = MaterialTheme.typography.bodyLarge)
        }
        items(state.catalogs.size, key = { state.catalogs[it].id }) { index ->
            val catalog = state.catalogs[index]
            CatalogRow(catalog, catalog.id in state.updatingIds) { viewModel.toggle(catalog.id) }
        }
    }
}

@Composable
private fun CatalogRow(catalog: CatalogResponse, updating: Boolean, onToggle: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(catalog.name, style = MaterialTheme.typography.titleLarge)
                Text(catalog.description ?: "A curated PingLet collection.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (catalog.enabled) "IN YOUR ROTATION" else "PAUSED", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
            }
            if (updating) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            else Switch(checked = catalog.enabled, onCheckedChange = { onToggle() })
        }
    }
}
