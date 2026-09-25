/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 GOST Desktop contributors
 */
package xyz.gobliggg.gost.screen.dashboard
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import gost.composeapp.generated.resources.gostLogoPainter
import xyz.gobliggg.gost.data.AppState
import xyz.gobliggg.gost.data.ServiceRegistry
import xyz.gobliggg.gost.data.ServiceStatus
import xyz.gobliggg.gost.ui.components.*
import xyz.gobliggg.gost.ui.theme.*

private object DashboardDimensions {
    val heroGraphic = 120.dp
    val metadataSeparator = 3.dp
    val statusDot = 6.dp
    val heroButtonWidth = 220.dp
}

class DashboardScreen(
    private val onCreateService: () -> Unit = {},
) : Screen {
    @Composable
    override fun Content() {
        val sc = GostSemantics.colors
        val engineRunning by AppState.isEngineRunning.collectAsState()
        val services by ServiceRegistry.default().services.collectAsState()
        val activeTunnelCount = services.count { it.status == ServiceStatus.RUNNING }

        ScreenScaffold(
            header = {
                SaaSScreenHeader(
                    superTitle = "OVERVIEW",
                    title = "Dashboard",
                )
            },
        ) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = Spacing.xxxl),
                ) {
                    Image(
                        painter = gostLogoPainter(),
                        contentDescription = "GOST logo",
                        modifier = Modifier.size(DashboardDimensions.heroGraphic),
                    )

                    Spacer(Modifier.height(Spacing.xxl))

                    Text(
                        text =
                            if (engineRunning) {
                                "GOST tunnel engine is active."
                            } else {
                                "GOST tunnel engine is stopped."
                            },
                        style = MaterialTheme.typography.headlineMedium,
                        color = sc.textPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(Spacing.sm))

                    Text(
                        text =
                            if (engineRunning) {
                                "Local proxy chains, listeners, and forwarding rules are ready to manage."
                            } else {
                                "Start the engine to run tunnels. Configuration remains available while stopped."
                            },
                        style = MaterialTheme.typography.titleMedium,
                        color = sc.textMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = Spacing.xxl),
                    )

                    Spacer(Modifier.height(Spacing.xl))

                    // Quick Stats Pill
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(GostRadius.pill))
                                .background(sc.surfaceApp)
                                .border(GostControlSize.borderWidth, sc.borderSubtle, RoundedCornerShape(GostRadius.pill))
                                .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                    ) {
                        Text(
                            "Configured tunnels: ${services.size}",
                            color = sc.textMuted,
                            style = GostTextStyles.pillLabel,
                        )
                        Spacer(Modifier.width(Spacing.lg))
                        Box(Modifier.size(DashboardDimensions.metadataSeparator).clip(CircleShape).background(sc.textDisabled))
                        Spacer(Modifier.width(Spacing.lg))
                        Text(
                            "Active tunnels: $activeTunnelCount",
                            color = sc.textMuted,
                            style = GostTextStyles.pillLabel,
                        )
                        Spacer(Modifier.width(Spacing.lg))
                        Box(Modifier.size(DashboardDimensions.metadataSeparator).clip(CircleShape).background(sc.textDisabled))
                        Spacer(Modifier.width(Spacing.lg))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val statusColor = if (engineRunning) sc.statusSuccess else sc.textMuted
                            Box(Modifier.size(DashboardDimensions.statusDot).clip(CircleShape).background(statusColor))
                            Spacer(Modifier.width(Spacing.sm))
                            Text(
                                if (engineRunning) "Engine Active" else "Engine Stopped",
                                color = statusColor,
                                style = GostTextStyles.pillLabel.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.xxxl))

                    // Hero Button (static, no pulse)
                    SaaSButton(
                        text = "Open Tunnel Console",
                        onClick = onCreateService,
                        modifier = Modifier.width(DashboardDimensions.heroButtonWidth),
                        type = SaaSButtonType.PRIMARY,
                    )
                }
            }
        }
    }
}
