package com.linger.app.ui.trial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.ui.components.LingerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrialOfferScreen(
    entrySource: String,
    onBack: () -> Unit,
    onActivated: () -> Unit,
    onContinueFree: () -> Unit,
    onViewPlans: () -> Unit,
    viewModel: TrialOfferViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.track("TRIAL_OFFER_VIEWED", entrySource) }
    LaunchedEffect(state.activated) { if (state.activated) onActivated() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("PINGLET PLUS", style = MaterialTheme.typography.labelLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(34.dp), tint = MaterialTheme.colorScheme.tertiary)
            Text("Try PingLet Plus", style = MaterialTheme.typography.displaySmall)
            Text("Enjoy everything in Plus free for 7 days.", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)

            LingerCard(color = MaterialTheme.colorScheme.secondaryContainer) {
                TrialBenefit("Full summaries, transcripts, insights, and takeaways")
                TrialBenefit("Premium widget themes, profiles, and scheduled modes")
                TrialBenefit("More AI imports and unlimited personal saves")
                TrialBenefit("Personalized rotation and manual show-another controls")
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Rounded.Shield, null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text("No payment required", fontWeight = FontWeight.SemiBold)
                    Text("No card. No automatic subscription.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            when {
                state.loading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                state.entitlement?.trialEligible == true -> {
                    Button(
                        onClick = { viewModel.startTrial(entrySource) },
                        enabled = !state.activating,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                    ) {
                        if (state.activating) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("TRY PLUS FREE")
                    }
                    TextButton(onClick = { viewModel.track("TRIAL_SKIPPED", entrySource); onContinueFree() }, modifier = Modifier.fillMaxWidth()) { Text("CONTINUE WITH FREE") }
                }
                state.entitlement?.trialStatus == "ACTIVE" -> {
                    LingerCard {
                        Icon(Icons.Rounded.Schedule, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Your Plus trial is already active", style = MaterialTheme.typography.titleLarge)
                        Text("${state.entitlement?.trialDaysRemaining ?: 0} days remaining. You will not be charged when it ends.")
                    }
                    Button(onClick = onContinueFree, modifier = Modifier.fillMaxWidth()) { Text("CONTINUE TO PINGLET") }
                }
                else -> {
                    Text("This account has already used its free Plus trial. You can keep using Free or choose a paid plan.", style = MaterialTheme.typography.bodyLarge)
                    if (state.entitlement?.paidPlansEnabled == true) Button(onClick = onViewPlans, modifier = Modifier.fillMaxWidth()) { Text("VIEW PLUS PLANS") }
                    TextButton(onClick = onContinueFree, modifier = Modifier.fillMaxWidth()) { Text("CONTINUE WITH FREE") }
                }
            }
            if (state.entitlement?.trialEligible == true && state.entitlement?.paidPlansEnabled == true) TextButton(onClick = onViewPlans, modifier = Modifier.fillMaxWidth()) { Text("VIEW PAID PLANS") }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TrialBenefit(text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Rounded.CheckCircle, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
    }
}
