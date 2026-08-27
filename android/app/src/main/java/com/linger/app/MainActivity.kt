package com.linger.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.linger.app.data.local.DataStoreManager
import com.linger.app.domain.model.DeepLink
import com.linger.app.navigation.LingerNavHost
import com.linger.app.ui.theme.LingerTheme
import com.linger.app.ui.widget.WidgetInstallPrompt
import com.linger.app.ui.widget.WidgetManualInstallDialog
import com.linger.app.widget.WidgetPinning
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var dataStoreManager: DataStoreManager

    private var sharedText by mutableStateOf<String?>(null)
    private var widgetContentId by mutableStateOf<String?>(null)
    private var showWidgetInstallPrompt by mutableStateOf(false)
    private var showWidgetManualHelp by mutableStateOf(false)
    private var widgetPinRequestPending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sharedText = intent.sharedText()
        widgetContentId = intent.getStringExtra(EXTRA_CONTENT_ID)

        setContent {
            LingerTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot(
                        sharedText = sharedText,
                        widgetContentId = widgetContentId,
                        onExternalShareFinished = { finish() },
                        showWidgetInstallPrompt = showWidgetInstallPrompt,
                        onAddWidget = { completeWidgetInstallPrompt(addWidget = true) },
                        onDismissWidgetPrompt = { completeWidgetInstallPrompt(addWidget = false) },
                        showWidgetManualHelp = showWidgetManualHelp,
                        onDismissWidgetManualHelp = { showWidgetManualHelp = false },
                        onOpenHomeSettings = { openHomeSettings() },
                    )
                }
            }
        }

        prepareWidgetPinPromptOnFirstLauncherOpen(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedText = intent.sharedText()
        widgetContentId = intent.getStringExtra(EXTRA_CONTENT_ID)
    }

    override fun onResume() {
        super.onResume()
        if (widgetPinRequestPending) {
            lifecycleScope.launch {
                delay(300L)
                resolvePendingWidgetPinRequest()
            }
        }
    }

    private fun Intent?.sharedText(): String? = this?.takeIf {
        it.action == Intent.ACTION_SEND && it.type == "text/plain"
    }?.getStringExtra(Intent.EXTRA_TEXT)

    private fun prepareWidgetPinPromptOnFirstLauncherOpen(launchIntent: Intent?) {
        val isLauncherOpen = launchIntent?.action == Intent.ACTION_MAIN &&
            launchIntent.hasCategory(Intent.CATEGORY_LAUNCHER)
        if (!isLauncherOpen) return

        lifecycleScope.launch {
            if (dataStoreManager.readWidgetPinPromptVersion() >= WIDGET_PIN_PROMPT_REVISION) return@launch

            if (WidgetPinning.hasInstalledWidget(this@MainActivity)) {
                dataStoreManager.setWidgetPinPromptVersion(WIDGET_PIN_PROMPT_REVISION)
                return@launch
            }

            if (!WidgetPinning.isPinningSupported(this@MainActivity)) {
                dataStoreManager.setWidgetPinPromptVersion(WIDGET_PIN_PROMPT_REVISION)
                showWidgetManualHelp = true
                return@launch
            }

            delay(WIDGET_PIN_PROMPT_DELAY_MS)
            if (isFinishing || isDestroyed) return@launch
            showWidgetInstallPrompt = true
        }
    }

    private fun completeWidgetInstallPrompt(addWidget: Boolean) {
        showWidgetInstallPrompt = false
        lifecycleScope.launch {
            dataStoreManager.setWidgetPinPromptVersion(WIDGET_PIN_PROMPT_REVISION)
            if (!addWidget || isFinishing || isDestroyed) return@launch

            val requestSent = WidgetPinning.requestPin(this@MainActivity)
            if (!requestSent) {
                showWidgetManualHelp = true
            } else {
                widgetPinRequestPending = true
                delay(WIDGET_PIN_RESULT_FALLBACK_MS)
                if (widgetPinRequestPending && hasWindowFocus()) {
                    resolvePendingWidgetPinRequest()
                }
            }
        }
    }

    private fun resolvePendingWidgetPinRequest() {
        if (!widgetPinRequestPending) return
        widgetPinRequestPending = false
        if (!WidgetPinning.hasInstalledWidget(this)) {
            showWidgetManualHelp = true
        }
    }

    private fun openHomeSettings() {
        runCatching { startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }
            .onSuccess { showWidgetManualHelp = false }
    }

    companion object {
        const val EXTRA_CONTENT_ID = "widget_content_id"
        private const val WIDGET_PIN_PROMPT_DELAY_MS = 700L
        private const val WIDGET_PIN_RESULT_FALLBACK_MS = 2_000L
        private const val WIDGET_PIN_PROMPT_REVISION = 2
    }
}

@Composable
fun AppRoot(
    sharedText: String?,
    widgetContentId: String?,
    onExternalShareFinished: () -> Unit,
    showWidgetInstallPrompt: Boolean,
    onAddWidget: () -> Unit,
    onDismissWidgetPrompt: () -> Unit,
    showWidgetManualHelp: Boolean,
    onDismissWidgetManualHelp: () -> Unit,
    onOpenHomeSettings: () -> Unit,
) {
    val navController = rememberNavController()
    val deepLink = sharedText?.let { DeepLink.AddContentText(it) }

    LingerNavHost(
        navController = navController,
        sharedText = deepLink,
        initialContentId = widgetContentId,
        onExternalShareFinished = onExternalShareFinished,
    )

    if (showWidgetInstallPrompt) {
        WidgetInstallPrompt(
            onAddWidget = onAddWidget,
            onDismiss = onDismissWidgetPrompt,
        )
    }

    if (showWidgetManualHelp) {
        WidgetManualInstallDialog(
            onOpenHomeSettings = onOpenHomeSettings,
            onDismiss = onDismissWidgetManualHelp,
        )
    }
}
