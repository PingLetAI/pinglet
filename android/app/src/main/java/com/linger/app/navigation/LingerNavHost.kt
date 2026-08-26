package com.linger.app.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.linger.app.domain.model.DeepLink
import com.linger.app.ui.add.AddContentScreen
import com.linger.app.ui.discover.DiscoverScreen
import com.linger.app.ui.discover.CatalogDetailScreen
import com.linger.app.ui.discover.CatalogPreferencesScreen
import com.linger.app.ui.detail.ContentDetailScreen
import com.linger.app.ui.home.HomeScreen
import com.linger.app.ui.home.UpcomingScreen
import com.linger.app.ui.library.LibraryScreen
import com.linger.app.ui.queue.ProcessingQueueScreen
import com.linger.app.ui.settings.SettingsScreen
import com.linger.app.ui.trial.TrialOfferScreen
import com.linger.app.ui.account.AccountScreen
import com.linger.app.ui.paywall.PaywallScreen
import com.linger.app.ui.widgetsettings.WidgetSettingsScreen

private data class NavItem(val route: String, val icon: ImageVector, val label: String)
private val navItems = listOf(
    NavItem("home", Icons.Rounded.Home, "Home"),
    NavItem("library", Icons.Rounded.Bookmarks, "Library"),
    NavItem("discover", Icons.Rounded.Explore, "Explore"),
    NavItem("settings", Icons.Rounded.Settings, "Settings"),
)
private val topLevelRoutes = navItems.map { it.route }.toSet()

@Composable
fun LingerNavHost(
    navController: NavHostController,
    sharedText: DeepLink?,
    initialContentId: String? = null,
    onExternalShareFinished: () -> Unit = {},
) {
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "home"
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (route in topLevelRoutes) {
                LingerBottomBar(
                    route = route,
                    onNavigate = { destination ->
                        navController.navigate(destination) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAdd = { navController.navigate("add") },
                )
            }
        },
    ) { padding ->
        NavHost(navController, "home", Modifier.padding(padding)) {
            composable("home") {
                HomeScreen(
                    onOpenContent = { id -> navController.navigate("content/$id") },
                    onUpgrade = { navController.navigate("paywall") },
                    onOpenUpcoming = {
                        navController.navigate("library") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable("library") {
                LibraryScreen(
                    onOpenAdd = { navController.navigate("add") },
                    onOpenQueue = { navController.navigate("queue") },
                    onOpenContent = { id -> navController.navigate("content/$id") },
                )
            }
            composable("add") {
                AddContentScreen(
                    preFillText = (sharedText as? DeepLink.AddContentText)?.text.orEmpty(),
                    onQueued = {
                        if (sharedText != null) {
                            onExternalShareFinished()
                        } else if (!navController.popBackStack()) {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    onCreateAccount = { navController.navigate("account") },
                    onTryPlus = { navController.navigate("trial-offer/SAVE_LIMIT") },
                    onUpgrade = { navController.navigate("paywall") },
                    onBack = {
                        if (sharedText != null) onExternalShareFinished()
                        else navController.popBackStack()
                    },
                )
            }
            composable("queue") { ProcessingQueueScreen(onBack = { navController.popBackStack() }) }
            composable("upcoming") {
                UpcomingScreen(
                    onBack = { navController.popBackStack() },
                    onOpenContent = { id -> navController.navigate("content/$id") },
                )
            }
            composable("content/{contentId}") { backStackEntry ->
                ContentDetailScreen(
                    contentId = backStackEntry.arguments?.getString("contentId").orEmpty(),
                    onBack = { navController.popBackStack() },
                    onUpgrade = { navController.navigate("paywall") },
                    onCreateAccount = { navController.navigate("account") },
                    onTryPlus = { navController.navigate("trial-offer/SAVED_DETAIL") },
                    entitlementRefreshKey = backStackEntry.savedStateHandle["entitlement_changed"] ?: 0L,
                )
            }
        composable("discover") {
            DiscoverScreen(
                onOpenCatalog = { catalogId -> navController.navigate("catalog/$catalogId") },
                onOpenPreferences = { navController.navigate("catalog-preferences") },
            )
        }
        composable("catalog/{catalogId}") {
            CatalogDetailScreen(onBack = { navController.popBackStack() })
        }
        composable("catalog-preferences") {
            CatalogPreferencesScreen(onBack = { navController.popBackStack() })
        }
            composable("settings") {
                SettingsScreen(
                    onCreateAccount = { navController.navigate("account") },
                    onTryPlus = { navController.navigate("trial-offer/SETTINGS") },
                    onUpgrade = { navController.navigate("paywall") },
                    onOpenQueue = { navController.navigate("queue") },
                    onOpenWidgetSettings = { navController.navigate("widget-settings") },
                    onAccountReset = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    entitlementRefreshKey = it.savedStateHandle["entitlement_changed"] ?: 0L,
                )
            }
            composable("widget-settings") {
                WidgetSettingsScreen(
                    onBack = { navController.popBackStack() },
                    onTryPlus = { navController.navigate("trial-offer/WIDGET_SETTINGS") },
                    onUpgrade = { navController.navigate("paywall") },
                )
            }
            composable("account") {
                AccountScreen(
                    onBack = { navController.popBackStack() },
                    onVerified = {
                        navController.navigate("trial-offer/ACCOUNT_VERIFIED") {
                            popUpTo("account") { inclusive = true }
                        }
                    },
                )
            }
            composable("trial-offer/{source}") { backStackEntry ->
                val source = backStackEntry.arguments?.getString("source") ?: "UNKNOWN"
                TrialOfferScreen(
                    entrySource = source,
                    onBack = { navController.popBackStack() },
                    onActivated = {
                        navController.previousBackStackEntry?.savedStateHandle?.set("entitlement_changed", System.currentTimeMillis())
                        navController.popBackStack()
                    },
                    onContinueFree = { navController.popBackStack() },
                    onViewPlans = {
                        navController.navigate("paywall") {
                            popUpTo("trial-offer/{source}") { inclusive = true }
                        }
                    },
                )
            }
            composable("paywall") {
                PaywallScreen(
                    onBack = { navController.popBackStack() },
                    onPurchased = {
                        navController.previousBackStackEntry?.savedStateHandle?.set("entitlement_changed", System.currentTimeMillis())
                        navController.popBackStack()
                    },
                )
            }
        }
    }
    LaunchedEffect(sharedText) { if (sharedText != null) navController.navigate("add") }
    LaunchedEffect(initialContentId) {
        initialContentId?.takeIf { it.isNotBlank() }?.let { navController.navigate("content/$it") }
    }
}

@Composable
private fun LingerBottomBar(route: String, onNavigate: (String) -> Unit, onAdd: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f))
            Row(
                Modifier.fillMaxWidth().navigationBarsPadding().height(68.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                navItems.take(2).forEach { item -> NavDestination(item, route == item.route, onNavigate) }
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    FilledIconButton(
                        onClick = onAdd,
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                        ),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add a PingLet", modifier = Modifier.size(26.dp))
                    }
                }
                navItems.drop(2).forEach { item -> NavDestination(item, route == item.route, onNavigate) }
            }
        }
    }
}

@Composable
private fun RowScope.NavDestination(item: NavItem, selected: Boolean, onNavigate: (String) -> Unit) {
    NavigationBarItem(
        selected = selected,
        onClick = { onNavigate(item.route) },
        modifier = Modifier.weight(1f),
        icon = { Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(23.dp)) },
        label = { Text(item.label, style = MaterialTheme.typography.labelMedium) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.34f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}
