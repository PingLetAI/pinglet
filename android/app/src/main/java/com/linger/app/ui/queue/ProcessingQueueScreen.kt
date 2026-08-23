package com.linger.app.ui.queue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.data.remote.IngestUrlResponse
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerPage
import com.linger.app.ui.components.StatusPill

private enum class QueueFilter { PROCESSING, READY, FAILED }

@Composable
fun ProcessingQueueScreen(onBack: () -> Unit, viewModel: ProcessingQueueViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var filter by rememberSaveable { mutableStateOf(QueueFilter.PROCESSING) }
    val visibleItems = state.items.filter { item ->
        when (filter) {
            QueueFilter.PROCESSING -> item.status == "RECEIVED" || item.status == "PROCESSING"
            QueueFilter.READY -> item.status == "READY"
            QueueFilter.FAILED -> item.status == "FAILED" || item.status == "REJECTED"
        }
    }
    LingerPage("Processing queue", "Working quietly.", "You can leave this screen while Linger reads each public post in the background.", onBack = onBack) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QueueFilter.entries.forEach { option ->
                FilterChip(
                    selected = filter == option,
                    onClick = { filter = option },
                    label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                )
            }
        }
        if (state.loading) LingerCard(color = MaterialTheme.colorScheme.secondaryContainer) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                Text("Checking your saves...", style = MaterialTheme.typography.titleMedium)
            }
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (!state.loading && visibleItems.isEmpty()) {
            LingerCard(color = when (filter) {
                QueueFilter.FAILED -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            }) {
                Text(
                    when (filter) {
                        QueueFilter.PROCESSING -> "Nothing processing"
                        QueueFilter.READY -> "Nothing ready yet"
                        QueueFilter.FAILED -> "No failed saves"
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    when (filter) {
                        QueueFilter.PROCESSING -> "New social saves appear here while they are being read."
                        QueueFilter.READY -> "Completed social saves will remain available here."
                        QueueFilter.FAILED -> "Posts Linger could not access or process will appear here."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        visibleItems.forEach { QueueItem(it) }
    }
}

@Composable
private fun QueueItem(item: IngestUrlResponse) {
    val active = item.status == "RECEIVED" || item.status == "PROCESSING"
    LingerCard(color = if (active) MaterialTheme.colorScheme.secondaryContainer else null) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(item.processingStage.orEmpty().replace('_', ' ').ifBlank { item.status }, style = MaterialTheme.typography.labelLarge)
            StatusPill(item.status)
        }
        Text(
            item.contentItem?.text ?: item.takeaways?.firstOrNull()?.text ?: item.caption ?: "Analyzing public post content...",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
        item.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        item.extractionConfidence?.let { Text("${(it * 100).toInt()}% extraction confidence", style = MaterialTheme.typography.bodyMedium) }
    }
}
