package com.linger.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager

object WidgetPinning {
    fun hasInstalledWidget(context: Context): Boolean {
        val manager = AppWidgetManager.getInstance(context)
        val provider = ComponentName(context, AmbientWidgetReceiver::class.java)
        return manager.getAppWidgetIds(provider).isNotEmpty()
    }

    fun isPinningSupported(context: Context): Boolean =
        AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported

    suspend fun requestPin(context: Context): Boolean {
        if (!isPinningSupported(context)) return false
        return runCatching {
            GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                receiver = AmbientWidgetReceiver::class.java,
                preview = AmbientWidget(),
            )
        }.getOrDefault(false)
    }
}
