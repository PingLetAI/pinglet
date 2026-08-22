package com.linger.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onOpenAdd: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Current message", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("The current message rotates every configured interval")

            Row {
                Text("Favorite", modifier = Modifier
                    .padding(8.dp)
                    .clickable { /* TODO: favorite action */ })
                Spacer(Modifier.padding(8.dp))
                Text("Next", modifier = Modifier
                    .padding(8.dp)
                    .clickable { /* TODO: next action */ })
            }

            Spacer(Modifier.height(16.dp))
            Text("Recently shown")
            Text("- placeholder")

            Text("Add content", modifier = Modifier
                .padding(top = 16.dp)
                .clickable { onOpenAdd() },
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
