package com.linger.app.ui.add

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContentScreen(
    preFillText: String = "",
    viewModel: AddContentViewModel = hiltViewModel(),
    onQueued: (String) -> Unit = {},
    onCreateAccount: () -> Unit = {},
    onTryPlus: () -> Unit = {},
    onUpgrade: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    var text by remember { mutableStateOf(preFillText) }
    var author by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Thought") }
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(preFillText) { if (preFillText.isNotBlank()) text = preFillText; viewModel.refreshEntitlements() }
    LaunchedEffect(state.queuedIngestionId) { state.queuedIngestionId?.let(onQueued) }
    LaunchedEffect(state.gate) { when (state.gate) { SaveGate.ACCOUNT -> { viewModel.consumeGate(); onCreateAccount() }; SaveGate.PLUS -> { viewModel.consumeGate(); when { state.entitlement?.trialEligible == true -> onTryPlus(); state.entitlement?.paidPlansEnabled == true -> onUpgrade(); else -> viewModel.showSubscriptionsPending() } }; null -> Unit } }

    val sourceUrl = remember(text) { Regex("https://(?:www\\.)?(?:instagram\\.com|tiktok\\.com|[^/]*\\.tiktok\\.com|facebook\\.com|[^/]*\\.facebook\\.com|fb\\.watch)/\\S+", RegexOption.IGNORE_CASE).find(text)?.value?.trimEnd('.', ',', ')') }
    val platform = sourceUrl?.let { when { it.contains("instagram", true) -> "Instagram"; it.contains("tiktok", true) -> "TikTok"; else -> "Facebook" } }

    if (state.showTermsPrompt) {
        AlertDialog(
            onDismissRequest = viewModel::dismissTermsPrompt,
            title = { Text("Sharing content with PingLet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("By continuing, you agree to our Terms of Use. Only submit content you are permitted to share. PingLet may analyze public links and use eligible AI-derived excerpts, topics, source attribution, and links in public Explore catalogs. Your personal notes, account information, and full saved details remain private.")
                    Text("Do not submit illegal, sexually explicit, hateful, violent, abusive, misleading, privacy-invasive, or rights-infringing content. Content may be filtered, removed, or reported.")
                    TextButton(onClick = { uriHandler.openUri("https://pinglet.ai/terms") }) {
                        Text("READ TERMS OF USE")
                    }
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            },
            confirmButton = {
                Button(onClick = viewModel::acceptTermsAndContinue, enabled = !state.acceptingTerms) {
                    Text(if (state.acceptingTerms) "SAVING..." else "AGREE AND CONTINUE")
                }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissTermsPrompt) { Text("NOT NOW") } },
        )
    }

    LingerPage("New PingLet", if (sourceUrl != null) "Save this post." else "What should stay with you?", if (sourceUrl != null) "We will read its words, images, and speech in the background." else "Write it as you want to meet it again.", onBack) {
        state.entitlement?.takeIf { it.accountPromptRecommended && it.isAnonymous }?.let {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = .62f)) {
                Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Connect an email to keep your library safe.", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = onCreateAccount) { Text("CONNECT") }
                }
            }
        }
        if (platform != null) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .7f)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Link, null); Spacer(Modifier.width(10.dp)); Column { Text("$platform post", style = MaterialTheme.typography.titleMedium); Text("Public source detected", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
        LingerCard {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = if (sourceUrl == null) 150.dp else 112.dp),
                placeholder = { Text("Write something worth keeping, or paste an Instagram, TikTok, or Facebook link...") },
                textStyle = MaterialTheme.typography.bodyLarge,
                label = { Text(if (sourceUrl == null) "Thought or social link" else "Shared link") },
            )
            if (sourceUrl == null) {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    listOf("Thought", "Quote", "Reminder").forEachIndexed { index, option ->
                        SegmentedButton(type == option, { type = option }, SegmentedButtonDefaults.itemShape(index, 3), label = { Text(option) }, icon = {})
                    }
                }
                OutlinedTextField(author, { author = it }, Modifier.fillMaxWidth(), label = { Text("Author or source (optional)") }, singleLine = true)
            }
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        Button(
            onClick = { viewModel.save(text, if (type == "Thought") "NOTE" else type, sourceUrl, author) },
            enabled = text.isNotBlank() && !state.saving && !state.saved,
            modifier = Modifier.fillMaxWidth().height(54.dp),
        ) { Text(when { state.saving -> "ADDING TO QUEUE..."; state.saved -> "QUEUED"; sourceUrl != null -> "SAVE AND PROCESS"; else -> "SAVE TO MY ROTATION" }) }
        if (sourceUrl != null) Text("You can leave immediately after saving. Processing may take a few minutes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
