package com.linger.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.linger.app.domain.model.WidgetState
import com.linger.app.widget.rotation.RotationManager

class AmbientWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent(WidgetState(
                contentId = "",
                text = "Ready. Add content to start resurfacing rotation.",
                author = null,
                shownAt = System.currentTimeMillis(),
                nextChangeAt = RotationManager.nextChangeAt(System.currentTimeMillis()),
            ))
        }
    }
}

@Composable
fun WidgetContent(state: WidgetState) {
    Column(
        modifier = GlanceModifier
            .padding(10.dp)
            .fillMaxSize(),
        verticalAlignment = Alignment.Vertical.Top,
    ) {
        Text(text = state.text, style = TextStyle(color = ColorProvider(Color.White)))
        Spacer(modifier = GlanceModifier.height(8.dp))
        if (!state.author.isNullOrBlank()) {
            Text(text = state.author)
        }
    }
}

class AmbientWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AmbientWidget()
}
