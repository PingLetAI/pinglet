package com.linger.app.domain.usecase

import kotlin.math.floor

object SlotCalculator {
    fun slotForMillis(timestampMillis: Long, intervalMinutes: Int): Long {
        return floor(timestampMillis.toDouble() / (intervalMinutes * 60_000L).toDouble()).toLong()
    }

    fun nextSlotAt(timestampMillis: Long, intervalMinutes: Int): Long {
        val step = intervalMinutes * 60_000L
        return ((timestampMillis / step) + 1) * step
    }
}
