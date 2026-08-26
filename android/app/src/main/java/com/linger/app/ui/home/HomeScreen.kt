package com.linger.app.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.local.db.DatabaseProvider
import com.linger.app.data.repository.FavoriteRepository
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerPage
import com.linger.app.ui.components.StatusPill
import com.linger.app.ui.theme.LingerGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

private data class HomeState(val currentId: String = "", val message: String = "Your next thought is finding its place.", val author: String? = null, val sourceUrl: String? = null, val favorite: Boolean = false, val remaining: Long = 30, val interval: Int = 30, val upcoming: List<HomeItem> = emptyList(), val trialHoursRemaining: Long? = null, val paidPlansEnabled: Boolean = false)
private data class HomeItem(val id: String, val text: String, val author: String?, val sourceUrl: String?, val type: String)

@Composable
fun HomeScreen(onOpenContent: (String) -> Unit, onOpenUpcoming: () -> Unit, onUpgrade: () -> Unit = {}) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf(HomeState()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            val store = DataStoreManager(context)
            val dao = DatabaseProvider.database(context).contentDao()
            val currentId = store.readLastDisplayedContentId()
            val queued = dao.queue(200)
                .mapNotNull { queued -> dao.contentById(queued.contentItemId)?.let { HomeItem(it.id, it.text, it.author, it.sourceUrl, it.type) } }
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
                sourceUrl = store.readLastDisplayedSourceUrl().ifBlank { null },
                favorite = store.readLastDisplayedFavorite(),
                remaining = max(0L, (nextAt - System.currentTimeMillis()) / 60_000),
                interval = store.refreshMinutes().firstOrNull() ?: 30,
                upcoming = queued.take(5),
                trialHoursRemaining = trialHoursRemaining,
                paidPlansEnabled = store.readPaidPlansEnabled(),
            )
            delay(15_000)
        }
    }

    LingerPage("Today", "One good thought, kept close.", "Your personal saves lead. PingLet fills the gaps quietly.") {
        val currentTextStyle = when {
            state.message.length > 380 -> MaterialTheme.typography.titleLarge
            state.message.length > 220 -> MaterialTheme.typography.headlineSmall
            else -> MaterialTheme.typography.headlineMedium
        }
        LingerCard(Modifier.fillMaxWidth().clickable(enabled = state.currentId.isNotBlank()) { onOpenContent(state.currentId) }, dark = true) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                StatusPill("On your widget")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (state.remaining <= 1) "CHANGING SOON" else "ABOUT ${state.remaining} MIN", style = MaterialTheme.typography.labelMedium, color = Color(0xFFB8B4AA))
                    if (state.currentId.isNotBlank()) IconButton(onClick = {
                        val target = !state.favorite
                        state = state.copy(favorite = target)
                        scope.launch {
                            if (runCatching { FavoriteRepository.setFavorite(context, state.currentId, target) }.isFailure) state = state.copy(favorite = !target)
                        }
                    }, modifier = Modifier.size(36.dp)) {
                        Icon(if (state.favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, if (state.favorite) "Remove favorite" else "Favorite", tint = if (state.favorite) LingerGold else Color(0xFFB8B4AA), modifier = Modifier.size(21.dp))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(cleanDisplayText(state.message), style = currentTextStyle)
            state.author?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Color(0xFFB8B4AA)) }
            Row(Modifier.fillMaxWidth().padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(36.dp).height(3.dp).background(LingerGold, RoundedCornerShape(4.dp)))
                state.sourceUrl?.let { url ->
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD7CFAF))) {
                        Icon(Icons.Rounded.Link, null, Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(sourceName(url), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        state.trialHoursRemaining?.takeIf { it <= 48L }?.let { hours ->
            LingerCard(color = MaterialTheme.colorScheme.secondaryContainer) {
                Text("Keep your PingLet Plus features", style = MaterialTheme.typography.titleLarge)
                Text(if (hours <= 24L) "Your free Plus access ends tomorrow." else "Your free Plus access ends in 2 days.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("You will not be charged automatically.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (state.paidPlansEnabled) Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) { Text("KEEP PINGLET PLUS") }
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Coming up", style = MaterialTheme.typography.titleLarge)
                Text("Ready offline · every ${state.interval} minutes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (state.upcoming.isNotEmpty()) TextButton(onClick = onOpenUpcoming) { Text("SEE ALL"); Spacer(Modifier.width(4.dp)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(17.dp)) }
        }

        if (state.upcoming.isEmpty()) {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .62f)) {
                Text("Share a post or tap + to build your rotation.", Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface.copy(alpha = .84f), tonalElevation = 1.dp, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .62f))) {
                Column {
                    state.upcoming.forEachIndexed { index, item ->
                        Row(Modifier.fillMaxWidth().defaultMinSize(minHeight = 82.dp).clickable { onOpenContent(item.id) }.padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(11.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(10.dp), color = if (index == 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)) {
                                Icon(Icons.Rounded.Link, null, Modifier.padding(9.dp).size(17.dp), tint = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(if (index == 0) "NEXT · ABOUT ${state.remaining.coerceAtLeast(1)} MIN" else itemMeta(item), style = MaterialTheme.typography.labelSmall, color = if (index == 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                Text(cleanDisplayText(item.text), style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                                if (index == 0 && itemMeta(item).isNotBlank()) Text(itemMeta(item), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Open", Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (index < state.upcoming.lastIndex) HorizontalDivider(Modifier.padding(start = 58.dp, end = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .48f))
                    }
                }
            }
        }
    }
}

private val trailingCount = Regex("""([.!?])\s+(?:\d{1,3}(?:[,.]\d{3})*|\d+(?:\.\d+)?[KkMmBb])\s*$""")
private fun cleanDisplayText(text: String) = text.trim().replace(trailingCount) { it.groupValues[1] }
private fun sourceName(url: String): String = when {
    "instagram.com" in url -> "Instagram"
    "tiktok.com" in url -> "TikTok"
    "facebook.com" in url || "fb.watch" in url -> "Facebook"
    "youtube.com" in url || "youtu.be" in url -> "YouTube"
    else -> url.substringAfter("://", url).substringBefore('/').removePrefix("www.").ifBlank { "Source" }
}
private fun itemMeta(item: HomeItem): String = item.author?.takeIf { it.isNotBlank() }
    ?: item.sourceUrl?.let(::sourceName)
    ?: item.type.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercase)
