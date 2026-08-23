package com.linger.app.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerPage
import com.linger.app.ui.components.SectionLabel
import com.linger.app.ui.theme.LingerGold

private enum class LibraryFilter { ALL, FAVORITES }

@Composable
fun LibraryScreen(
    onOpenAdd: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenContent: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var filter by rememberSaveable { mutableStateOf(LibraryFilter.ALL) }
    LaunchedEffect(Unit) { viewModel.refresh() }
    val visibleItems = if (filter == LibraryFilter.FAVORITES) state.items.filter { it.favorite } else state.items

    LingerPage("Your library", "Everything you chose to keep.", "Personal saves live here and take priority in your rotation.") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            FilterChip(filter == LibraryFilter.ALL, { filter = LibraryFilter.ALL }, { Text("All saves") })
            FilterChip(
                selected = filter == LibraryFilter.FAVORITES,
                onClick = { filter = LibraryFilter.FAVORITES },
                label = { Text("Favorites") },
                leadingIcon = { Icon(Icons.Rounded.Favorite, null, Modifier.size(17.dp)) },
            )
        }
        FilledTonalButton(onClick = onOpenQueue, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Rounded.Schedule, null)
            Spacer(Modifier.width(9.dp))
            Text("PROCESSING QUEUE", style = MaterialTheme.typography.labelLarge)
        }
        if (state.loading) LingerCard(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondaryContainer) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                Text("Loading your saves...", style = MaterialTheme.typography.titleMedium)
            }
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        if (!state.loading && visibleItems.isEmpty()) {
            LingerCard(Modifier.fillMaxWidth(), color = if (filter == LibraryFilter.FAVORITES) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer) {
                Text(if (filter == LibraryFilter.FAVORITES) "No favorites yet" else "Your library is ready", style = MaterialTheme.typography.headlineMedium)
                Text(
                    if (filter == LibraryFilter.FAVORITES) "Tap the heart on a quote or widget to keep it here."
                    else "Write a thought or share a public social post to PingLet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (filter == LibraryFilter.ALL) Button(onClick = onOpenAdd) { Text("ADD YOUR FIRST SAVE", style = MaterialTheme.typography.labelLarge) }
            }
        }
        if (visibleItems.isNotEmpty()) {
            SectionLabel(if (filter == LibraryFilter.FAVORITES) "Favorites" else "Your saves", "tap to open")
            visibleItems.forEach { item ->
                LingerCard(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenContent(item.contentItemId) },
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(item.type, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                        IconButton(
                            onClick = { viewModel.toggleFavorite(item.contentItemId) },
                            enabled = item.contentItemId !in state.updatingFavoriteIds,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                if (item.favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                if (item.favorite) "Remove from favorites" else "Add to favorites",
                                tint = if (item.favorite) LingerGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(21.dp),
                            )
                        }
                    }
                    Text(
                        item.text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 17.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        maxLines = 5,
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.author?.takeIf { it.isNotBlank() } ?: "Saved by you", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!item.sourceUrl.isNullOrBlank()) Text("SOURCE ATTACHED", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
            OutlinedButton(onClick = onOpenAdd, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Text("ADD ANOTHER SAVE", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
