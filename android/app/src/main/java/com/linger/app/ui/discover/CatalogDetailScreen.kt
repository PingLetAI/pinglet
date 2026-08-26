package com.linger.app.ui.discover

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerLazyPage
import com.linger.app.ui.components.SectionLabel

@Composable
fun CatalogDetailScreen(onBack: () -> Unit, viewModel: CatalogDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val catalog = state.catalog
    val uriHandler = LocalUriHandler.current
    var menuItemId by remember { mutableStateOf<String?>(null) }
    var reportItemId by remember { mutableStateOf<String?>(null) }
    LingerLazyPage("Collection", catalog?.name ?: "Loading collection", catalog?.description, onBack) {
        if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.error?.let { item { LingerCard { Text(it, color = MaterialTheme.colorScheme.error); TextButton(viewModel::refresh) { Text("TRY AGAIN") } } } }
        state.notice?.let { notice -> item { Text(notice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary) } }
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
                    Row(Modifier.fillMaxWidth()) {
                        Text((index + 1).toString().padStart(2, '0'), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                        Box {
                            IconButton(
                                onClick = { menuItemId = item.id },
                                enabled = state.actingOnItemId == null,
                                modifier = Modifier.size(32.dp),
                            ) { Icon(Icons.Rounded.MoreVert, "More options") }
                            DropdownMenu(expanded = menuItemId == item.id, onDismissRequest = { menuItemId = null }) {
                                DropdownMenuItem(
                                    text = { Text("Report this PingLet") },
                                    leadingIcon = { Icon(Icons.Rounded.Flag, null) },
                                    onClick = { menuItemId = null; reportItemId = item.id },
                                )
                                DropdownMenuItem(
                                    text = { Text("Hide this source") },
                                    leadingIcon = { Icon(Icons.Rounded.VisibilityOff, null) },
                                    onClick = { menuItemId = null; viewModel.hideSource(item.id) },
                                )
                            }
                        }
                    }
                    Text(item.text, style = MaterialTheme.typography.bodyLarge)
                    item.author?.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    item.sourceUrl?.takeIf(String::isNotBlank)?.let { sourceUrl ->
                        TextButton(onClick = { runCatching { uriHandler.openUri(sourceUrl) } }) {
                            Icon(Icons.Rounded.OpenInNew, null)
                            Spacer(Modifier.width(8.dp))
                            Text("VIEW ORIGINAL SOURCE")
                        }
                    }
                }
            }
        }
    }

    reportItemId?.let { contentItemId ->
        AlertDialog(
            onDismissRequest = { reportItemId = null },
            title = { Text("Report this PingLet") },
            text = {
                Column {
                    listOf(
                        "UNSAFE" to "Inappropriate or unsafe",
                        "MISLEADING_SPAM" to "Misleading or spam",
                        "PRIVACY_RIGHTS" to "Privacy or rights concern",
                        "OTHER" to "Other",
                    ).forEach { (reason, label) ->
                        TextButton(
                            onClick = { reportItemId = null; viewModel.report(contentItemId, reason) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(label, Modifier.fillMaxWidth()) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { reportItemId = null }) { Text("CANCEL") } },
        )
    }
}
