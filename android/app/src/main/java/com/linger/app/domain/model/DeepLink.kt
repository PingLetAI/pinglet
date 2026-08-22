package com.linger.app.domain.model

sealed interface DeepLink {
    data class AddContentText(val text: String) : DeepLink
}
