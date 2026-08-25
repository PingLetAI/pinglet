package com.linger.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.linger.app.data.local.db.DatabaseProvider
import com.linger.app.ui.components.LingerLazyPage

private data class UpcomingContent(val id: String, val text: String, val author: String?)

@Composable
fun UpcomingScreen(onBack: () -> Unit, onOpenContent: (String) -> Unit) {
    val context = LocalContext.current.applicationContext
    var content by remember { mutableStateOf<List<UpcomingContent>>(emptyList()) }
    LaunchedEffect(Unit) {
        val dao = DatabaseProvider.database(context).contentDao()
        content = dao.queue(200).mapNotNull { queued -> dao.contentById(queued.contentItemId)?.let { UpcomingContent(it.id, cleanQueueText(it.text), it.author) } }
    }
    LingerLazyPage("Coming up", "Ready when the moment arrives.", "These PingLets are cached on your phone and can rotate without a connection.", onBack) {
        if (content.isEmpty()) item {
            Text("Your next saves will appear here.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(content.size, key = { content[it].id }) { index ->
            val item = content[index]
            Surface(Modifier.fillMaxWidth().clickable { onOpenContent(item.id) }, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Surface(Modifier.padding(top = 8.dp).size(6.dp), CircleShape, MaterialTheme.colorScheme.secondary) {}
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(item.text, style = MaterialTheme.typography.bodyLarge)
                        item.author?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
}

private val queueTrailingCount = Regex("""([.!?])\s+(?:\d{1,3}(?:[,.]\d{3})*|\d+(?:\.\d+)?[KkMmBb])\s*$""")
private fun cleanQueueText(text: String) = text.trim().replace(queueTrailingCount) { it.groupValues[1] }
