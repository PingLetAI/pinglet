package com.linger.app.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.local.db.DatabaseProvider
import com.linger.app.data.local.entity.ContentEntity
import com.linger.app.data.remote.ContentInsightDto
import com.linger.app.data.repository.FavoriteRepository
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.theme.LingerGold
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContentDetailScreen(
    contentId: String,
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
    onCreateAccount: () -> Unit = {},
    onTryPlus: () -> Unit = {},
    entitlementRefreshKey: Long = 0L,
    viewModel: ContentDetailViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val remote by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var item by remember(contentId) { mutableStateOf<ContentEntity?>(null) }
    var localLoaded by remember(contentId) { mutableStateOf(false) }
    var favorite by remember(contentId) { mutableStateOf(false) }
    var savingFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(contentId) {
        item = DatabaseProvider.database(appContext).contentDao().contentById(contentId)
        favorite = DataStoreManager(appContext).isContentFavorite(contentId)
        localLoaded = true
    }
    LaunchedEffect(entitlementRefreshKey) { if (entitlementRefreshKey > 0L) viewModel.refresh() }

    val detail = remote.detail
    val sourceUrl = detail?.content?.sourceUrl ?: item?.sourceUrl
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(item?.type?.lowercase()?.replaceFirstChar(Char::uppercase) ?: "PingLet", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
                actions = {
                    IconButton(enabled = item != null && !savingFavorite, onClick = {
                        val target = !favorite
                        favorite = target
                        savingFavorite = true
                        scope.launch {
                            if (runCatching { FavoriteRepository.setFavorite(appContext, contentId, target) }.isFailure) favorite = !target
                            savingFavorite = false
                        }
                    }) {
                        Icon(if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, if (favorite) "Remove favorite" else "Favorite", tint = if (favorite) LingerGold else MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = .96f)),
            )
        },
    ) { padding ->
        when {
            !localLoaded -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            item == null -> EmptyDetail(Modifier.padding(padding), onBack)
            else -> Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                val content = item!!
                Text(if (content.source == "PERSONAL") "SAVED BY YOU" else "FROM PINGLET", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                Text(cleanDetailText(content.text), style = MaterialTheme.typography.headlineMedium)
                content.author?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Box(Modifier.width(38.dp).height(3.dp).background(LingerGold, RoundedCornerShape(4.dp)))

                sourceUrl?.takeIf { it.isNotBlank() }?.let { url ->
                    FilledTonalButton(
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE)) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) { Icon(Icons.Rounded.OpenInNew, null); Spacer(Modifier.width(9.dp)); Text("OPEN ORIGINAL SOURCE") }
                }

                if (remote.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                detail?.overview?.takeIf { it.isNotBlank() }?.let { DetailSection("Overview") { Text(it, style = MaterialTheme.typography.bodyLarge) } }
                if (detail?.insights?.isNotEmpty() == true) DetailSection("Key insights") {
                    detail.insights.forEachIndexed { index, insight -> InsightRow(index + 1, insight) }
                }

                if (detail?.access?.fullDetailsUnlocked == true) {
                    detail.comprehensiveSummary?.takeIf { it.isNotBlank() }?.let { DetailSection("Full summary") { Text(it, style = MaterialTheme.typography.bodyMedium) } }
                    if (detail.actions.isNotEmpty()) DetailSection("Things to take forward") { detail.actions.forEach { BulletRow(it) } }
                    if (detail.themes.isNotEmpty()) FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        detail.themes.forEach { AssistChip(onClick = {}, label = { Text(it) }) }
                    }
                    detail.transcript?.takeIf { it.isNotBlank() }?.let { CollapsibleText("Full transcript", it) }
                    detail.visibleText?.takeIf { it.isNotBlank() }?.let { CollapsibleText("Text found in images", it) }
                    detail.caption?.takeIf { it.isNotBlank() }?.let { CollapsibleText("Original caption", it) }
                } else if (detail?.access?.hasAnalysis == true) {
                    PremiumUnlockCard(detail.access, onCreateAccount, onTryPlus, onUpgrade)
                }

                if (remote.loadFailed && detail == null) DetailLoadFailure(viewModel::refresh)
            }
        }
    }
}

@Composable
private fun DetailLoadFailure(onRetry: () -> Unit) {
    LingerCard(color = MaterialTheme.colorScheme.surface) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Rounded.CloudOff, null, tint = MaterialTheme.colorScheme.tertiary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Details couldn't load", style = MaterialTheme.typography.titleMedium)
                Text("Your saved PingLet and original source are still available.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text("TRY AGAIN")
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(title, style = MaterialTheme.typography.titleLarge)
        content()
    }
}

@Composable
private fun InsightRow(number: Int, insight: ContentInsightDto) {
    LingerCard {
        Text("$number", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
        Text(insight.title, style = MaterialTheme.typography.titleMedium)
        Text(insight.explanation, style = MaterialTheme.typography.bodyMedium)
        if (insight.evidence.isNotBlank()) Text("“" + insight.evidence + "”", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BulletRow(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.padding(top = 7.dp).size(6.dp).background(LingerGold, RoundedCornerShape(50)))
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PremiumUnlockCard(access: com.linger.app.data.remote.ContentDetailAccessDto, onCreateAccount: () -> Unit, onTryPlus: () -> Unit, onUpgrade: () -> Unit) {
    LingerCard(color = MaterialTheme.colorScheme.secondaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.tertiary)
            Text("There is more in this PingLet", style = MaterialTheme.typography.titleLarge)
        }
        Text("Unlock the full summary, all insights, practical takeaways, transcript, visible text, and related topics.", style = MaterialTheme.typography.bodyMedium)
        if (access.lockedSections.isNotEmpty()) Text("${access.lockedSections.size} detailed sections available", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        when {
            access.isAnonymous -> Button(onClick = onCreateAccount, modifier = Modifier.fillMaxWidth()) { Text("CREATE ACCOUNT TO TRY PLUS") }
            access.trialEligible -> Button(onClick = onTryPlus, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.LockOpen, null); Spacer(Modifier.width(8.dp)); Text("TRY PLUS FREE - 7 DAYS") }
            access.paidPlansEnabled -> Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.LockOpen, null); Spacer(Modifier.width(8.dp)); Text("UNLOCK WITH PINGLET PLUS") }
            else -> Text("PingLet Plus subscriptions are coming soon.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!access.isAnonymous && access.trialEligible && access.paidPlansEnabled) TextButton(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) { Text("VIEW PAID PLANS") }
    }
}

@Composable
private fun CollapsibleText(title: String, text: String) {
    var expanded by rememberSaveable(title) { mutableStateOf(false) }
    OutlinedCard(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, if (expanded) "Collapse" else "Expand")
            }
            if (expanded) Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EmptyDetail(modifier: Modifier, onBack: () -> Unit) {
    Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("This PingLet is unavailable.", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("It may have been removed or has not finished syncing.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onBack) { Text("GO BACK") }
    }
}

private val detailTrailingCount = Regex("""([.!?])\s+(?:\d{1,3}(?:[,.]\d{3})*|\d+(?:\.\d+)?[KkMmBb])\s*$""")
private fun cleanDetailText(text: String) = text.trim().replace(detailTrailingCount) { it.groupValues[1] }
