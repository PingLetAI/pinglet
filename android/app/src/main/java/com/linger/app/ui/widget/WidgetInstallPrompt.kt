package com.linger.app.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun WidgetInstallPrompt(
    onAddWidget: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFFF8F5ED),
            tonalElevation = 0.dp,
            shadowElevation = 18.dp,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFFDDAE3D), CircleShape),
                    )
                    Text(
                        text = "YOUR HOME SCREEN",
                        color = Color(0xFFAE4F39),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    )
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Bring PingLet Home.",
                    color = Color(0xFF171914),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "See meaningful ideas return throughout your day.",
                    color = Color(0xFF4E504A),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = Color(0xFF171914),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                        Text(
                            text = "PINGLET",
                            color = Color(0xFFDDAE3D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "What you keep should find its way back.",
                            color = Color(0xFFF8F6F0),
                            style = MaterialTheme.typography.titleMedium,
                            lineHeight = 24.sp,
                        )
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 54.dp, height = 4.dp)
                                .background(Color(0xFFDDAE3D), RoundedCornerShape(2.dp)),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onAddWidget,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF171914),
                        contentColor = Color(0xFFF8F6F0),
                    ),
                ) {
                    Text(
                        text = "ADD WIDGET",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp,
                    )
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "NOT NOW",
                        color = Color(0xFF4E504A),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun WidgetManualInstallDialog(
    onOpenHomeSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFFF8F5ED),
            shadowElevation = 18.dp,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "WIDGET ACCESS",
                    color = Color(0xFFAE4F39),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Widget setup needs attention.",
                    color = Color(0xFF171914),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Your launcher did not add PingLet. Confirm One UI Home is your default Home app and that Home screen layout is unlocked.",
                    color = Color(0xFF4E504A),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onOpenHomeSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF171914),
                        contentColor = Color(0xFFF8F6F0),
                    ),
                ) {
                    Text("CHECK HOME APP", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("NOT NOW", color = Color(0xFF4E504A), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
