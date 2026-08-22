package com.linger.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.linger.app.domain.model.DeepLink
import com.linger.app.ui.add.AddContentScreen
import com.linger.app.ui.discover.DiscoverScreen
import com.linger.app.ui.home.HomeScreen
import com.linger.app.ui.library.LibraryScreen
import com.linger.app.ui.settings.SettingsScreen

sealed interface Route {
    data object Home : Route
    data object Library : Route
    data object Add : Route
    data object Discover : Route
    data object Settings : Route
}

@Composable
fun LingerNavHost(navController: NavHostController, sharedText: DeepLink?) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(onOpenAdd = { navController.navigate("add") }) }
        composable("library") { LibraryScreen() }
        composable("add") { AddContentScreen(preFillText = (sharedText as? DeepLink.AddContentText)?.text.orEmpty()) }
        composable("discover") { DiscoverScreen() }
        composable("settings") { SettingsScreen() }
    }

    val hasSharedText by remember(sharedText) { mutableStateOf(sharedText != null) }
    if (hasSharedText) {
        LaunchedEffect(sharedText) {
            navController.navigate("add")
        }
    }
}
