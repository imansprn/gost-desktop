package xyz.gobliggg.gost.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import xyz.gobliggg.gost.ui.theme.*

/**
 * Global background wrapper with the SaaS ambient glow & grid pattern.
 */
@Composable
fun SaaSAppBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val sc = GostSemantics.colors

    val meshBrush =
        remember(sc.surfaceApp) {
            Brush.radialGradient(
                colors = listOf(AmbientMeshGlow.copy(alpha = 0.18f), AmbientMeshBase, sc.surfaceApp),
                center = Offset(x = 1000f, y = 500f),
                radius = 2000f,
            )
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(sc.surfaceApp)
                .background(meshBrush)
                .gridPattern(
                    color = sc.textOnAccent.copy(alpha = 0.018f),
                    gridSize = 40f,
                ),
    ) {
        content()
    }
}

/**
 * Top-level screen header with a premium double-line title pattern.
 */
@Composable
fun SaaSScreenHeader(
    title: String,
    superTitle: String? = null,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val sc = GostSemantics.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.xxl),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(Spacing.lg))
            }
            Column(modifier = Modifier.weight(1f)) {
                if (superTitle != null) {
                    Text(
                        text = superTitle.uppercase(),
                        color = sc.focusRing,
                        style = GostTextStyles.superTitle,
                    )
                    Spacer(Modifier.height(Spacing.xs))
                }
                Text(
                    text = title,
                    color = sc.textPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = subtitle,
                        color = sc.textMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}

/**
 * Standard container for lists and detail views.
 */
@Composable
fun SaaSListContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sc = GostSemantics.colors
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(GostRadius.lg))
                .background(sc.surfacePanel)
                .border(1.dp, sc.borderSubtle, RoundedCornerShape(GostRadius.lg)),
    ) {
        content()
    }
}

/**
 * Standard table header text style.
 */
@Composable
fun SaaSTableHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    val sc = GostSemantics.colors
    Text(
        text = text,
        color = sc.textMuted,
        style = GostTextStyles.tableHeader,
        modifier = modifier,
    )
}

/**
 * Unified text field with the dark SaaS aesthetic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaaSTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    isError: Boolean = false,
    helperText: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
) {
    val sc = GostSemantics.colors
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                color = sc.textMuted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(Spacing.xs))
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(40.dp),
            enabled = enabled,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = sc.textPrimary),
            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    enabled = enabled,
                    singleLine = singleLine,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    placeholder = {
                        Text(
                            text = placeholder,
                            color = DarkTextDim,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    trailingIcon = trailingIcon,
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = saasTextFieldColors(),
                            shape = RoundedCornerShape(GostRadius.md),
                            focusedBorderThickness = 1.dp,
                            unfocusedBorderThickness = 1.dp,
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = saasTextFieldColors(),
                )
            },
        )

        if (helperText != null && !isError) {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = helperText,
                color = DarkTextDim,
                style = GostTextStyles.tableHeader,
            )
        }
    }
}

/**
 * Reusable colors for standard Material3 TextFields in the SaaS theme.
 */
@Composable
fun saasTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = GostSemantics.colors.surfaceInput,
        focusedContainerColor = GostSemantics.colors.surfaceInput,
        unfocusedBorderColor = GostSemantics.colors.borderSubtle,
        focusedBorderColor = GostSemantics.colors.focusRing,
        unfocusedTextColor = GostSemantics.colors.textPrimary,
        focusedTextColor = GostSemantics.colors.textPrimary,
        cursorColor = GostSemantics.colors.focusRing,
        errorBorderColor = GostSemantics.colors.statusError,
    )

@Composable
fun SaaSSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
) {
    SaaSTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = placeholder,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = DarkTextSlate,
                modifier = Modifier.size(18.dp),
            )
        },
    )
}

enum class SaaSButtonType {
    PRIMARY, // Gradient Cyan
    SECONDARY, // Slate Grey
    ACTION, // Dark Teal + Green Text
}

