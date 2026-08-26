package com.linger.app.widget

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.linger.app.MainActivity
import com.linger.app.R
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.repository.FavoriteRepository
import com.linger.app.domain.model.WidgetState
import com.linger.app.sync.SyncScheduler
import com.linger.app.widget.rotation.RotationManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AmbientWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val store = DataStoreManager(context)
        val widgetKey = id.toString()
        val stored = store.readWidgetProfile(widgetKey)
        val isPlus = store.isPlusAccessActive()
        val profile = if (isPlus) stored else stored.freeDefaults()
        val now = System.currentTimeMillis()
        val state = WidgetState(
            contentId = profile.currentContentId,
            text = profile.currentText.ifBlank { "Add something worth keeping and it will live here." },
            author = profile.currentAuthor,
            shownAt = profile.shownAt.takeIf { it > 0 } ?: now,
            nextChangeAt = profile.nextChangeAt.takeIf { it > 0 } ?: RotationManager.nextChangeAt(now),
            favorite = profile.currentContentId.isNotBlank() && store.isContentFavorite(profile.currentContentId),
            sourceUrl = profile.currentSourceUrl,
        )
        val palette = widgetPalette(context, profile.theme, profile.opacity)

        provideContent { WidgetContent(state, profile, palette, isPlus, widgetKey) }
    }
}

private data class WidgetPalette(val surface: Color, val text: Color, val muted: Color, val accent: Color)

private fun widgetPalette(context: Context, theme: String, opacity: Int): WidgetPalette {
    val alpha = opacity.coerceIn(55, 96) / 100f
    return when (theme) {
        "INK" -> WidgetPalette(Color(0xFF10110F).copy(alpha = alpha), Color(0xFFFFFBF0), Color(0xFFB9B7AD), Color(0xFFE7B64B))
        "FOREST" -> WidgetPalette(Color(0xFF183029).copy(alpha = alpha), Color(0xFFF4F2E8), Color(0xFFB8C8BE), Color(0xFF8ED0AA))
        "CLAY" -> WidgetPalette(Color(0xFF512A23).copy(alpha = alpha), Color(0xFFFFF4E8), Color(0xFFD7BDB4), Color(0xFFF0A879))
        else -> WidgetPalette(wallpaperTintedSurface(context, opacity), Color(0xFFFFF9EC), Color(0xFFAAA99F), Color(0xFFE7B64B))
    }
}

