package com.linger.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.local.db.DatabaseProvider
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerPage
import com.linger.app.ui.components.StatusPill
import com.linger.app.ui.theme.LingerGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlin.math.max

private data class HomeState(val currentId: String = "", val message: String = "Your next thought is finding its place.", val author: String? = null, val remaining: Long = 30, val interval: Int = 30, val upcoming: List<HomeItem> = emptyList(), val hasMore: Boolean = false, val trialHoursRemaining: Long? = null)
private data class HomeItem(val id: String, val text: String)

@Composable
fun HomeScreen(onOpenContent: (String) -> Unit, onOpenUpcoming: () -> Unit, onUpgrade: () -> Unit = {}) {
    val context = LocalContext.current.applicationContext
    var state by remember { mutableStateOf(HomeState()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            val store = DataStoreManager(context)
            val dao = DatabaseProvider.database(context).contentDao()
            val currentId = store.readLastDisplayedContentId()
            val queued = dao.queue(200)
                .mapNotNull { queued -> dao.contentById(queued.contentItemId)?.let { HomeItem(it.id, it.text) } }
                .filterNot { it.id == currentId }
            val nextAt = store.readLastDisplayedNextChangeAt()
            val entitlementExpiresAt = store.readEntitlementExpiresAt()
            val trialHoursRemaining = if (store.readEntitlementSource() == "TRIAL" && entitlementExpiresAt > System.currentTimeMillis()) {
                ((entitlementExpiresAt - System.currentTimeMillis()) / 3_600_000L).coerceAtLeast(1L)
            } else null
            state = HomeState(
                currentId = currentId,
                message = store.readLastDisplayedContentText().ifBlank { state.message },
                author = store.readLastDisplayedContentAuthor().ifBlank { null },
                remaining = max(0L, (nextAt - System.currentTimeMillis()) / 60_000),
                interval = store.refreshMinutes().firstOrNull() ?: 30,
                upcoming = queued.take(5),
                hasMore = queued.size > 5,
                trialHoursRemaining = trialHoursRemaining,
            )
            delay(15_000)
        }
    }

    LingerPage("Today", "One good thought, kept close.", "Your personal saves lead. PingLet fills the gaps quietly.") {
        LingerCard(Modifier.fillMaxWidth().clickable(enabled = state.currentId.isNotBlank()) { onOpenContent(state.currentId) }, dark = true) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                StatusPill("On your widget")
                Text(if (state.remaining <= 1) "CHANGING SOON" else "ABOUT ${state.remaining} MIN", style = MaterialTheme.typography.labelMedium, color = Color(0xFFB8B4AA))
            }
            Spacer(Modifier.height(10.dp))
            Text(cleanDisplayText(state.message), style = MaterialTheme.typography.headlineMedium)
            state.author?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Color(0xFFB8B4AA)) }
            Box(Modifier.padding(top = 7.dp).width(36.dp).height(3.dp).background(LingerGold, RoundedCornerShape(4.dp)))
        }

        state.trialHoursRemaining?.takeIf { it <= 48L }?.let { hours ->
            LingerCard(color = MaterialTheme.colorScheme.secondaryContainer) {
                Text("Keep your PingLet Plus features", style = MaterialTheme.typography.titleLarge)
                Text(if (hours <= 24L) "Your free Plus access ends tomorrow." else "Your free Plus access ends in 2 days.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("You will not be charged automatically.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) { Text("KEEP PINGLET PLUS") }
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Coming up", style = MaterialTheme.typography.titleLarge)
                Text("Ready offline · every ${state.interval} minutes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (state.hasMore) TextButton(onClick = onOpenUpcoming) { Text("SEE ALL"); Spacer(Modifier.width(4.dp)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(17.dp)) }
        }

        if (state.upcoming.isEmpty()) {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .62f)) {
                Text("Share a post or tap + to build your rotation.", Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column {
                    state.upcoming.forEachIndexed { index, item ->
                        Row(Modifier.fillMaxWidth().clickable { onOpenContent(item.id) }.padding(horizontal = 16.dp, vertical = 13.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                            Text("${index + 1}".padStart(2, '0'), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 2.dp))
                            Text(cleanDisplayText(item.text), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                        }
                        if (index < state.upcoming.lastIndex) HorizontalDivider(Modifier.padding(start = 34.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .65f))
                    }
                }
            }
        }
    }
}

private val trailingCount = Regex("""([.!?])\s+(?:\d{1,3}(?:[,.]\d{3})*|\d+(?:\.\d+)?[KkMmBb])\s*$""")
private fun cleanDisplayText(text: String) = text.trim().replace(trailingCount) { it.groupValues[1] }
