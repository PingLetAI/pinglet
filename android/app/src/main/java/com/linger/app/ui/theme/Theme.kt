package com.linger.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linger.app.R

val LingerInk = Color(0xFF12140F)
val LingerPaper = Color(0xFFF0ECE3)
val LingerGold = Color(0xFFDDAE3D)
val LingerClay = Color(0xFFA84F38)
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
    surface = Color(0xFFFFFFFF),
    onSurface = LingerInk,
    surfaceVariant = Color(0xFFE2DDD3),
    onSurfaceVariant = Color(0xFF24261F),
    primaryContainer = Color(0xFFE8E5DD),
    onPrimaryContainer = LingerInk,
    secondaryContainer = Color(0xFFC5E2D5),
    onSecondaryContainer = Color(0xFF233B32),
    tertiaryContainer = Color(0xFFECCBC3),
    onTertiaryContainer = Color(0xFF492A24),
    outline = Color(0xFF9F998D),
    outlineVariant = Color(0xFFC7C0B4),
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

private val Editorial = FontFamily(Font(R.font.newsreader, weight = FontWeight.Normal), Font(R.font.newsreader, weight = FontWeight.SemiBold))
private val Interface = FontFamily(
    Font(R.font.manrope_regular, weight = FontWeight.Normal),
    Font(R.font.manrope_medium, weight = FontWeight.Medium),
    Font(R.font.manrope_semibold, weight = FontWeight.SemiBold),
    Font(R.font.manrope_bold, weight = FontWeight.Bold),
)

private val LingerTypography = Typography(
    displaySmall = TextStyle(fontFamily = Editorial, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = (-0.3).sp),
    headlineMedium = TextStyle(fontFamily = Editorial, fontWeight = FontWeight.Medium, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = Editorial, fontWeight = FontWeight.SemiBold, fontSize = 23.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = Editorial, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = Interface, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Interface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Interface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Interface, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = Interface, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = .65.sp),
    labelMedium = TextStyle(fontFamily = Interface, fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = .7.sp),
)

private val LingerShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp),
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
