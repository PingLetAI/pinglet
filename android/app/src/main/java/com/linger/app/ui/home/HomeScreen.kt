package com.linger.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.local.db.DatabaseProvider
import com.linger.app.ui.components.*
import com.linger.app.ui.theme.LingerGold
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.max

private data class HomeState(
    val currentId: String = "", val message: String = "Your next thought is finding its place.", val author: String? = null,
    val remaining: Long = 30, val interval: Int = 30,
    val upcoming: List<HomeItem> = emptyList(), val hasMore: Boolean = false,
)
private data class HomeItem(val id: String, val text: String)

@Composable
fun HomeScreen(onOpenContent: (String) -> Unit, onOpenUpcoming: () -> Unit) {
    val context = LocalContext.current.applicationContext
    var state by remember { mutableStateOf(HomeState()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            val store = DataStoreManager(context)
            val dao = DatabaseProvider.database(context).contentDao()
            val nextAt = store.readLastDisplayedNextChangeAt()
            val currentId = store.readLastDisplayedContentId()
            val queued = dao.queue(200).mapNotNull { queueItem ->
                dao.contentById(queueItem.contentItemId)?.let { HomeItem(it.id, it.text) }
            }
            state = HomeState(
                currentId = currentId,
                message = store.readLastDisplayedContentText().ifBlank { state.message },
                author = store.readLastDisplayedContentAuthor().ifBlank { null },
                remaining = max(0L, (nextAt - System.currentTimeMillis()) / 60_000),
                interval = store.refreshMinutes().firstOrNull() ?: 30,
                upcoming = queued.take(3),
                hasMore = queued.size > 3,
            )
            delay(2_000)
        }
    }
    LingerPage("Today", "Keep one good thought close.") {
        LingerCard(
            Modifier.fillMaxWidth().clickable(enabled = state.currentId.isNotBlank()) { onOpenContent(state.currentId) },
            dark = true,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatusPill("Live on widget")
                Text(
                    if (state.remaining <= 1) "CHANGING SOON" else "ABOUT ${state.remaining}M",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFAAA99F),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(cleanDisplayText(state.message), style = MaterialTheme.typography.headlineMedium)
            state.author?.let {
                Text(it.uppercase(), style = MaterialTheme.typography.labelMedium, color = Color(0xFFB8B4AA))
            }
            Box(Modifier.padding(top = 8.dp).width(38.dp).height(3.dp).background(LingerGold, RoundedCornerShape(4.dp)))
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f),
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Personal thoughts first", style = MaterialTheme.typography.titleMedium)
                Text("EVERY ${state.interval} MIN", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("COMING UP", style = MaterialTheme.typography.labelLarge)
            if (state.hasMore) TextButton(onClick = onOpenUpcoming) {
                Text("SEE ALL", style = MaterialTheme.typography.labelMedium)
            } else Text("READY OFFLINE", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (state.upcoming.isEmpty()) {
            LingerCard(Modifier.fillMaxWidth()) {
                Text("Your next saves will appear here.", style = MaterialTheme.typography.titleMedium)
                Text("Use the center + button or share a post directly to PingLet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            state.upcoming.forEach { item ->
                Surface(Modifier.fillMaxWidth().clickable { onOpenContent(item.id) }, RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)) {
                    Row(Modifier.padding(17.dp), horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                        Box(Modifier.padding(top = 7.dp).size(7.dp).background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(50)))
                        Text(cleanDisplayText(item.text), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, maxLines = 2)
                    }
                }
            }
        }
    }
}

private val trailingCount = Regex("""([.!?])\s+(?:\d{1,3}(?:[,.]\d{3})*|\d+(?:\.\d+)?[KkMmBb])\s*$""")
private fun cleanDisplayText(text: String): String = text.trim().replace(trailingCount) { it.groupValues[1] }