@Composable
fun SaaSButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: SaaSButtonType = SaaSButtonType.SECONDARY,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val sc = GostSemantics.colors
    val cs = MaterialTheme.colorScheme
    val isLightShell = cs.background.luminance() > 0.5f
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val containerColor =
        when (type) {
            SaaSButtonType.PRIMARY -> Color.Transparent
            SaaSButtonType.SECONDARY -> if (isLightShell) cs.surfaceContainer else SaASSlate
            SaaSButtonType.ACTION -> if (isLightShell) cs.primaryContainer else SaASAction
        }

    val contentColor =
        when (type) {
            SaaSButtonType.PRIMARY -> sc.textOnAccent
            SaaSButtonType.SECONDARY -> if (isLightShell) cs.onSurfaceVariant else sc.textPrimary
            SaaSButtonType.ACTION -> if (isLightShell) cs.onPrimaryContainer else sc.focusRing
        }

    val shape = RoundedCornerShape(GostRadius.md)
    val overlayAlpha =
        when {
            isPressed -> 0.10f
            isHovered -> 0.06f
            else -> 0f
        }

    val gradientModifier =
        modifier
            .background(
                brush =
                    Brush.linearGradient(
                        colors = listOf(BrandGradientStart, BrandGradientEnd),
                        start = Offset(0f, 0f),
                        end = Offset(500f, 0f),
                    ),
                shape = shape,
            ).then(
                if (overlayAlpha > 0f) {
                    Modifier.background(sc.textOnAccent.copy(alpha = overlayAlpha), shape)
                } else {
                    Modifier
                },
            )

    val content: @Composable () -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = contentColor,
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = GostTextStyles.buttonLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                )
            }
        }
    }

    if (type == SaaSButtonType.PRIMARY) {
        // Custom button avoids desktop indication that can draw a box around text on hover.
        Box(
            modifier =
                gradientModifier
                    .height(40.dp)
                    .clip(shape)
                    .clickable(
                        enabled = enabled && !loading,
                        interactionSource = interactionSource,
                        indication = null,
                    ) {
                        onClick()
                        focusManager.clearFocus()
                    }.padding(horizontal = Spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    } else {
        Button(
            onClick = {
                onClick()
                focusManager.clearFocus()
            },
            modifier = modifier.height(40.dp),
            enabled = enabled && !loading,
            shape = shape,
            interactionSource = interactionSource,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                    disabledContainerColor = containerColor.copy(alpha = 0.3f),
                    disabledContentColor = contentColor.copy(alpha = 0.5f),
                ),
            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = 0.dp),
        ) {
            content()
        }
    }
}

/**
 * Base SaaS Dialog with support for split-view layout.
 */
@Composable
fun SaaSDialog(
    title: String,
    onDismissRequest: () -> Unit,
    size: SaaSDialogSize = SaaSDialogSize.Md,
    showSplit: Boolean = false,
    leftContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sc = GostSemantics.colors
    val maxWidth =
        when (size) {
            SaaSDialogSize.Sm -> 520.dp
            SaaSDialogSize.Md -> 640.dp
            SaaSDialogSize.Lg -> 820.dp
            SaaSDialogSize.Xl -> 980.dp
        }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = maxWidth)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(GostRadius.lg))
                    .background(sc.surfaceApp)
                    .border(1.dp, sc.borderSubtle, RoundedCornerShape(GostRadius.lg)),
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                // Optional Side Panel (Left)
                if (showSplit && leftContent != null) {
                    Column(
                        modifier =
                            Modifier
                                .width(300.dp)
                                .fillMaxHeight()
                                .background(sc.surfacePanel)
                                .padding(Spacing.dialogPadding),
                    ) {
                        leftContent()
                    }
                    VerticalDivider(color = sc.borderSubtle)
                }

                // Main Area
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(Spacing.dialogPadding),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = sc.textPrimary,
                    )
                    Spacer(Modifier.height(Spacing.dialogPadding))
                    content()
                }
            }

            // Close Button (Top Right)
            IconButton(
                onClick = onDismissRequest,
                modifier = Modifier.align(Alignment.TopEnd).padding(Spacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = sc.textSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

enum class SaaSDialogSize { Sm, Md, Lg, Xl }

/**
 * Reusable toggle group for multiple choice selection (e.g., settings pills).
 */
@Composable
fun <T> SaaSToggleGroup(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    labelModifier: (T) -> String = { it.toString() },
    modifier: Modifier = Modifier,
) {
    val sc = GostSemantics.colors
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        options.forEach { option ->
            val isActive = selectedOption == option
            Box(
                Modifier
                    .clip(RoundedCornerShape(GostRadius.sm))
                    .background(if (isActive) sc.stateSelected else sc.surfaceInput)
                    .clickable { onOptionSelected(option) }
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            ) {
                Text(
                    text = labelModifier(option),
                    color = if (isActive) sc.focusRing else sc.textSecondary,
                    style = GostTextStyles.buttonLabel.copy(fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal),
                )
            }
        }
    }
}
