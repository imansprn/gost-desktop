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
import xyz.gobliggg.gost.data.EngineTransition
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

private object SidebarDimensions {
    val logoCollapsed = 44.dp
    val logoExpanded = 56.dp
    val logoHorizontalCollapsed = 10.dp
    val logoVerticalPadding = 20.dp
    val navVerticalPadding = 10.dp
    val statusDot = 6.dp
    val selectedStripe = 3.dp
}

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
    isEngineRunning: Boolean,
    engineTransition: EngineTransition? = null,
    gostVersion: String?,
    onItemSelected: (String) -> Unit,
    onToggleCollapse: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sidebarWidth by
        animateDpAsState(
            if (collapsed) GostLayoutSize.sidebarCollapsed else GostLayoutSize.sidebarExpanded,
        )
    val cs = MaterialTheme.colorScheme
    val sc = GostSemantics.colors
    val lightShell = cs.background.luminance() > 0.5f
    val logoSize = if (collapsed) SidebarDimensions.logoCollapsed else SidebarDimensions.logoExpanded
    val logoRowHorizontalPadding =
        if (collapsed) SidebarDimensions.logoHorizontalCollapsed else Spacing.xl

    Column(
        modifier =
            modifier
                .width(sidebarWidth)
                .fillMaxHeight()
                .background(sc.surfacePanel)
                .border(
                    width = GostControlSize.borderWidth,
                    color = sc.borderSubtle,
                    shape = RoundedCornerShape(0.dp),
                )
                .padding(vertical = Spacing.sm),
    ) {
        // ── Logo / Brand ──
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = logoRowHorizontalPadding,
                        vertical = SidebarDimensions.logoVerticalPadding,
                    ),
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
                Spacer(Modifier.width(Spacing.lg))
                Column {
                    Text(
                        text = "GOST Desktop",
                        color = sc.textPrimary,
                        style = GostTextStyles.sectionTitle.copy(fontWeight = FontWeight.Bold),
                    )
                    if (gostVersion != null) {
                        Text(
                            text = "v$gostVersion",
                            color = sc.textSecondary,
                            style = MaterialTheme.typography.labelSmall,
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
                        .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                        .clip(RoundedCornerShape(GostRadius.sm))
                        .border(
                            width = GostControlSize.borderWidth,
                            color = sc.borderSubtle,
                            shape = RoundedCornerShape(GostRadius.sm),
                        )
                        .background(sc.surfaceInput)
                        .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Status Dot
                        Box(
                            modifier =
                                Modifier
                                    .size(SidebarDimensions.statusDot)
                                    .clip(CircleShape)
                                    .background(
                                when {
                                    !isRuntimeValid -> sc.statusError
                                    engineTransition == EngineTransition.STARTING -> sc.statusInfo
                                    engineTransition == EngineTransition.STOPPING -> sc.statusWarning
                                    isEngineRunning -> sc.statusSuccess
                                    else -> sc.textMuted
                                },
                            ),
                        )
                        Spacer(Modifier.width(Spacing.xs))
                        Text(
                            text = "RUNTIME",
                            color = sc.focusRing,
                            style = GostTextStyles.superTitle,
                        )
                    }
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text =
                            when {
                                !isRuntimeValid -> "Runtime unavailable"
                                engineTransition == EngineTransition.STARTING -> "Engine starting…"
                                engineTransition == EngineTransition.STOPPING -> "Engine stopping…"
                                isEngineRunning -> "Engine active"
                                else -> "Engine stopped"
                            },
                        color = sc.textPrimary,
                        style = GostTextStyles.navItem.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Compact engine state indicator.
                SaaSCompactSwitch(checked = isEngineRunning)
            }
            Spacer(Modifier.height(Spacing.sm))
        }

        HorizontalDivider(
            color = sc.borderSubtle,
            modifier = Modifier.padding(horizontal = Spacing.xl),
        )
        Spacer(Modifier.height(Spacing.sm))

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

        Spacer(Modifier.height(Spacing.xs))

        if (isRuntimeValid) {
            SidebarNavItem(
                item =
                    SidebarItem(
                        "disconnect",
                        when (engineTransition) {
                            EngineTransition.STARTING -> "Starting Engine…"
                            EngineTransition.STOPPING -> "Stopping Engine…"
                            null -> if (isEngineRunning) "Stop Engine" else "Start Engine"
                        },
                        androidx.compose.material.icons.Icons.Default.PowerSettingsNew,
                    ),
                isSelected = false,
                isCollapsed = collapsed,
                lightShell = lightShell,
                onClick = {
                    if (engineTransition == null) onDisconnect()
                },
                tint =
                    if (isEngineRunning) {
                        if (lightShell) Rose500 else AmberStatus
                    } else {
                        sc.statusSuccess
                    },
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
                .padding(horizontal = Spacing.lg, vertical = Spacing.xs)
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
                                    .Size(SidebarDimensions.selectedStripe.toPx(), size.height),
                        )
                    }
                },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(
                        horizontal = Spacing.lg,
                        vertical = SidebarDimensions.navVerticalPadding,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = textColor, // Icon matches text exactly per hover/active state
                modifier = Modifier.size(GostControlSize.iconLarge),
            )

            if (!isCollapsed) {
                Spacer(Modifier.width(Spacing.lg))

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
                    Spacer(Modifier.width(Spacing.sm))
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
                            style = GostTextStyles.microLabel,
                        )
                    }
                }
            }
        } // End of Row
    } // End of Box
}
