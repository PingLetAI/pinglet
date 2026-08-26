package com.linger.app.ui.paywall

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerPage

@Composable
fun PaywallScreen(onBack: () -> Unit, onPurchased: () -> Unit, viewModel: PaywallViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context.findActivity()
    var selected by remember { mutableStateOf("annual") }
    LaunchedEffect(state.purchased) { if (state.purchased) onPurchased() }
    val offers = state.product?.subscriptionOfferDetails.orEmpty()
    fun price(plan: String, fallback: String) = offers.firstOrNull { it.basePlanId == plan }?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice ?: fallback

    LingerPage("PingLet Plus", "Keep everything worth remembering.", "More room for the ideas, words, and stories you want to carry forward.", onBack) {
        if (!state.loading && !state.billingEnabled) {
            LingerCard(color = MaterialTheme.colorScheme.secondaryContainer) {
                Icon(Icons.Rounded.WorkspacePremium, null)
                Text("Subscriptions are coming soon", style = MaterialTheme.typography.titleLarge)
                Text("The seven-day PingLet Plus trial remains free, requires no payment method, and never starts an automatic subscription.", style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("GO BACK") }
            return@LingerPage
        }
        LingerCard(dark = true) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.WorkspacePremium, null, tint = MaterialTheme.colorScheme.secondary); Spacer(Modifier.width(9.dp)); Text("PINGLET PLUS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary) }
            Benefit("Complete AI breakdowns", "Full transcripts, visible text and comprehensive summaries")
            Benefit("Every useful insight", "All takeaways, themes and practical actions")
            Benefit("50 social imports monthly", "Duplicate links never use your processing quota")
            Benefit("Unlimited personal saves", "Keep your entire library without a total cap")
            Benefit("Premium widget profiles", "Independent themes, schedules and content mixes")
        }
        PlanChoice("annual", "Annual", "${price("annual", "$14.99")} per year", "BEST VALUE", selected) { selected = "annual" }
        PlanChoice("monthly", "Monthly", "${price("monthly", "$1.99")} per month", null, selected) { selected = "monthly" }
        Button(
            onClick = { activity?.let { viewModel.buy(it, selected) } },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            enabled = state.product != null && !state.loading && !state.verifying,
        ) { Text(if (state.verifying) "CONFIRMING..." else "CONTINUE WITH ${selected.uppercase()}") }
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        TextButton(viewModel::restore, Modifier.fillMaxWidth(), enabled = !state.verifying) { Text("RESTORE PURCHASES") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton({ context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://pinglet.ai/terms"))) }) { Text("Terms") }
            TextButton({ context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://pinglet.ai/privacy"))) }) { Text("Privacy") }
        }
        Text("Payment is handled securely by Google Play. Subscriptions renew automatically unless canceled in your Play account.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Benefit(title: String, supporting: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Rounded.CheckCircle, null, Modifier.size(19.dp), tint = MaterialTheme.colorScheme.secondary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                supporting,
                style = MaterialTheme.typography.bodySmall,
                color = LocalContentColor.current.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun PlanChoice(id: String, title: String, price: String, badge: String?, selected: String, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = MaterialTheme.shapes.large, color = if (selected == id) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected == id, onClick); Spacer(Modifier.width(8.dp)); Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleLarge); Text(price, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            badge?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary) }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) { is Activity -> this; is ContextWrapper -> baseContext.findActivity(); else -> null }