@Composable
private fun WidgetContent(state: WidgetState, profile: WidgetProfile, palette: WidgetPalette, isPlus: Boolean, widgetKey: String) {
    val context = LocalContext.current
    val openApp = actionStartActivity(
        Intent(context, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_CONTENT_ID, state.contentId)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
    )
    val size = LocalSize.current
    val compactWidth = size.width < 160.dp
    val compactHeight = size.height < 150.dp
    val expanded = size.height >= 220.dp && size.width >= 220.dp
    val spacingAdjustment = when (profile.spacing) { "COMPACT" -> -3; "AIRY" -> 4; else -> 0 }
    val padding = ((if (compactHeight || compactWidth) 12 else if (expanded) 16 else 14) + spacingAdjustment).coerceAtLeast(8).dp
    val typeAdjustment = when (profile.typography) { "CLEAN" -> 1; "COMPACT" -> -2; else -> 0 }
    val scaleAdjustment = when (profile.textScale) { "LARGE" -> 2; "MEDIUM" -> 0; else -> -1 }
    val messageSize = ((if (compactHeight || compactWidth) 13 else if (expanded) 17 else 15) + typeAdjustment + scaleAdjustment).sp
    val messageWeight = if (profile.typography == "CLEAN") FontWeight.Medium else FontWeight.Normal

    Column(
        modifier = GlanceModifier.fillMaxSize().background(ColorProvider(palette.surface)).cornerRadius(20.dp).padding(padding),
        verticalAlignment = Alignment.Vertical.Top,
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Vertical.CenterVertically) {
            Row(modifier = GlanceModifier.defaultWeight().clickable(openApp), verticalAlignment = Alignment.Vertical.CenterVertically) {
                Box(GlanceModifier.size(8.dp).background(ColorProvider(palette.accent)).cornerRadius(8.dp)) {}
                Spacer(GlanceModifier.width(8.dp))
                Text("PINGLET", style = TextStyle(ColorProvider(palette.text), 11.sp, FontWeight.Bold))
            }
            if (isPlus && profile.manualNext && state.contentId.isNotBlank()) {
                Box(
                    modifier = GlanceModifier.size(if (compactHeight) 34.dp else 40.dp).clickable(
                        actionRunCallback<NextContentAction>(
                            actionParametersOf(NextContentAction.widgetKey to widgetKey),
                        ),
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(ImageProvider(R.drawable.ic_widget_next), "Show another", GlanceModifier.size(21.dp))
                }
            }
            if (state.contentId.isNotBlank()) {
                Box(
                    modifier = GlanceModifier.size(if (compactHeight) 38.dp else 42.dp).clickable(
                        actionRunCallback<FavoriteContentAction>(
                            actionParametersOf(
                                FavoriteContentAction.contentIdKey to state.contentId,
                                FavoriteContentAction.widgetKey to widgetKey,
                            ),
                        ),
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        ImageProvider(if (state.favorite) R.drawable.ic_widget_heart_filled else R.drawable.ic_widget_heart_outline),
                        if (state.favorite) "Remove from favorites" else "Add to favorites",
                        GlanceModifier.size(if (compactHeight) 22.dp else 25.dp),
                    )
                }
            }
        }

        Spacer(GlanceModifier.height(if (compactHeight) 4.dp else (8 + spacingAdjustment.coerceAtLeast(0)).dp))
        Box(GlanceModifier.fillMaxWidth().defaultWeight().clickable(openApp), contentAlignment = Alignment.CenterStart) {
            Text(
                widgetDisplayText(state.text),
                GlanceModifier.fillMaxWidth(),
                TextStyle(ColorProvider(palette.text), messageSize, messageWeight),
                maxLines = if (compactHeight) 2 else if (expanded) 7 else 4,
            )
        }

        if (!compactHeight) {
            Spacer(GlanceModifier.height(if (expanded) 12.dp else 7.dp).clickable(openApp))
            Box(GlanceModifier.width(28.dp).height(2.dp).background(ColorProvider(palette.accent)).cornerRadius(2.dp).clickable(openApp)) {}
            Spacer(GlanceModifier.height((8 + spacingAdjustment.coerceAtLeast(0)).dp))
            Text(
                state.author?.takeIf(String::isNotBlank)?.uppercase() ?: "A THOUGHT WORTH KEEPING",
                GlanceModifier.fillMaxWidth().clickable(openApp),
                TextStyle(ColorProvider(palette.muted), 10.sp, FontWeight.Medium),
                maxLines = 1,
            )
            if (expanded && state.sourceUrl != null) {
                Spacer(GlanceModifier.height(3.dp))
                Text("SOURCE SAVED", GlanceModifier.clickable(openApp), TextStyle(ColorProvider(palette.accent), 9.sp, FontWeight.Bold))
            }
        }
    }
}

private val trailingEngagementCount = Regex("""([.!?])\s+(?:\d{1,3}(?:[,.]\d{3})*|\d+(?:\.\d+)?[KkMmBb])\s*$""")
private fun widgetDisplayText(text: String) = text.trim().replace(trailingEngagementCount) { it.groupValues[1] }

class FavoriteContentAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val contentId = parameters[contentIdKey]?.takeIf(String::isNotBlank) ?: return
        val key = parameters[widgetKey] ?: glanceId.toString()
        val store = DataStoreManager(context)
        toggleMutex.withLock {
            val profile = store.readWidgetProfile(key)
            if (profile.currentContentId != contentId) return
            val target = !profile.currentFavorite
            FavoriteRepository.setFavorite(context, contentId, target)
            store.setWidgetProfile(key, profile.copy(currentFavorite = target))
            AmbientWidget().update(context, glanceId)
        }
    }

    companion object {
        val contentIdKey = ActionParameters.Key<String>("favorite_content_id")
        val widgetKey = ActionParameters.Key<String>("favorite_widget_key")
        private val toggleMutex = Mutex()
    }
}

class NextContentAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        if (!DataStoreManager(context).isPlusAccessActive()) return
        SyncScheduler.rotateWidget(context, parameters[widgetKey] ?: glanceId.toString())
    }

    companion object { val widgetKey = ActionParameters.Key<String>("next_widget_key") }
}

private fun wallpaperTintedSurface(context: Context, opacityPercent: Int): Color {
    val alpha = opacityPercent.coerceIn(55, 96) / 100f
    val fallback = Color(0xFF191A17).copy(alpha = alpha)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return fallback
    val wallpaper = runCatching {
        WallpaperManager.getInstance(context).getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            ?.primaryColor?.toArgb()?.let(::Color)
    }.getOrNull() ?: return fallback
    return Color(
        red = wallpaper.red * .28f + .09f * .72f,
        green = wallpaper.green * .28f + .10f * .72f,
        blue = wallpaper.blue * .28f + .09f * .72f,
        alpha = alpha,
    )
}

class AmbientWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AmbientWidget()
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        SyncScheduler.scheduleWidgetAdded(context)
    }
}
