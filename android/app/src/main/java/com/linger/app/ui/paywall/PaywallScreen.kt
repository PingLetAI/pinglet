package com.linger.app.ui.paywall

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerPage

@Composable
fun PaywallScreen(onBack: () -> Unit, onPurchased: () -> Unit, viewModel: PaywallViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val activity = LocalContext.current.findActivity()
    LaunchedEffect(state.purchased) { if (state.purchased) onPurchased() }
    val offers = state.product?.subscriptionOfferDetails.orEmpty()
    fun price(plan: String, fallback: String) = offers.firstOrNull { it.basePlanId == plan }
        ?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice ?: fallback

    LingerPage("LINGER PLUS", "An unlimited home for what matters.", "Keep every thought. Process more of the posts you find worth remembering.", onBack) {
        LingerCard(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .82f)) {
            Text("Unlimited personal saves", style = MaterialTheme.typography.titleLarge)
            Text("30 original social imports each month")
            Text("Duplicate links never consume AI processing quota")
        }
        PlanButton("Annual", "${price("annual", "$19.99")} per year", "BEST VALUE", state.product != null && !state.verifying) {
            activity?.let { viewModel.buy(it, "annual") }
        }
        PlanButton("Monthly", "${price("monthly", "$2.99")} per month", null, state.product != null && !state.verifying) {
            activity?.let { viewModel.buy(it, "monthly") }
        }
        if (state.loading || state.verifying) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text("Subscriptions renew automatically unless canceled in Google Play. Manage or cancel from your Play account.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PlanButton(title: String, price: String, badge: String?, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(onClick, Modifier.fillMaxWidth().height(68.dp), enabled = enabled) {
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(price) }
        badge?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
