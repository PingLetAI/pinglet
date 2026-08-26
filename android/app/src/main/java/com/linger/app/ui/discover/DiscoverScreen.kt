package com.linger.app.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.data.remote.CatalogResponse
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerLazyPage
import com.linger.app.ui.components.SectionLabel
import com.linger.app.ui.components.StatusPill
import com.linger.app.ui.theme.LingerGold

@Composable
fun DiscoverScreen(
    onOpenCatalog: (String) -> Unit,
    onOpenPreferences: () -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    LingerLazyPage(
        eyebrow = "Explore",
        title = "Ideas beyond your saves.",
        subtitle = "Curated collections fill the quiet spaces. Your personal PingLets always come first.",
        headerAction = { FilledTonalIconButton(onClick = onOpenPreferences) { Icon(Icons.Rounded.Tune, "Collection preferences") } },
    ) {
        if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.error?.let { message -> item {
            LingerCard {
                Text("Explore could not be loaded", style = MaterialTheme.typography.titleLarge)
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = viewModel::refresh) { Text("TRY AGAIN") }
            }
        } }
        if (!state.loading && state.catalogs.isEmpty() && state.error == null) item {
            LingerCard(color = MaterialTheme.colorScheme.secondaryContainer) {
                Text("New collections are being prepared.", style = MaterialTheme.typography.titleLarge)
                Text("Your own saves will continue rotating normally.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        state.catalogs.firstOrNull()?.let { featured -> item { FeaturedCatalog(featured) { onOpenCatalog(featured.id) } } }
        if (state.catalogs.size > 1) item { SectionLabel("BROWSE COLLECTIONS", state.catalogs.size.toString() + " available") }
        items(state.catalogs.drop(1).size, key = { state.catalogs.drop(1)[it].id }) { index ->
            val catalog = state.catalogs.drop(1)[index]
            CatalogCard(catalog) { onOpenCatalog(catalog.id) }
        }
    }
}

@Composable
private fun FeaturedCatalog(catalog: CatalogResponse, onClick: () -> Unit) {
    LingerCard(Modifier.fillMaxWidth().clickable(onClick = onClick), dark = true) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = LingerGold)
                Spacer(Modifier.width(9.dp))
                Text("FEATURED COLLECTION", style = MaterialTheme.typography.labelLarge, color = LingerGold)
            }
            StatusPill(if (catalog.enabled) "Included" else "Paused")
        }
        Spacer(Modifier.height(10.dp))
        Text(catalog.name, style = MaterialTheme.typography.displaySmall)
        Text(catalog.description ?: "A collection of thoughts selected to stay with you.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC7C4BA))
        catalog.previewItems.firstOrNull()?.let { preview -> Text("“" + preview.text + "”", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 10.dp)) }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(catalog.itemCount.toString() + " PINGLETS", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = Color(0xFFB8B4AA))
            Text("EXPLORE", style = MaterialTheme.typography.labelLarge, color = LingerGold)
            Spacer(Modifier.width(5.dp))
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(18.dp), tint = LingerGold)
        }
    }
}

@Composable
private fun CatalogCard(catalog: CatalogResponse, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(9.dp).background(if (catalog.enabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline, RoundedCornerShape(50)))
                Spacer(Modifier.width(10.dp))
                Text(catalog.name, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(catalog.description ?: "A curated PingLet collection.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            catalog.previewItems.firstOrNull()?.let { Text("“" + it.text + "”", style = MaterialTheme.typography.bodyLarge, maxLines = 2) }
            Row {
                Text(catalog.itemCount.toString() + " PINGLETS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.weight(1f))
                Text(if (catalog.enabled) "IN YOUR ROTATION" else "PAUSED", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
