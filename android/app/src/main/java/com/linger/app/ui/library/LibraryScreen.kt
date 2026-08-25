package com.linger.app.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerLazyPage
import com.linger.app.ui.theme.LingerGold

private enum class LibraryFilter { ALL, FAVORITES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenAdd: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenContent: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var filter by rememberSaveable { mutableStateOf(LibraryFilter.ALL) }
    var query by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) { viewModel.refresh() }
    val filtered = if (filter == LibraryFilter.FAVORITES) state.items.filter { it.favorite } else state.items
    val visible = filtered.filter { query.isBlank() || it.text.contains(query, true) || it.author?.contains(query, true) == true }

    LingerLazyPage("Library", "Everything you kept.", "Personal saves live here and lead your rotation.") {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SingleChoiceSegmentedButtonRow(Modifier.weight(1f)) {
                    LibraryFilter.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = filter == option,
                            onClick = { filter = option },
                            shape = SegmentedButtonDefaults.itemShape(index, LibraryFilter.entries.size),
                            label = { Text(if (option == LibraryFilter.ALL) "All saves" else "Favorites") },
                            icon = {},
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                FilledTonalIconButton(onClick = onOpenQueue) { Icon(Icons.Rounded.Schedule, "Processing queue") }
            }
        }
        if (state.items.isNotEmpty()) item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search your PingLets") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                trailingIcon = { if (query.isNotBlank()) IconButton({ query = "" }) { Icon(Icons.Rounded.Close, "Clear search") } },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
            )
        }
        if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.error?.let { message -> item {
            LingerCard {
                Text("Your library could not be loaded", style = MaterialTheme.typography.titleLarge)
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = viewModel::refresh) { Text("TRY AGAIN") }
            }
        } }
        if (!state.loading && visible.isEmpty()) item {
            LingerCard(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .7f)) {
                Text(if (filter == LibraryFilter.FAVORITES) "Nothing favorited yet" else "Start your library", style = MaterialTheme.typography.headlineSmall)
                Text(if (filter == LibraryFilter.FAVORITES) "Use the heart on any PingLet to keep it close." else "Write a thought or share a public post from another app.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (filter == LibraryFilter.ALL) Button(onClick = onOpenAdd) { Text("ADD A PINGLET") }
            }
        }
        items(visible.size, key = { visible[it].contentItemId }) { index ->
            val item = visible[index]
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onOpenContent(item.contentItemId) },
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 10.dp, bottom = 15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(item.type.replace('_', ' '), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                        IconButton(
                            onClick = { viewModel.toggleFavorite(item.contentItemId) },
                            enabled = item.contentItemId !in state.updatingFavoriteIds,
                        ) {
                            Icon(if (item.favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, if (item.favorite) "Remove favorite" else "Favorite", tint = if (item.favorite) LingerGold else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(cleanLibraryText(item.text), style = MaterialTheme.typography.bodyLarge, maxLines = 5)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.author?.takeIf { it.isNotBlank() } ?: "Saved by you", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!item.sourceUrl.isNullOrBlank()) Text("VIEW SOURCE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
        if (visible.isNotEmpty()) item { OutlinedButton(onClick = onOpenAdd, modifier = Modifier.fillMaxWidth()) { Text("ADD ANOTHER") } }
    }
}

private val libraryTrailingCount = Regex("""([.!?])\s+(?:\d{1,3}(?:[,.]\d{3})*|\d+(?:\.\d+)?[KkMmBb])\s*$""")
private fun cleanLibraryText(text: String) = text.trim().replace(libraryTrailingCount) { it.groupValues[1] }
