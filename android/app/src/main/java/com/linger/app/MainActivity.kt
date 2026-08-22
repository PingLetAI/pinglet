package com.linger.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.linger.app.domain.model.DeepLink
import com.linger.app.navigation.LingerNavHost
import com.linger.app.ui.theme.LingerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = intent?.let { intent: Intent ->
            if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
                intent.getStringExtra(Intent.EXTRA_TEXT)
            } else null
        }

        setContent {
            LingerTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot(sharedText)
                }
            }
        }
    }
}

@Composable
fun AppRoot(sharedText: String?) {
    val navController = rememberNavController()
    val deepLink = sharedText?.let { DeepLink.AddContentText(it) }

    LingerNavHost(
        navController = navController,
        sharedText = deepLink,
    )
}
