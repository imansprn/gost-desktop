/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 GOST Desktop contributors
 */
package xyz.gobliggg.gost.screen.dashboard
import xyz.gobliggg.gost.ui.components.*

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import gost.composeapp.generated.resources.gostLogoPainter
import xyz.gobliggg.gost.ui.theme.*
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun GhostMascot(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Body color
        val mainColor = Color.White
        val shadowColor = Color(0xFFE0E0E0)
        val glowColor = Color.White.copy(alpha = 0.8f)

        // Draw main body (clay-morphism style)
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.8f)
            cubicTo(w * 0.1f, h * 0.4f, w * 0.3f, h * 0.1f, w * 0.5f, h * 0.1f)
            cubicTo(w * 0.7f, h * 0.1f, w * 0.9f, h * 0.4f, w * 0.8f, h * 0.8f)
            lineTo(w * 0.7f, h * 0.75f)
            lineTo(w * 0.6f, h * 0.85f)
            lineTo(w * 0.5f, h * 0.75f)
            lineTo(w * 0.4f, h * 0.85f)
            lineTo(w * 0.3f, h * 0.75f)
            close()
        }

        // 1. Outer Soft Shadow
        drawPath(
            path = path,
            color = Color.Black.copy(alpha = 0.1f),
            style = Fill
        )

        // 2. Main Body with subtle lighting gradient
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(mainColor, shadowColor),
                startY = 0f,
                endY = h
            )
        )

        // 3. Inner Glow (Top Left)
        drawCircle(
            color = glowColor,
            radius = w * 0.25f,
            center = Offset(w * 0.4f, h * 0.3f),
            blendMode = BlendMode.Overlay
        )

        // 4. Eyes
        drawCircle(
            color = Color(0xFF1A1A2E),
            radius = w * 0.04f,
            center = Offset(w * 0.42f, h * 0.45f)
        )
        drawCircle(
            color = Color(0xFF1A1A2E),
            radius = w * 0.04f,
            center = Offset(w * 0.58f, h * 0.45f)
        )
    }
}

class DashboardScreen(
    private val onCreateService: () -> Unit = {},
) : Screen {

@Composable
override fun Content() {
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
                modifier = Modifier.padding(bottom = 120.dp) // Shift upward (Rule of Thirds)
            ) {
            // Hero Graphic - Custom Clay-morphism Ghost
            Box(
                modifier = Modifier
                    .size(280.dp) // Slightly larger
                    .drawBehind {
                        // Soft glow behind the ghost
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(GreenGlow.copy(alpha = 0.3f), Color.Transparent),
                                radius = size.width / 1.5f
                            ),
                            center = center
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Draw a sleek dark server icon box
                Box(
                    modifier = Modifier
                        .size(120.dp) // Much smaller than the ghost
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF131F2E).copy(alpha = 0.5f))
                        .border(1.dp, Color(0xFF1E3A5F).copy(alpha = 0.6f), RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.size(width = 56.dp, height = 24.dp).border(3.dp, Color.White, RoundedCornerShape(8.dp)).padding(start = 8.dp), contentAlignment = Alignment.CenterStart) {
                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color.White))
                        }
                        Box(modifier = Modifier.size(width = 56.dp, height = 24.dp).border(3.dp, Color.White, RoundedCornerShape(8.dp)).padding(start = 8.dp), contentAlignment = Alignment.CenterStart) {
                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color.White))
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xxl))

            Text(
                text = "GOST tunnel wrapper is online.",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp,
            )
            
            Spacer(Modifier.height(Spacing.sm))
            
            Text(
                text = "Local proxy chains, listeners, and forwarding rules are ready to manage.",
                style = MaterialTheme.typography.titleMedium,
                color = DarkTextSlate, // Slate muted color
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Spacing.xxl),
            )

            Spacer(Modifier.height(Spacing.xl))

            // Quick Stats Row (System Feedback)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(SaaSBackground) // Dark pill box
                    .border(1.dp, Color.White.copy(alpha=0.05f), RoundedCornerShape(32.dp))
                    .padding(horizontal = Spacing.lg, vertical = 6.dp)
            ) {
                Text("Core: 127.0.0.1", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Spacer(Modifier.width(16.dp))
                Box(Modifier.size(3.dp).clip(CircleShape).background(Color(0xFF334155))) // Gray separator dot
                Spacer(Modifier.width(16.dp))
                Text("Active tunnels: 4", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Spacer(Modifier.width(16.dp))
                Box(Modifier.size(3.dp).clip(CircleShape).background(Color(0xFF334155))) // Gray separator dot
                Spacer(Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(GreenBright))
                    Spacer(Modifier.width(6.dp))
                    Text("Wrapped", color = GreenBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(64.dp))

            // Pulse Animation setup
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f, // Subtle heartbeat pulse
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "cta_pulse"
            )

            // Hero Button (Premium CTA)
            SaaSButton(
                text = "Open Tunnel Console",
                onClick = onCreateService,
                modifier = Modifier
                    .width(220.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    },
                type = SaaSButtonType.PRIMARY
            )
        }
    }
}
}
}

