package com.linger.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.linger.app.data.local.db.DatabaseProvider

private data class UpcomingContent(val id: String, val text: String, val author: String?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingScreen(onBack: () -> Unit, onOpenContent: (String) -> Unit) {
    val context = LocalContext.current.applicationContext
    var content by remember { mutableStateOf<List<UpcomingContent>>(emptyList()) }
    LaunchedEffect(Unit) {
        val dao = DatabaseProvider.database(context).contentDao()
        content = dao.queue(200).mapNotNull { queued ->
            dao.contentById(queued.contentItemId)?.let { UpcomingContent(it.id, cleanQueueText(it.text), it.author) }
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Coming up", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = .28f))))) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Text("READY OFFLINE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary) }
                items(content, key = { it.id }) { item ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenContent(item.id) },
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                    ) {
                        Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(13.dp), verticalAlignment = Alignment.Top) {
                            Box(Modifier.padding(top = 7.dp).size(7.dp).background(MaterialTheme.colorScheme.secondary, CircleShape))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(item.text, style = MaterialTheme.typography.bodyLarge)
                                item.author?.takeIf { it.isNotBlank() }?.let {
                                    Text(it.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val queueTrailingCount = Regex("""([.!?])\s+(?:\d{1,3}(?:[,.]\d{3})*|\d+(?:\.\d+)?[KkMmBb])\s*$""")
private fun cleanQueueText(text: String) = text.trim().replace(queueTrailingCount) { it.groupValues[1] }
