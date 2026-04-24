/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 GOST Desktop contributors
 */
package xyz.gobliggg.gost.screen.dashboard
import xyz.gobliggg.gost.ui.components.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import xyz.gobliggg.gost.ui.theme.*

class DashboardScreen(
    private val onCreateService: () -> Unit = {},
) : Screen {

    @Composable
    override fun Content() {
        val sc = GostSemantics.colors

        ScreenScaffold(
            header = {
                SaaSScreenHeader(
                    superTitle = "OVERVIEW",
                    title = "Dashboard"
                )
            }
        ) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = Spacing.xxxl)
                ) {
                    // Hero Graphic — sleek server icon
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(Spacing.xxl))
                            .background(sc.surfacePanel.copy(alpha = 0.5f))
                            .border(1.dp, sc.borderSubtle, RoundedCornerShape(Spacing.xxl)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 56.dp, height = 24.dp)
                                    .border(3.dp, sc.textPrimary, RoundedCornerShape(GostRadius.sm))
                                    .padding(start = Spacing.sm),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(sc.textPrimary)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(width = 56.dp, height = 24.dp)
                                    .border(3.dp, sc.textPrimary, RoundedCornerShape(GostRadius.sm))
                                    .padding(start = Spacing.sm),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(sc.textPrimary)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.xxl))

                    Text(
                        text = "GOST tunnel wrapper is online.",
                        style = MaterialTheme.typography.headlineMedium,
                        color = sc.textPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(Spacing.sm))

                    Text(
                        text = "Local proxy chains, listeners, and forwarding rules are ready to manage.",
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
                        modifier = Modifier
                            .clip(RoundedCornerShape(GostRadius.pill))
                            .background(sc.surfaceApp)
                            .border(1.dp, sc.borderSubtle, RoundedCornerShape(GostRadius.pill))
                            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                    ) {
                        Text("Core: 127.0.0.1", color = sc.textMuted, fontSize = 11.sp)
                        Spacer(Modifier.width(Spacing.lg))
                        Box(Modifier.size(3.dp).clip(CircleShape).background(sc.textDisabled))
                        Spacer(Modifier.width(Spacing.lg))
                        Text("Active tunnels: 4", color = sc.textMuted, fontSize = 11.sp)
                        Spacer(Modifier.width(Spacing.lg))
                        Box(Modifier.size(3.dp).clip(CircleShape).background(sc.textDisabled))
                        Spacer(Modifier.width(Spacing.lg))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(sc.statusSuccess))
                            Spacer(Modifier.width(Spacing.sm))
                            Text("Wrapped", color = sc.statusSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(Spacing.xxxl))

                    // Hero Button (static, no pulse)
                    SaaSButton(
                        text = "Open Tunnel Console",
                        onClick = onCreateService,
                        modifier = Modifier.width(220.dp),
                        type = SaaSButtonType.PRIMARY
                    )
                }
            }
        }
    }
}
