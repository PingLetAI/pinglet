package com.linger.app.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerPage

@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onVerified: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    LaunchedEffect(state.verified) { if (state.verified) onVerified() }

    LingerPage(
        eyebrow = "YOUR ACCOUNT",
        title = if (state.codeSent) "Check your inbox." else "Keep what matters.",
        subtitle = if (state.codeSent) "Enter the six-digit code sent to ${state.email}." else "No password. Your email keeps this library attached to you.",
        onBack = onBack,
    ) {
        LingerCard(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .78f)) {
            Text("Your current saves stay exactly where they are.", style = MaterialTheme.typography.titleMedium)
            Text("Verifying upgrades this anonymous profile instead of creating an empty one.")
        }
        if (!state.codeSent) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email address") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Button(
                onClick = { viewModel.requestCode(email) },
                enabled = email.contains('@') && !state.loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text(if (state.loading) "SENDING..." else "SEND MY CODE") }
        } else {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.filter(Char::isDigit).take(6) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Verification code") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            )
            state.devCode?.let { Text("Development code: $it", style = MaterialTheme.typography.labelMedium) }
            Button(
                onClick = { viewModel.verify(code) },
                enabled = code.length == 6 && !state.loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text(if (state.loading) "VERIFYING..." else "VERIFY EMAIL") }
            TextButton(onClick = { viewModel.requestCode(state.email) }, modifier = Modifier.fillMaxWidth()) { Text("SEND A NEW CODE") }
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
