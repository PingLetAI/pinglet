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
import androidx.glance.text.Text
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.linger.app.MainActivity
import com.linger.app.R
import com.linger.app.data.local.DataStoreManager
import com.linger.app.data.remote.ApiConfig
import com.linger.app.data.remote.RetrofitClient
import com.linger.app.data.repository.AuthRepositoryImpl
import com.linger.app.data.repository.SessionManager
import com.linger.app.data.repository.FavoriteRepository
import com.linger.app.domain.model.WidgetState
import com.linger.app.widget.rotation.RotationManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AmbientWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = DataStoreManager(context)
        val now = System.currentTimeMillis()

        val contentId = dataStore.readLastDisplayedContentId()
        val contentText = dataStore.readLastDisplayedContentText().ifBlank {
            if (contentId.isBlank()) {
                "Add content and your messages will keep rotating."
            } else {
                "Your message is ready. Open the app to refresh content."
            }
        }
        val contentAuthor = dataStore.readLastDisplayedContentAuthor().ifBlank { null }
        val shownAt = dataStore.readLastDisplayedShownAt()
        val nextChangeAt = dataStore.readLastDisplayedNextChangeAt()
        val favorite = dataStore.readLastDisplayedFavorite()
        val sourceUrl = dataStore.readLastDisplayedSourceUrl().ifBlank { null }
        val textSize = dataStore.widgetTextSize().firstOrNull() ?: "SMALL"
        val opacity = dataStore.widgetOpacity().firstOrNull() ?: 78
        val refreshMinutes = dataStore.refreshMinutes().firstOrNull() ?: 30
        val surfaceColor = wallpaperTintedSurface(context, opacity)

        provideContent {
            WidgetContent(
                WidgetState(
                    contentId = contentId,
                    text = contentText,
                    author = contentAuthor,
                    shownAt = if (shownAt == 0L) now else shownAt,
                    nextChangeAt = if (nextChangeAt == 0L) RotationManager.nextChangeAt(now) else nextChangeAt,
                    favorite = favorite,
                    sourceUrl = sourceUrl,
                ),
                refreshMinutes,
                surfaceColor,
                textSize,
            )
        }
    }
}

@Composable
fun WidgetContent(state: WidgetState, intervalMinutes: Int, surfaceColor: Color, textSize: String) {
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
    val contentPadding = when {
        compactHeight || compactWidth -> 12.dp
        expanded -> 16.dp
        else -> 14.dp
    }
    val sizeAdjustment = when (textSize) { "LARGE" -> 2; "MEDIUM" -> 0; else -> -1 }
    val messageFontSize = ((if (compactHeight || compactWidth) 13 else if (expanded) 17 else 15) + sizeAdjustment).sp
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(surfaceColor))
            .cornerRadius(20.dp)
            .padding(contentPadding),
        verticalAlignment = Alignment.Vertical.Top,
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            Row(
                modifier = GlanceModifier.defaultWeight().clickable(openApp),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(8.dp)
                        .background(ColorProvider(Color(0xFFE7B64B)))
                        .cornerRadius(8.dp),
                ) {}
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = "PINGLET",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFF7F0DF)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
            if (state.contentId.isNotBlank()) {
                Box(
                    modifier = GlanceModifier
                        .size(if (compactHeight) 38.dp else 44.dp)
                        .clickable(
                            actionRunCallback<FavoriteContentAction>(
                                actionParametersOf(
                                    FavoriteContentAction.contentIdKey to state.contentId,
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        provider = ImageProvider(if (state.favorite) R.drawable.ic_widget_heart_filled else R.drawable.ic_widget_heart_outline),
                        contentDescription = if (state.favorite) "Remove from favorites" else "Add to favorites",
                        modifier = GlanceModifier.size(if (compactHeight) 22.dp else 26.dp),
                    )
                }
            }
        }

        Spacer(modifier = GlanceModifier.height(if (compactHeight) 4.dp else 8.dp))
        Box(modifier = GlanceModifier.fillMaxWidth().defaultWeight().clickable(openApp), contentAlignment = Alignment.CenterStart) {
            Text(
                text = widgetDisplayText(state.text),
                modifier = GlanceModifier.fillMaxWidth(),
                style = TextStyle(color = ColorProvider(Color(0xFFFFF9EC)), fontSize = messageFontSize, fontWeight = FontWeight.Normal),
                maxLines = if (compactHeight) 2 else if (expanded) 7 else 4,
            )
        }

        if (!compactHeight) {
            Spacer(modifier = GlanceModifier.fillMaxWidth().height(if (expanded) 12.dp else 7.dp).clickable(openApp))
            Box(
                modifier = GlanceModifier
                    .width(28.dp)
                    .height(2.dp)
                    .background(ColorProvider(Color(0xFFE7B64B)))
                    .cornerRadius(2.dp)
                    .clickable(openApp),
            ) {}
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = state.author?.takeIf { it.isNotBlank() }?.uppercase()
                    ?: "A THOUGHT WORTH KEEPING",
                modifier = GlanceModifier.fillMaxWidth().clickable(openApp),
                style = TextStyle(
                    color = ColorProvider(Color(0xFFAAA99F)),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
            )
            if (expanded && state.sourceUrl != null) {
                Spacer(modifier = GlanceModifier.height(3.dp))
                Text("SOURCE SAVED", modifier = GlanceModifier.clickable(openApp), style = TextStyle(color = ColorProvider(Color(0xFFE7B64B)), fontSize = 9.sp, fontWeight = FontWeight.Bold))
            }
        }
    }
}

private val trailingEngagementCount = Regex(
    """([.!?])\s+(?:\d{1,3}(?:[,.]\d{3})*|\d+(?:\.\d+)?[KkMmBb])\s*$""",
)

private fun widgetDisplayText(text: String): String = text
    .trim()
    .replace(trailingEngagementCount) { match -> match.groupValues[1] }

class FavoriteContentAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val contentId = parameters[contentIdKey]?.takeIf { it.isNotBlank() } ?: return
        val dataStore = DataStoreManager(context)
        toggleMutex.withLock {
            if (dataStore.readLastDisplayedContentId() != contentId) return

            val targetFavorite = !dataStore.readLastDisplayedFavorite()
            FavoriteRepository.setFavorite(context, contentId, targetFavorite)
        }
    }

    companion object {
        val contentIdKey = ActionParameters.Key<String>("favorite_content_id")
        private val toggleMutex = Mutex()
    }
}

private fun wallpaperTintedSurface(context: Context, opacityPercent: Int): Color {
    val alpha = opacityPercent.coerceIn(55, 92) / 100f
    val fallback = Color(0xFF191A17).copy(alpha = alpha)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return fallback

    val wallpaperColor = runCatching {
        WallpaperManager.getInstance(context)
            .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            ?.primaryColor
            ?.toArgb()
            ?.let(::Color)
    }.getOrNull() ?: return fallback

    val tintStrength = 0.28f
    val baseStrength = 1f - tintStrength
    return Color(
        red = (wallpaperColor.red * tintStrength) + (0.09f * baseStrength),
        green = (wallpaperColor.green * tintStrength) + (0.10f * baseStrength),
        blue = (wallpaperColor.blue * tintStrength) + (0.09f * baseStrength),
        alpha = alpha,
    )
}

class AmbientWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AmbientWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        com.linger.app.sync.SyncScheduler.scheduleWidgetAdded(context)
    }
}
