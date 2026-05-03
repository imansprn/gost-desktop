/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 GOST Desktop contributors
 */
package xyz.gobliggg.gost.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.map
import androidx.compose.ui.graphics.Color

@Immutable
data class GostSemanticColors(
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val textOnAccent: Color,
    val surfaceApp: Color,
    val surfacePanel: Color,
    val surfaceCard: Color,
    val surfaceInput: Color,
    val borderSubtle: Color,
    val borderStrong: Color,
    val dividerSubtle: Color,
    val stateHover: Color,
    val statePressed: Color,
    val stateSelected: Color,
    val focusRing: Color,
    val statusSuccess: Color,
    val statusSuccessContainer: Color,
    val statusWarning: Color,
    val statusWarningContainer: Color,
    val statusError: Color,
    val statusErrorContainer: Color,
    val statusInfo: Color,
    val statusInfoContainer: Color,
    val brandGradientStart: Color,
    val brandGradientEnd: Color,
)

internal val LocalGostSemanticColors: ProvidableCompositionLocal<GostSemanticColors> =
    staticCompositionLocalOf {
        error("GostSemanticColors not provided. Wrap your UI in GostTheme.")
    }

object GostSemantics {
    val colors: GostSemanticColors
        @Composable get() = LocalGostSemanticColors.current

    val typography: androidx.compose.material3.Typography
        @Composable get() = MaterialTheme.typography
}

@Composable
internal fun rememberGostSemanticColors(dark: Boolean): GostSemanticColors {
    val accent by remember {
        xyz.gobliggg.gost.data.AppState.settings.map { it.accentColor }
    }.collectAsState(xyz.gobliggg.gost.data.AppState.settings.value.accentColor)

    val (start, end) =
        when (accent) {
            xyz.gobliggg.gost.model.AccentColor.CYAN -> GreenBright to BlueBright
            xyz.gobliggg.gost.model.AccentColor.EMERALD -> Emerald500 to Teal400
            xyz.gobliggg.gost.model.AccentColor.INDIGO -> BlueBright to Violet500
            xyz.gobliggg.gost.model.AccentColor.AMBER -> OrangeBright to RedStatus
        }

    // These choices intentionally preserve the current “dark SaaS” aesthetic while
    // enabling a single semantic source of truth for components.
    return if (dark) {
        val infoAccent = if (accent == xyz.gobliggg.gost.model.AccentColor.AMBER) OrangeBright else start
        GostSemanticColors(
            // Match previous UI: white-forward typography with slate for muted labels.
            textPrimary = Color.White,
            textSecondary = Color.White.copy(alpha = 0.7f),
            textMuted = DarkTextSlate,
            textDisabled = Color.White.copy(alpha = 0.5f),
            textOnAccent = Color.White,
            surfaceApp = SaaSBackground,
            surfacePanel = SaASSurface,
            surfaceCard = SaASSurface,
            surfaceInput = SaaSInputBg,
            borderSubtle = Color.White.copy(alpha = 0.05f),
            borderStrong = GlassBorderHigh,
            dividerSubtle = Color.White.copy(alpha = 0.03f),
            stateHover = Color.White.copy(alpha = 0.05f),
            statePressed = Color.White.copy(alpha = 0.08f),
            stateSelected = SaaSSelection,
            focusRing = start,
            statusSuccess = GreenStatus,
            statusSuccessContainer = GreenStatus.copy(alpha = 0.10f),
            statusWarning = AmberStatus,
            statusWarningContainer = AmberStatus.copy(alpha = 0.12f),
            statusError = RedStatus,
            statusErrorContainer = RedStatus.copy(alpha = 0.10f),
            statusInfo = infoAccent,
            statusInfoContainer = infoAccent.copy(alpha = 0.12f),
            brandGradientStart = start,
            brandGradientEnd = end,
        )
    } else {
        // Fallback for light (though we hardcode dark, keeping this clean)
        GostSemanticColors(
            textPrimary = LightTextSlate900,
            textSecondary = LightTextSlate500,
            textMuted = LightTextSlate400,
            textDisabled = LightTextSlate400.copy(alpha = 0.5f),
            textOnAccent = Color.White,
            surfaceApp = LightBgGradientEnd,
            surfacePanel = LightSidebarBg,
            surfaceCard = LightHopCardBg,
            surfaceInput = LightInputBg,
            borderSubtle = LightEditorBorder.copy(alpha = 0.5f),
            borderStrong = LightEditorBorder,
            dividerSubtle = LightEditorBorder.copy(alpha = 0.3f),
            stateHover = start.copy(alpha = 0.05f),
            statePressed = start.copy(alpha = 0.1f),
            stateSelected = start.copy(alpha = 0.1f),
            focusRing = start,
            statusSuccess = Emerald500,
            statusSuccessContainer = Emerald500.copy(alpha = 0.10f),
            statusWarning = AmberStatusDark,
            statusWarningContainer = AmberStatusDark.copy(alpha = 0.12f),
            statusError = Rose500,
            statusErrorContainer = Rose500.copy(alpha = 0.10f),
            statusInfo = start,
            statusInfoContainer = start.copy(alpha = 0.12f),
            brandGradientStart = start,
            brandGradientEnd = end,
        )
    }
}
