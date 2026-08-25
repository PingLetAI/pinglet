package com.linger.app.ui.queue

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.data.remote.IngestUrlResponse
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerLazyPage

private enum class QueueFilter { PROCESSING, READY, FAILED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingQueueScreen(onBack: () -> Unit, viewModel: ProcessingQueueViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var filter by rememberSaveable { mutableStateOf(QueueFilter.PROCESSING) }
    val visible = state.items.filter { item -> when (filter) {
        QueueFilter.PROCESSING -> item.status in listOf("RECEIVED", "PROCESSING")
        QueueFilter.READY -> item.status == "READY"
        QueueFilter.FAILED -> item.status in listOf("FAILED", "REJECTED")
    } }
    LingerLazyPage("Queue", "Working quietly.", "Leave whenever you like. PingLet will keep reading in the background.", onBack) {
        item {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                QueueFilter.entries.forEachIndexed { index, option ->
                    SegmentedButton(filter == option, { filter = option }, SegmentedButtonDefaults.itemShape(index, QueueFilter.entries.size), label = { Text(option.name.lowercase().replaceFirstChar(Char::uppercase)) }, icon = {})
                }
            }
        }
        if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.error?.let { item { Text("Queue unavailable. It will update when your connection returns.", color = MaterialTheme.colorScheme.error) } }
        if (!state.loading && visible.isEmpty()) item {
            LingerCard(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .68f)) {
                Text(when (filter) { QueueFilter.PROCESSING -> "All caught up"; QueueFilter.READY -> "Nothing ready yet"; QueueFilter.FAILED -> "No saves need attention" }, style = MaterialTheme.typography.headlineSmall)
                Text(when (filter) { QueueFilter.PROCESSING -> "New shared posts appear here while PingLet reads them."; QueueFilter.READY -> "Finished imports appear here before joining your library."; QueueFilter.FAILED -> "If a public post cannot be accessed, it will appear here." }, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(visible.size, key = { visible[it].id }) { QueueRow(visible[it]) }
    }
}

@Composable
private fun QueueRow(item: IngestUrlResponse) {
    val active = item.status in listOf("RECEIVED", "PROCESSING")
    val icon = when { active -> Icons.Rounded.HourglassTop; item.status == "READY" -> Icons.Rounded.CheckCircle; else -> Icons.Rounded.ErrorOutline }
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(13.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = if (item.status in listOf("FAILED", "REJECTED")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(friendlyStage(item), style = MaterialTheme.typography.titleMedium)
                Text(item.contentItem?.text ?: item.takeaways?.firstOrNull()?.text ?: item.caption ?: "Reading the words, images, and audio in this post.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 4)
                item.errorMessage?.let { Text(friendlyError(item.errorCode), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

private fun friendlyStage(item: IngestUrlResponse) = when (item.processingStage) {
    "QUEUED" -> "Waiting to start"
    "TRANSCRIBING_SPEECH" -> "Transcribing the video"
    "DERIVING_TAKEAWAYS" -> "Finding what is worth keeping"
    "READY", "REUSED_EXISTING_EXTRACTION" -> "Ready for your library"
    "REJECTED" -> "Could not be added"
    "FAILED", "QUEUE_FAILED" -> "Needs attention"
    else -> if (item.status == "PROCESSING") "Reading the post" else item.status.lowercase().replaceFirstChar(Char::uppercase)
}

private fun friendlyError(code: String?) = when (code) {
    "PUBLIC_MEDIA_UNAVAILABLE" -> "The post is private, unavailable, or requires a social-media login."
    "VIDEO_MEDIA_UNUSABLE" -> "No usable speech or visible text could be found."
    else -> "PingLet could not process this post. You can try sharing it again later."
}
