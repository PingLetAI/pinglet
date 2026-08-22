package com.linger.app.domain.usecase

enum class Mix { MOSTLY_MINE, BALANCED, MORE_DISCOVERY }

fun personalRatio(personalCount: Int, mix: Mix = Mix.BALANCED): Double {
    if (personalCount <= 0) return 0.0
    return when (mix) {
        Mix.MOSTLY_MINE -> if (personalCount >= 1) 0.8 else 0.0
        Mix.MORE_DISCOVERY -> if (personalCount >= 1) 0.2 else 0.0
        Mix.BALANCED -> when {
            personalCount <= 10 -> 0.5
            personalCount <= 30 -> 0.7
            else -> 0.8
        }
    }
}
