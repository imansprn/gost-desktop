package xyz.gobliggg.gost.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gost.composeapp.generated.resources.gostLogoPainter
import xyz.gobliggg.gost.ui.theme.*
import xyz.gobliggg.gost.ui.theme.Spacing

/**
 * Navigation sidebar item definition.
 */
data class SidebarItem(
    val id: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badge: String? = null,
)

/**
 * Sidebar navigation for the GOST Desktop app shell.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Sidebar(
    items: List<SidebarItem>,
    selectedId: String,
    collapsed: Boolean,
    connectionName: String?,
    isRuntimeValid: Boolean,
    gostVersion: String?,
    onItemSelected: (String) -> Unit,
    onToggleCollapse: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sidebarWidth by animateDpAsState(if (collapsed) 64.dp else 240.dp)
    val cs = MaterialTheme.colorScheme
    val sc = GostSemantics.colors
    val lightShell = cs.background.luminance() > 0.5f
    val logoSize = if (collapsed) 44.dp else 56.dp
    val logoRowHorizontalPadding = if (collapsed) 10.dp else 16.dp

    Column(
        modifier =
            modifier
                .width(sidebarWidth)
                .fillMaxHeight()
                .background(sc.surfacePanel)
                .border(width = 1.dp, color = sc.borderSubtle, shape = RoundedCornerShape(0.dp))
                .padding(vertical = Spacing.md),
    ) {
        // ── Logo / Brand ──
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = logoRowHorizontalPadding, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = gostLogoPainter(),
                contentDescription = "GOST Desktop",
                modifier =
                    Modifier
                        .size(logoSize)
                        .clip(RoundedCornerShape(GostRadius.md)),
                contentScale = ContentScale.Fit,
            )

            if (!collapsed) {
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "GOST Desktop",
                        color = sc.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (gostVersion != null) {
                        Text(
                            text = "v$gostVersion",
                            color = sc.textSecondary,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }

        // ── Connection Toggle ──
        if (!collapsed && connectionName != null) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(GostRadius.sm))
                        .border(
                            width = 1.dp,
                            color = sc.borderSubtle,
                            shape = RoundedCornerShape(GostRadius.sm),
                        ).background(sc.surfaceInput)
                        .padding(horizontal = Spacing.md, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Status Dot
                        Box(
                            modifier =
                                Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isRuntimeValid) sc.statusSuccess else sc.statusError),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "RUNTIME",
                            color = sc.focusRing,
                            style = GostTextStyles.superTitle,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isRuntimeValid) "GOST Active" else "Disconnected",
                        color = sc.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Visual switch pill
                Box(
                    modifier =
                        Modifier
                            .height(24.dp)
                            .width(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRuntimeValid) {
                                    sc.statusSuccess.copy(alpha = 0.2f)
                                } else {
                                    sc.borderStrong.copy(alpha = 0.25f)
                                },
                            ).padding(horizontal = 2.dp),
                    contentAlignment = if (isRuntimeValid) Alignment.CenterEnd else Alignment.CenterStart,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isRuntimeValid) sc.statusSuccess else sc.textMuted),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        HorizontalDivider(
            color = sc.borderSubtle,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))

        // ── Nav items ──
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
        ) {
            items.forEach { item ->
                SidebarNavItem(
                    item = item,
                    isSelected = item.id == selectedId,
                    isCollapsed = collapsed,
                    lightShell = lightShell,
                    onClick = { onItemSelected(item.id) },
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        if (isRuntimeValid) {
            SidebarNavItem(
                item = SidebarItem("disconnect", "Stop Engine", androidx.compose.material.icons.Icons.Default.PowerSettingsNew),
                isSelected = false,
                isCollapsed = collapsed,
                lightShell = lightShell,
                onClick = onDisconnect,
                tint = if (lightShell) Rose500 else AmberStatus,
            )
        }

        Spacer(Modifier.height(Spacing.lg))
    }
}

@OptIn(ExperimentalFoundationApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
private fun SidebarNavItem(
    item: SidebarItem,
    isSelected: Boolean,
    isCollapsed: Boolean,
    lightShell: Boolean,
    onClick: () -> Unit,
    tint: Color? = null,
    modifier: Modifier = Modifier,
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var isHovered by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    val sc = GostSemantics.colors

    val bgColor by animateColorAsState(
        when {
            isSelected -> sc.stateSelected
            isHovered -> sc.stateHover
            else -> Color.Transparent
        },
    )

    val targetTextColor =
        tint ?: when {
            isSelected -> sc.focusRing
            isHovered -> sc.textPrimary
            else -> sc.textSecondary
        }

    val textColor by animateColorAsState(targetTextColor)

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(GostRadius.sm))
                .clickable {
                    onClick()
                    focusManager.clearFocus()
                }.onPointerEvent(PointerEventType.Enter) { isHovered = true }
                .onPointerEvent(PointerEventType.Exit) { isHovered = false }
                .drawBehind {
                    if (isSelected && lightShell) {
                        // Modern violet left border stripe for light mode
                        drawRect(
                            color = sc.focusRing,
                            topLeft = androidx.compose.ui.geometry.Offset.Zero,
                            size =
                                androidx.compose.ui.geometry
                                    .Size(3.dp.toPx(), size.height),
                        )
                    }
                },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = textColor, // Icon matches text exactly per hover/active state
                modifier = Modifier.size(20.dp),
            )

            if (!isCollapsed) {
                Spacer(Modifier.width(12.dp))

                val textShadow =
                    if (isSelected && !lightShell) {
                        androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.6f),
                            offset =
                                androidx.compose.ui.geometry
                                    .Offset(0f, 2f),
                            blurRadius = 6f,
                        )
                    } else {
                        null
                    }

                Text(
                    text = item.label,
                    color = textColor,
                    style = GostTextStyles.navItem.copy(shadow = textShadow),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                if (item.badge != null) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(GostRadius.sm))
                                .background(
                                    if (lightShell) {
                                        cs.primary.copy(alpha = 0.12f)
                                    } else {
                                        Cyan400.copy(alpha = 0.2f)
                                    },
                                ).padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                    ) {
                        Text(
                            text = item.badge,
                            color = if (lightShell) cs.primary else Cyan300,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        } // End of Row
    } // End of Box
}
