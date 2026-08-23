package com.linger.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val LingerInk = Color(0xFF1B1B17)
val LingerPaper = Color(0xFFF8F6F0)
val LingerGold = Color(0xFFDDAE3D)
val LingerClay = Color(0xFFB95F45)
val LingerSage = Color(0xFF66735B)
val LingerMint = Color(0xFFDFF2EA)
val LingerBlush = Color(0xFFF5E2DE)

private val LightColors = lightColorScheme(
    primary = LingerInk,
    onPrimary = Color(0xFFFFF9EC),
    secondary = LingerGold,
    onSecondary = LingerInk,
    tertiary = LingerClay,
    background = LingerPaper,
    onBackground = LingerInk,
    surface = Color(0xFFFFFDF8),
    onSurface = LingerInk,
    surfaceVariant = Color(0xFFECE8DF),
    onSurfaceVariant = Color(0xFF625E55),
    primaryContainer = Color(0xFFE8E5DD),
    onPrimaryContainer = LingerInk,
    secondaryContainer = LingerMint,
    onSecondaryContainer = Color(0xFF233B32),
    tertiaryContainer = LingerBlush,
    onTertiaryContainer = Color(0xFF492A24),
    outline = Color(0xFFD5D0C6),
    outlineVariant = Color(0xFFE7E2D9),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF4E8CE),
    onPrimary = LingerInk,
    secondary = Color(0xFFE7B64B),
    onSecondary = LingerInk,
    tertiary = Color(0xFFE09A80),
    background = Color(0xFF12130F),
    onBackground = Color(0xFFF5EFE2),
    surface = Color(0xFF1B1D18),
    onSurface = Color(0xFFF5EFE2),
    surfaceVariant = Color(0xFF2B2E27),
    onSurfaceVariant = Color(0xFFC9C3B7),
    secondaryContainer = Color(0xFF263D34),
    onSecondaryContainer = Color(0xFFDFF2EA),
    tertiaryContainer = Color(0xFF482E29),
    onTertiaryContainer = Color(0xFFF5E2DE),
)

private val LingerTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium, fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium, fontSize = 27.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 27.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.1.sp),
)

private val LingerShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp),
)

@Composable
fun LingerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = LingerTypography,
        shapes = LingerShapes,
        content = content,
    )
}
