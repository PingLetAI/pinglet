package com.linger.app.ui.discover

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerLazyPage
import com.linger.app.ui.components.SectionLabel

@Composable
fun CatalogDetailScreen(onBack: () -> Unit, viewModel: CatalogDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val catalog = state.catalog
    LingerLazyPage("Collection", catalog?.name ?: "Loading collection", catalog?.description, onBack) {
        if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.error?.let { item { LingerCard { Text(it, color = MaterialTheme.colorScheme.error); TextButton(viewModel::refresh) { Text("TRY AGAIN") } } } }
        catalog?.let { current ->
            item {
                LingerCard(color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(if (current.enabled) "Included in your rotation" else "Paused", style = MaterialTheme.typography.titleLarge)
                    Text(if (current.enabled) "PingLets from this collection can fill spaces after your personal saves." else "This collection will stay out of your rotation until you include it again.", style = MaterialTheme.typography.bodyMedium)
                    Button(viewModel::toggle, Modifier.fillMaxWidth(), enabled = !state.updating) {
                        Icon(if (current.enabled) Icons.Rounded.PauseCircle else Icons.Rounded.CheckCircle, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (current.enabled) "PAUSE COLLECTION" else "INCLUDE IN ROTATION")
                    }
                }
            }
            item { SectionLabel("IN THIS COLLECTION", current.itemCount.toString() + " PingLets") }
            items(current.items.size, key = { current.items[it].id }) { index ->
                val item = current.items[index]
                LingerCard {
                    Text((index + 1).toString().padStart(2, '0'), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                    Text(item.text, style = MaterialTheme.typography.bodyLarge)
                    item.author?.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}
