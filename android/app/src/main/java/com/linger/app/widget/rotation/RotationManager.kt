package com.linger.app.widget.rotation

import com.linger.app.domain.usecase.SlotCalculator

object RotationManager {
    fun slotIndex(intervalMinutes: Int, atMillis: Long = System.currentTimeMillis()): Long {
        return SlotCalculator.slotForMillis(atMillis, intervalMinutes)
    }

    fun nextChangeAt(nowMillis: Long, intervalMinutes: Int = 30): Long {
        return SlotCalculator.nextSlotAt(nowMillis, intervalMinutes)
    }
}
