package com.linger.app.ui.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerPage
import com.linger.app.ui.components.SectionLabel

@Composable
fun AddContentScreen(
    preFillText: String = "",
    viewModel: AddContentViewModel = hiltViewModel(),
    onQueued: (String) -> Unit = {},
    onCreateAccount: () -> Unit = {},
    onUpgrade: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    var text by remember { mutableStateOf(preFillText) }
    var author by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Thought") }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(preFillText) {
        if (preFillText.isNotBlank()) text = preFillText
        viewModel.refreshEntitlements()
    }
    LaunchedEffect(state.queuedIngestionId) {
        state.queuedIngestionId?.let(onQueued)
    }
    LaunchedEffect(state.gate) {
        when (state.gate) {
            SaveGate.ACCOUNT -> {
                viewModel.consumeGate()
                onCreateAccount()
            }
            SaveGate.PLUS -> {
                viewModel.consumeGate()
                onUpgrade()
            }
            null -> Unit
        }
    }
    val sourceUrl = remember(text) { Regex("https://(?:www\\.)?(?:instagram\\.com|tiktok\\.com|[^/]*\\.tiktok\\.com|facebook\\.com|[^/]*\\.facebook\\.com|fb\\.watch)/\\S+", RegexOption.IGNORE_CASE).find(text)?.value?.trimEnd('.', ',', ')') }
    val platform = sourceUrl?.let {
        when {
            it.contains("instagram", true) -> "Instagram"
            it.contains("tiktok", true) -> "TikTok"
            else -> "Facebook"
        }
    }

    LingerPage("New save", if (sourceUrl != null) "Turn this post into a keepsake." else "What should linger?", "Share a public link or write something you want to meet again.") {
        state.entitlement?.takeIf { it.accountPromptRecommended }?.let { entitlement ->
            LingerCard(color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = .72f)) {
                Text("Keep your saves safe", style = MaterialTheme.typography.titleLarge)
                Text("You have ${entitlement.saveCount} saves. Add your email before save 11 so this library can follow you.")
                TextButton(onClick = onCreateAccount, contentPadding = PaddingValues(0.dp)) { Text("CREATE FREE ACCOUNT") }
            }
        }
        if (platform != null) {
            LingerCard(color = MaterialTheme.colorScheme.secondaryContainer) {
                Text("$platform link detected", style = MaterialTheme.typography.labelLarge)
                Text("You can leave this screen. Linger will preserve the source and process its words, images, and speech in the background.")
            }
        }
        LingerCard {
            SectionLabel(if (sourceUrl != null) "SHARED POST" else "THE WORDS")
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                placeholder = { Text("Paste an Instagram, TikTok, or Facebook link, or write a thought...") },
                textStyle = MaterialTheme.typography.titleLarge,
            )
        }
        SectionLabel("WHAT KIND IS IT?")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Thought", "Quote", "Reminder").forEach { option ->
                FilterChip(type == option, { type = option }, { Text(option) })
            }
        }
        if (sourceUrl == null) OutlinedTextField(author, { author = it }, Modifier.fillMaxWidth(), label = { Text("Author or source (optional)") })
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        state.extractedText?.let {
            LingerCard(color = MaterialTheme.colorScheme.tertiaryContainer) {
                SectionLabel("EXTRACTED TAKEAWAY")
                Text(it, style = MaterialTheme.typography.titleLarge)
            }
        }
        Button(
            onClick = { viewModel.save(text, if (type == "Thought") "NOTE" else type, sourceUrl) },
            enabled = text.isNotBlank() && !state.saving && !state.saved,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary),
        ) { Text(when { state.saving -> "ADDING TO QUEUE..."; state.saved -> "QUEUED"; sourceUrl != null -> "PROCESS IN BACKGROUND"; else -> "SAVE TO MY ROTATION" }) }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("NOT NOW") }
    }
}
