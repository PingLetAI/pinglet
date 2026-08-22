package com.linger.app.ui.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddContentScreen(preFillText: String = "") {
    var text by remember { mutableStateOf(preFillText) }
    var author by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Add Content", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Text") })
            OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author") })
            Button(onClick = { /* persist locally and sync later */ }) {
                Text("Save")
            }
        }
    }
}

@Composable
fun AddContentViewModelPlaceholder() {
    var _ignored by remember { mutableStateOf(false) }
    if (_ignored) {}
}
