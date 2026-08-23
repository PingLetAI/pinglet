package com.linger.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.linger.app.ui.theme.LingerClay
import com.linger.app.ui.theme.LingerGold
import com.linger.app.ui.theme.LingerMint
import com.linger.app.ui.theme.LingerBlush

@Composable
fun LingerPage(
    eyebrow: String,
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(Color(0xFFFFFEFB), MaterialTheme.colorScheme.background, Color(0xFFF7F1EA)),
            ),
        ),
    ) {
        Box(
            Modifier.offset(270.dp, (-80).dp).size(190.dp).clip(CircleShape)
                .background(LingerMint.copy(alpha = 0.72f)),
        )
        Box(
            Modifier.offset((-100).dp, 560.dp).size(180.dp).clip(CircleShape)
                .background(LingerBlush.copy(alpha = 0.62f)),
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            onBack?.let { navigateBack ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = navigateBack, modifier = Modifier.size(42.dp).offset(x = (-8).dp)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onBack == null) {
                    Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                }
                Text(title, style = MaterialTheme.typography.displaySmall)
                subtitle?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(2.dp))
            content()
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun LingerCard(
    modifier: Modifier = Modifier,
    color: Color? = null,
    dark: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = color ?: if (dark) Color(0xFF1B1C18) else MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        contentColor = if (dark) Color(0xFFFFF7E8) else MaterialTheme.colorScheme.onSurface,
        shadowElevation = if (dark) 0.dp else 2.dp,
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(11.dp), content = content)
    }
}

@Composable
fun SectionLabel(text: String, detail: String? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge)
        detail?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun StatusPill(text: String, color: Color = LingerGold) {
    Row(
        Modifier.clip(RoundedCornerShape(50.dp)).background(color.copy(alpha = 0.16f))
            .padding(horizontal = 11.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge)
    }
}
