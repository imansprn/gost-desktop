package xyz.gobliggg.gost.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.gobliggg.gost.data.ServiceStatus
import xyz.gobliggg.gost.ui.theme.*

/**
 * A stat card for the Dashboard showing a label, value, and optional color accent.
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.primary,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    val sc = GostSemantics.colors
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(GostRadius.lg))
                .background(sc.surfaceCard)
                .border(GostControlSize.borderWidth, sc.borderSubtle, RoundedCornerShape(GostRadius.lg))
                .padding(Spacing.statCardInner),
    ) {
        Text(
            text = label,
            color = sc.textSecondary,
            style = GostTextStyles.statLabel,
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = value,
            color = color,
            style = GostTextStyles.statValue,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
fun SaaSIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: androidx.compose.ui.unit.Dp = GostControlSize.iconButtonCompact,
    content: @Composable () -> Unit,
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    IconButton(
        onClick = {
            onClick()
            focusManager.clearFocus()
        },
        modifier = modifier.size(size),
        enabled = enabled,
    ) {
        content()
    }
}

@Composable
fun SaaSCompactSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    val sc = GostSemantics.colors
    Box(
        modifier =
            modifier
                .size(
                    width = GostControlSize.compactSwitchWidth,
                    height = GostControlSize.compactSwitchHeight,
                )
                .clip(CircleShape)
                .background(
                    if (checked) {
                        sc.statusSuccess.copy(alpha = 0.2f)
                    } else {
                        sc.borderStrong.copy(alpha = 0.25f)
                    },
                ).padding(horizontal = 2.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier =
                Modifier
                    .size(GostControlSize.compactSwitchThumb)
                    .clip(CircleShape)
                    .background(if (checked) sc.statusSuccess else sc.textMuted),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconTooltipButton(
    tooltip: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(tooltip) } },
        state = tooltipState,
    ) {
        SaaSIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            content = content,
        )
    }
}

/**
 * Typed status pill for tunnel/runtime entities.
 * Prefer this over stringly-typed status rendering in screens.
 */
@Composable
fun ServiceStatusPill(
    status: ServiceStatus,
    modifier: Modifier = Modifier,
) {
    val (label, icon) =
        when (status) {
            ServiceStatus.IDLE -> "Stopped" to Icons.Default.PauseCircle
            ServiceStatus.STARTING -> "Starting" to Icons.Default.PlayCircle
            ServiceStatus.RUNNING -> "Running" to Icons.Default.CheckCircle
            ServiceStatus.STOPPING -> "Stopping" to Icons.Default.StopCircle
            ServiceStatus.ERROR -> "Error" to Icons.Default.Error
        }

    val sc = GostSemantics.colors
    val (bg, fg) =
        when (status) {
            ServiceStatus.IDLE -> GlassWhite to MaterialTheme.colorScheme.onSurfaceVariant
            ServiceStatus.STARTING -> sc.statusInfoContainer to sc.statusInfo
            ServiceStatus.RUNNING -> sc.statusSuccessContainer to sc.statusSuccess
            ServiceStatus.STOPPING -> sc.statusWarningContainer to sc.statusWarning
            ServiceStatus.ERROR -> sc.statusErrorContainer to sc.statusError
        }

    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(GostRadius.md))
                .background(bg)
                .border(GostControlSize.borderWidth, fg.copy(alpha = 0.3f), RoundedCornerShape(GostRadius.md))
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = fg,
            modifier = Modifier.size(GostControlSize.iconSmall),
        )
        Text(
            text = label,
            color = fg,
            style = GostTextStyles.pillLabel,
        )
    }
}

/**
 * Empty state illustration with CTA.
 */
@Composable
fun EmptyState(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Inbox,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val sc = GostSemantics.colors
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(Spacing.emptyStatePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Empty state",
            modifier = Modifier.size(GostControlSize.emptyStateIcon),
            tint = MaterialTheme.colorScheme.outline,
        )
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = title,
            color = sc.textPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.xl))
            SaaSButton(
                text = actionLabel,
                onClick = onAction,
                type = SaaSButtonType.PRIMARY,
            )
        }
    }
}

/**
 * Delete confirmation dialog.
 * Button order: Cancel on the right (confirm position), destructive on the left (dismiss position).
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Delete",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sc = GostSemantics.colors
    SaaSDialog(
        title = title,
        onDismissRequest = onDismiss,
        size = SaaSDialogSize.Sm,
    ) {
        Text(message, color = sc.textSecondary, style = GostTextStyles.navItem)
        Spacer(Modifier.height(Spacing.dialogPadding))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            SaaSButton(
                text = "Cancel",
                onClick = onDismiss,
                type = SaaSButtonType.SECONDARY,
                modifier = Modifier.widthIn(max = GostControlSize.dialogActionMaxWidth),
            )
            Spacer(Modifier.width(Spacing.lg))
            SaaSButton(
                text = confirmLabel,
                onClick = onConfirm,
                type = SaaSButtonType.ACTION,
                modifier = Modifier.widthIn(max = GostControlSize.dialogActionMaxWidth),
            )
        }
    }
}

/**
 * Toast data model.
 */
data class ToastData(
    val message: String,
    val type: ToastType = ToastType.INFO,
    val durationMs: Long = 4000,
)

enum class ToastType { INFO, SUCCESS, ERROR, WARNING }

/**
 * Toast notification component.
 */
@Composable
fun ToastMessage(
    toast: ToastData,
    modifier: Modifier = Modifier,
) {
    val bgColor =
        when (toast.type) {
            ToastType.INFO -> BlueDeep.copy(alpha = 0.9f)
            ToastType.SUCCESS -> GreenStatus.copy(alpha = 0.9f)
            ToastType.ERROR -> RedStatus.copy(alpha = 0.9f)
            ToastType.WARNING -> AmberStatus.copy(alpha = 0.9f)
        }

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(GostRadius.sm))
                .background(bgColor)
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
    ) {
        Text(
            text = toast.message,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun ErrorMessage(message: String) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GostRadius.sm))
                .background(MaterialTheme.colorScheme.errorContainer)
                .border(GostControlSize.borderWidth, MaterialTheme.colorScheme.error.copy(alpha = 0.2f), RoundedCornerShape(GostRadius.sm))
                .padding(Spacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(GostControlSize.icon),
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(
                message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/**
 * Standardized info row for key-value pairs (used in Settings and forms).
 */
@Composable
fun SaaSInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val sc = GostSemantics.colors
    androidx.compose.foundation.text.selection.SelectionContainer {
        Row(
            modifier.fillMaxWidth().padding(vertical = Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                color = sc.textSecondary,
                style = GostTextStyles.navItem,
            )
            Text(
                text = value,
                color = sc.textPrimary,
                style = GostTextStyles.navItem.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}
