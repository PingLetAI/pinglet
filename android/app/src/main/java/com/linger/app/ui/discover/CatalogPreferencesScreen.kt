package com.linger.app.ui.discover

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerLazyPage

@Composable
fun CatalogPreferencesScreen(onBack: () -> Unit, viewModel: DiscoverViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    LingerLazyPage("Explore", "Collection preferences", "Choose which curated collections can appear after your personal saves.", onBack) {
        if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        items(state.catalogs.size, key = { state.catalogs[it].id }) { index ->
            val catalog = state.catalogs[index]
            LingerCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(catalog.name, style = MaterialTheme.typography.titleLarge)
                        Text(catalog.description ?: "A curated PingLet collection.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (catalog.id in state.updatingIds) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    else Switch(catalog.enabled, { viewModel.toggle(catalog.id) })
                }
            }
        }
    }
}
