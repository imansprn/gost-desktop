package xyz.gobliggg.gost.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
    darkColorScheme(
        primary = GreenBright,
        onPrimary = Color.White,
        primaryContainer = SaaSSelection,
        onPrimaryContainer = Color.White,
        secondary = BlueBright,
        onSecondary = Color.White,
        secondaryContainer = BlueDeep,
        onSecondaryContainer = Color.White,
        tertiary = GreenStatus,
        onTertiary = Color.Black,
        tertiaryContainer = GreenDeep,
        onTertiaryContainer = GreenStatus,
        background = SaaSBackground,
        onBackground = DarkTextPrimary,
        surface = SaaSBackground,
        onSurface = DarkTextPrimary,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkTextSecondary,
        surfaceContainerLowest = Color(0xFF040A12),
        surfaceContainerLow = Color(0xFF0C141E),
        surfaceContainer = DarkSurfaceVariant,
        surfaceContainerHigh = GlassWhite,
        surfaceContainerHighest = GlassWhiteHigh,
        outline = GlassBorder,
        outlineVariant = GlassBorderHigh,
        error = RedStatus,
        onError = Color.White,
        errorContainer = Color(0xFF5E1F1F),
        onErrorContainer = Color(0xFFFFEBEE),
        inverseSurface = LightSurface,
        inverseOnSurface = LightTextPrimary,
        inversePrimary = GreenGlow,
        scrim = Color(0x99000000),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Violet500,
        onPrimary = Color.White,
        primaryContainer = Violet50,
        onPrimaryContainer = Violet500,
        secondary = Teal400,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFB2DFDB),
        onSecondaryContainer = Color(0xFF004D40),
        tertiary = Emerald500,
        onTertiary = Color.White,
        tertiaryContainer = GreenStatusLight,
        onTertiaryContainer = GreenStatusDark,
        background = LightBgGradientEnd, // White
        onBackground = LightTextSlate900,
        surface = LightSidebarBg, // White
        onSurface = LightTextSlate900,
        surfaceVariant = LightEditorBg, // White
        onSurfaceVariant = LightTextSlate500,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = LightHopCardBg, // Slate 50
        surfaceContainer = LightSurfaceVariant,
        surfaceContainerHigh = LightSurfaceElevated,
        surfaceContainerHighest = LightSurfaceCard,
        outline = LightEditorBorder, // Slate 200
        outlineVariant = LightTextSlate400,
        error = Rose500,
        onError = Color.White,
        errorContainer = RedStatusLight,
        onErrorContainer = RedStatusDark,
        inverseSurface = DarkSurface,
        inverseOnSurface = DarkTextPrimary,
        inversePrimary = Cyan300,
        scrim = OverlayBg,
    )

@Composable
fun GostTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = GostTypography) {
        CompositionLocalProvider(
            LocalGostSemanticColors provides rememberGostSemanticColors(darkTheme),
        ) {
            content()
        }
    }
}
