package xyz.gobliggg.gost.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.semantics.semantics
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
        modifier = modifier
            .clip(RoundedCornerShape(GostRadius.lg))
            .background(sc.surfaceCard)
            .border(1.dp, sc.borderSubtle, RoundedCornerShape(GostRadius.lg))
            .padding(Spacing.statCardInner),
    ) {
        Text(
            text = label,
            color = sc.textSecondary,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 0.5.sp,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconTooltipButton(
    tooltip: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(tooltip) } },
        state = tooltipState,
    ) {
        IconButton(
            onClick = {
                onClick()
                focusManager.clearFocus()
            },
            modifier = modifier,
            enabled = enabled
        ) {
            content()
        }
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
    val (label, icon) = when (status) {
        ServiceStatus.RUNNING -> "Running" to Icons.Default.CheckCircle
        ServiceStatus.IDLE -> "Stopped" to Icons.Default.PauseCircle
        ServiceStatus.ERROR -> "Error" to Icons.Default.Error
    }

    val sc = GostSemantics.colors
    val (bg, fg) = when (status) {
        ServiceStatus.RUNNING -> sc.statusSuccessContainer to sc.statusSuccess
        ServiceStatus.IDLE -> GlassWhite to MaterialTheme.colorScheme.onSurfaceVariant
        ServiceStatus.ERROR -> sc.statusErrorContainer to sc.statusError
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(GostRadius.md))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.3f), RoundedCornerShape(GostRadius.md))
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = fg,
            modifier = Modifier.size(14.dp),
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
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.emptyStatePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Empty state",
            modifier = Modifier.size(48.dp),
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
                type = SaaSButtonType.PRIMARY
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
        Text(message, color = sc.textSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(Spacing.dialogPadding))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            SaaSButton(
                text = "Cancel",
                onClick = onDismiss,
                type = SaaSButtonType.SECONDARY,
                modifier = Modifier.widthIn(max = 160.dp),
            )
            Spacer(Modifier.width(12.dp))
            SaaSButton(
                text = confirmLabel,
                onClick = onConfirm,
                type = SaaSButtonType.ACTION,
                modifier = Modifier.widthIn(max = 160.dp),
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
    val bgColor = when (toast.type) {
        ToastType.INFO -> BlueDeep.copy(alpha = 0.9f)
        ToastType.SUCCESS -> GreenStatus.copy(alpha = 0.9f)
        ToastType.ERROR -> RedStatus.copy(alpha = 0.9f)
        ToastType.WARNING -> AmberStatus.copy(alpha = 0.9f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GostRadius.sm))
            .background(MaterialTheme.colorScheme.errorContainer)
            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f), RoundedCornerShape(GostRadius.sm))
            .padding(Spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
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
fun SaaSInfoRow(label: String, value: String, modifier: Modifier = Modifier) {
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
