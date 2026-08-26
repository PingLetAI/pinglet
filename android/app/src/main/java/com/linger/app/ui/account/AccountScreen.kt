package com.linger.app.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linger.app.ui.components.LingerPage

@Composable
fun AccountScreen(onBack: () -> Unit, onVerified: () -> Unit, viewModel: AccountViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var resendIn by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.verified) { if (state.verified) { kotlinx.coroutines.delay(650); onVerified() } }
    LaunchedEffect(state.codeSent) {
        if (state.codeSent) {
            resendIn = 60
            while (resendIn > 0) { kotlinx.coroutines.delay(1_000); resendIn-- }
        }
    }

    LingerPage("Account", if (state.verified) "You are signed in." else if (state.codeSent) "Check your inbox." else "Keep your library with you.", if (state.codeSent) "We sent a six-digit code to ${state.email}." else "No password. A new email secures your current saves; an existing email signs you back into the account you own.", onBack) {
        if (state.verified) {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer) { Text("Email verified", Modifier.fillMaxWidth().padding(24.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineSmall) }
        } else if (!state.codeSent) {
            OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email address") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            Button({ viewModel.requestCode(email) }, Modifier.fillMaxWidth().height(54.dp), enabled = email.contains('@') && !state.loading) { Text(if (state.loading) "SENDING..." else "EMAIL ME A SIGN-IN CODE") }
        } else {
            OutlinedTextField(code, { code = it.filter(Char::isDigit).take(6) }, Modifier.fillMaxWidth(), label = { Text("Verification code") }, singleLine = true, textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword))
            state.devCode?.let { Text("Development code: $it", style = MaterialTheme.typography.labelMedium) }
            Button({ viewModel.verify(code) }, Modifier.fillMaxWidth().height(54.dp), enabled = code.length == 6 && !state.loading) { Text(if (state.loading) "SIGNING IN..." else "VERIFY AND CONTINUE") }
            TextButton({ viewModel.requestCode(state.email) }, Modifier.fillMaxWidth(), enabled = !state.loading && resendIn == 0) { Text(if (resendIn > 0) "SEND AGAIN IN ${resendIn}S" else "SEND A NEW CODE") }
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (!state.verified) Text("Your email is used for account access and essential service messages.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
