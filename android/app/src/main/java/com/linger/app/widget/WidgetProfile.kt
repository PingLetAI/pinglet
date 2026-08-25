package com.linger.app.widget

import kotlinx.serialization.Serializable

@Serializable
data class WidgetProfile(
    val name: String = "My PingLet",
    val theme: String = "BLEND",
    val contentMode: String = "MIXED",
    val catalogIds: Set<String> = emptySet(),
    val scheduleMode: String = "ANYTIME",
    val typography: String = "EDITORIAL",
    val textScale: String = "SMALL",
    val spacing: String = "COMFORTABLE",
    val opacity: Int = 78,
    val manualNext: Boolean = false,
    val manualOffset: Int = 0,
    val currentContentId: String = "",
    val currentText: String = "",
    val currentAuthor: String? = null,
    val currentSourceUrl: String? = null,
    val currentFavorite: Boolean = false,
    val shownAt: Long = 0,
    val nextChangeAt: Long = 0,
)

fun WidgetProfile.freeDefaults() = copy(
    theme = "BLEND",
    contentMode = "MIXED",
    catalogIds = emptySet(),
    scheduleMode = "ANYTIME",
    typography = "EDITORIAL",
    spacing = "COMFORTABLE",
    manualNext = false,
)
