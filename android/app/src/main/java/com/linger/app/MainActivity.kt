package com.linger.app

import android.content.Intent
import android.os.Bundle
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
import androidx.navigation.compose.rememberNavController
import com.linger.app.domain.model.DeepLink
import com.linger.app.navigation.LingerNavHost
import com.linger.app.ui.theme.LingerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var sharedText by mutableStateOf<String?>(null)
    private var widgetContentId by mutableStateOf<String?>(null)

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
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedText = intent.sharedText()
        widgetContentId = intent.getStringExtra(EXTRA_CONTENT_ID)
    }

    private fun Intent?.sharedText(): String? = this?.takeIf {
        it.action == Intent.ACTION_SEND && it.type == "text/plain"
    }?.getStringExtra(Intent.EXTRA_TEXT)

    companion object {
        const val EXTRA_CONTENT_ID = "widget_content_id"
    }
}

@Composable
fun AppRoot(
    sharedText: String?,
    widgetContentId: String?,
    onExternalShareFinished: () -> Unit,
) {
    val navController = rememberNavController()
    val deepLink = sharedText?.let { DeepLink.AddContentText(it) }

    LingerNavHost(
        navController = navController,
        sharedText = deepLink,
        initialContentId = widgetContentId,
        onExternalShareFinished = onExternalShareFinished,
    )
}
