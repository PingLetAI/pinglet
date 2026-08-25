package com.linger.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
    PageBackground {
        Box(
            Modifier.offset(270.dp, (-80).dp).size(190.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.42f)),
        )
        Box(
            Modifier.offset((-100).dp, 560.dp).size(180.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.34f)),
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PageHeader(eyebrow, title, subtitle, onBack)
            Spacer(Modifier.height(2.dp))
            content()
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun LingerLazyPage(
    eyebrow: String,
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    PageBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { PageHeader(eyebrow, title, subtitle, onBack) }
            item { Spacer(Modifier.height(2.dp)) }
            content()
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun PageBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = .16f)),
            ),
        ),
        content = content,
    )
}

@Composable
private fun PageHeader(eyebrow: String, title: String, subtitle: String?, onBack: (() -> Unit)?) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            onBack?.let { navigateBack ->
                IconButton(onClick = navigateBack, modifier = Modifier.size(40.dp).offset(x = (-8).dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            }
            Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
        }
        Text(title, style = MaterialTheme.typography.displaySmall)
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun LingerCard(
    modifier: Modifier = Modifier,
    color: Color? = null,
    dark: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Surface(
        modifier = modifier.then(if (dark) Modifier else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .72f), shape)),
        shape = shape,
        color = color ?: if (dark) Color(0xFF1B1C18) else MaterialTheme.colorScheme.surface,
        contentColor = if (dark) Color(0xFFFFF7E8) else MaterialTheme.colorScheme.onSurface,
        tonalElevation = if (dark) 0.dp else 1.dp,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
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
