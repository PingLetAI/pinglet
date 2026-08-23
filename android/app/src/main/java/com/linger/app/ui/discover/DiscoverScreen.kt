package com.linger.app.ui.discover

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.linger.app.ui.components.LingerCard
import com.linger.app.ui.components.LingerPage
import com.linger.app.ui.components.SectionLabel
import com.linger.app.ui.components.StatusPill

@Composable
fun DiscoverScreen() {
    LingerPage("Explore", "Find a voice for every day.", "Curated catalogs gently fill the spaces between your personal saves.") {
        CatalogCard("STOIC WISDOM", "Steady words for difficult days.", "CALM + RESILIENCE", MaterialTheme.colorScheme.secondaryContainer)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SectionLabel("MORE CATALOGS")
            StatusPill("3 ACTIVE")
        }
        CatalogCard("SMALL COURAGE", "Gentle momentum without the noise.", "MOMENTUM + CARE", MaterialTheme.colorScheme.tertiaryContainer)
        CatalogCard("CREATIVE PRACTICE", "Prompts for returning to the work.", "FOCUS + CREATIVITY", MaterialTheme.colorScheme.surfaceVariant)
    }
}

@Composable
private fun CatalogCard(label: String, title: String, detail: String, color: Color) {
    LingerCard(color = color) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("IN ROTATION", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
        }
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(detail, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
