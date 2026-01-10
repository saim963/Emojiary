package `in`.cintech.moodmosaic.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material3.*
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// Mood Colors - Predefined palette for mood selection
object MoodColors {
    val Joyful = Color(0xFFFFD93D)      // Bright Yellow
    val Happy = Color(0xFFFF9500)        // Orange
    val Calm = Color(0xFF6BCB77)         // Green
    val Peaceful = Color(0xFF4ECDC4)     // Teal
    val Neutral = Color(0xFF95A5A6)      // Gray
    val Tired = Color(0xFF74B9FF)        // Light Blue
    val Anxious = Color(0xFFDDA0DD)      // Plum
    val Sad = Color(0xFF5DADE2)          // Sky Blue
    val Stressed = Color(0xFFE74C3C)     // Red
    val Angry = Color(0xFFC0392B)        // Dark Red
    val Grateful = Color(0xFFFF6B6B)     // Coral
    val Excited = Color(0xFFE056FD)      // Purple
    val Romantic = Color(0xFFFF85A2)     // Pink
    val Creative = Color(0xFF00D2D3)     // Cyan
    val Focused = Color(0xFF0984E3)      // Blue

    val allColors = listOf(
        Joyful, Happy, Calm, Peaceful, Neutral, Tired,
        Anxious, Sad, Stressed, Angry, Grateful, Excited,
        Romantic, Creative, Focused
    )

    val colorNames = mapOf(
        Joyful to "Joyful",
        Happy to "Happy",
        Calm to "Calm",
        Peaceful to "Peaceful",
        Neutral to "Neutral",
        Tired to "Tired",
        Anxious to "Anxious",
        Sad to "Sad",
        Stressed to "Stressed",
        Angry to "Angry",
        Grateful to "Grateful",
        Excited to "Excited",
        Romantic to "Romantic",
        Creative to "Creative",
        Focused to "Focused"
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6BCB77),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A23),
    onPrimaryContainer = Color(0xFF98F69D),
    secondary = Color(0xFF4ECDC4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E3836),
    onSecondaryContainer = Color(0xFF8EF4EC),
    tertiary = Color(0xFFE056FD),
    onTertiary = Color.White,
    background = Color(0xFF0D0D0D),
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFCACACA),
    outline = Color(0xFF3D3D3D),
    outlineVariant = Color(0xFF2D2D2D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2D9F3A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4F5D6),
    onPrimaryContainer = Color(0xFF1A3D1E),
    secondary = Color(0xFF38B2AC),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0F0ED),
    onSecondaryContainer = Color(0xFF1A3634),
    tertiary = Color(0xFFC026D3),
    onTertiary = Color.White,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF4A4A4A),
    outline = Color(0xFFD0D0D0),
    outlineVariant = Color(0xFFE8E8E8)
)

@Composable
fun MoodMosaicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

//Typography is replaced by AppTypography
val AppTypography = Typography(
    displayLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)