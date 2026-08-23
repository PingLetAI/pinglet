package com.linger.app.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.local.db.DatabaseProvider
import com.linger.app.data.local.entity.ContentEntity
import com.linger.app.data.remote.ApiConfig
import com.linger.app.data.remote.RetrofitClient
import com.linger.app.data.repository.AuthRepositoryImpl
import com.linger.app.data.repository.SessionManager
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.StatusPill
import com.linger.app.ui.theme.LingerGold
import com.linger.app.widget.AmbientWidget
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentDetailScreen(contentId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()
    var item by remember(contentId) { mutableStateOf<ContentEntity?>(null) }
    var favorite by remember(contentId) { mutableStateOf(false) }
    var savingFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(contentId) {
        val store = DataStoreManager(appContext)
        item = DatabaseProvider.database(appContext).contentDao().contentById(contentId)
        favorite = if (store.readLastDisplayedContentId() == contentId) {
            store.readLastDisplayedFavorite()
        } else {
            val api = RetrofitClient.build(ApiConfig.apiBaseUrl())
            val session = SessionManager(AuthRepositoryImpl(api), store)
            runCatching {
                session.withAuthRetry { api.getMyContent() }.any { it.contentItemId == contentId && it.favorite }
            }.getOrDefault(false)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Thought", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
                actions = {
                    IconButton(
                        enabled = item != null && !savingFavorite,
                        onClick = {
                            val target = !favorite
                            favorite = target
                            savingFavorite = true
                            scope.launch {
                                val store = DataStoreManager(appContext)
                                val api = RetrofitClient.build(ApiConfig.apiBaseUrl())
                                val session = SessionManager(AuthRepositoryImpl(api), store)
                                val success = runCatching {
                                    session.withAuthRetry {
                                        if (target) api.favorite(contentId) else api.unfavorite(contentId)
                                    }
                                }.isSuccess
                                if (success && store.readLastDisplayedContentId() == contentId) {
                                    store.setLastDisplayedFavorite(target)
                                    AmbientWidget().updateAll(appContext)
                                } else if (!success) {
                                    favorite = !target
                                }
                                savingFavorite = false
                            }
                        },
                    ) {
                        Icon(
                            if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            if (favorite) "Remove from favorites" else "Add to favorites",
                            tint = if (favorite) LingerGold else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .34f))))) {
            item?.let { content ->
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    StatusPill(if (content.source == "PERSONAL") "Saved by you" else "From Linger")
                    LingerCard(Modifier.fillMaxWidth(), dark = true) {
                        Text(cleanDetailText(content.text), style = MaterialTheme.typography.headlineMedium)
                        content.author?.takeIf { it.isNotBlank() }?.let {
                            Text(it.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(Modifier.padding(top = 8.dp).width(38.dp).height(3.dp).background(LingerGold, RoundedCornerShape(4.dp)))
                    }
                    content.sourceUrl?.takeIf { it.isNotBlank() }?.let { sourceUrl ->
                        FilledTonalButton(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl)).addCategory(Intent.CATEGORY_BROWSABLE))
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(17.dp),
                        ) {
                            Icon(Icons.Rounded.OpenInNew, contentDescription = null)
                            Spacer(Modifier.width(9.dp))
                            Text("OPEN ORIGINAL SOURCE", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Text("This thought remains available offline and may return in a future rotation.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } ?: Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
        }
    }
}

private val detailTrailingCount = Regex("""([.!?])\s+(?:\d{1,3}(?:[,.]\d{3})*|\d+(?:\.\d+)?[KkMmBb])\s*$""")
private fun cleanDetailText(text: String) = text.trim().replace(detailTrailingCount) { it.groupValues[1] }
