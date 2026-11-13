package com.ccnaacademy.app.ui.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp

// Navy / deep-blue identity with cyan accents. Text-on-color pairs meet WCAG AA (≥4.5:1) for body text.
private val Light = lightColorScheme(
    primary = Color(0xFF0B3D91), onPrimary = Color.White,                       // deep blue
    primaryContainer = Color(0xFFDCE6FA), onPrimaryContainer = Color(0xFF051A40),
    secondary = Color(0xFF00708A), onSecondary = Color.White,                   // cyan (darkened for contrast on white)
    secondaryContainer = Color(0xFFCFF1F8), onSecondaryContainer = Color(0xFF00242D),
    tertiary = Color(0xFF13294B), onTertiary = Color.White,                     // navy
    tertiaryContainer = Color(0xFFDDE3EE), onTertiaryContainer = Color(0xFF0B1A33),
    background = Color(0xFFF4F6FA), onBackground = Color(0xFF111827),
    surface = Color(0xFFF4F6FA), onSurface = Color(0xFF111827), onSurfaceVariant = Color(0xFF4A5568),
    surfaceVariant = Color(0xFFE2E8F0), surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFFFFF), surfaceContainer = Color(0xFFEDF1F7), surfaceContainerHigh = Color(0xFFE5EAF2),
    surfaceContainerHighest = Color(0xFFDDE3EC),
    error = Color(0xFFB3261E), outline = Color(0xFF6B7684), outlineVariant = Color(0xFFCBD3DE)
)
private val Dark = darkColorScheme(
    primary = Color(0xFF9EC2FF), onPrimary = Color(0xFF002E6B),
    primaryContainer = Color(0xFF123A7A), onPrimaryContainer = Color(0xFFDCE6FA),
    secondary = Color(0xFF5ED6EC), onSecondary = Color(0xFF00363F),             // cyan accent
    secondaryContainer = Color(0xFF004E5C), onSecondaryContainer = Color(0xFFCFF1F8),
    tertiary = Color(0xFFB9C8E6), onTertiary = Color(0xFF1B2B47),
    tertiaryContainer = Color(0xFF26375A), onTertiaryContainer = Color(0xFFDDE3EE),
    background = Color(0xFF0A1222), onBackground = Color(0xFFE6EAF2),           // navy
    surface = Color(0xFF0A1222), onSurface = Color(0xFFE6EAF2), onSurfaceVariant = Color(0xFFB4BDCB),
    surfaceVariant = Color(0xFF2A3549), surfaceContainerLowest = Color(0xFF070D19),
    surfaceContainerLow = Color(0xFF101A2E), surfaceContainer = Color(0xFF142039), surfaceContainerHigh = Color(0xFF1A2843),
    surfaceContainerHighest = Color(0xFF22314F),
    error = Color(0xFFFFB4AB), outline = Color(0xFF8893A5), outlineVariant = Color(0xFF34415A)
)

/** Semantic colors used across the app (success / warning / exam-required / supplementary). */
data class Semantic(val success: Color, val warning: Color, val danger: Color, val exam: Color, val supplementary: Color, val dark: Boolean)
val LocalSemantic = staticCompositionLocalOf { Semantic(Color.Green, Color.Yellow, Color.Red, Color.Blue, Color.Gray, false) }

@Composable
fun CcnaTheme(darkTheme: Boolean, dynamicColor: Boolean, content: @Composable () -> Unit) {
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(LocalContext.current) else dynamicLightColorScheme(LocalContext.current)
        darkTheme -> Dark
        else -> Light
    }
    val sem = if (darkTheme) Semantic(Color(0xFF7BD88F), Color(0xFFFFC966), Color(0xFFFF8A80), Color(0xFF9EC2FF), Color(0xFF5ED6EC), true)
              else Semantic(Color(0xFF1B6E30), Color(0xFF9A5B00), Color(0xFFB3261E), Color(0xFF0B3D91), Color(0xFF00708A), false)
    CompositionLocalProvider(LocalSemantic provides sem) {
        MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
    }
}

/** Slightly roomier line heights for long-form technical reading. */
private val AppTypography = Typography().run {
    copy(
        bodyLarge = bodyLarge.copy(lineHeight = 26.sp),
        bodyMedium = bodyMedium.copy(lineHeight = 22.sp),
        titleLarge = titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
        headlineSmall = headlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    )
}
