package xyz.gobliggg.gost.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.gobliggg.gost.api.dto.AuthDto
import xyz.gobliggg.gost.api.dto.AutherDto
import xyz.gobliggg.gost.api.dto.PluginDto
import xyz.gobliggg.gost.ui.theme.*

@Composable
fun AutherFormDialog(
    initialAuther: AutherDto? = null,
    onSave: (AutherDto) -> Unit,
    onDismiss: () -> Unit,
) {
    AutherFormEditor(
        initialAuther = initialAuther,
        onSave = onSave,
        onCancel = onDismiss,
        title = if (initialAuther == null) "New Auther Template" else "Edit Auther",
        variant = AutherFormVariant.DialogSplit,
    )
}

enum class AutherFormVariant {
    DialogSplit,
    FullScreen,
}

@Composable
fun AutherFormEditor(
    initialAuther: AutherDto? = null,
    title: String,
    onSave: (AutherDto) -> Unit,
    onCancel: () -> Unit,
    variant: AutherFormVariant,
    onDirtyChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    sidePanelWidth: Dp = GostLayoutSize.editorSidebar,
) {
    var name by remember { mutableStateOf(initialAuther?.name ?: "") }
    var isPluginMode by remember { mutableStateOf(initialAuther?.plugin != null) }

    var auths by remember {
        mutableStateOf(initialAuther?.auths?.toMutableList() ?: mutableListOf(AuthDto("", "")))
    }

    var pluginType by remember { mutableStateOf(initialAuther?.plugin?.type ?: "grpc") }
    var pluginAddr by remember { mutableStateOf(initialAuther?.plugin?.addr ?: "") }
    var pluginToken by remember { mutableStateOf(initialAuther?.plugin?.token ?: "") }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val passwordVisibility = remember { mutableStateMapOf<Int, Boolean>() }
    val sc = GostSemantics.colors

    val currentValue =
        AutherDto(
            name = name,
            auths = if (!isPluginMode) auths else null,
            plugin = if (isPluginMode) PluginDto(type = pluginType, addr = pluginAddr, token = pluginToken) else null,
        )
    val initialValue =
        initialAuther
            ?: AutherDto(
                name = "",
                auths = listOf(AuthDto("", "")),
                plugin = null,
            )
    val nameError =
        when {
            name.isBlank() -> "Name is required"
            !name.matches(Regex("^[a-zA-Z0-9_-]+$")) ->
                "Use only letters, numbers, underscore, and hyphen"
            else -> null
        }
    val inlineValid =
        auths.isNotEmpty() &&
            auths.all { !it.username.isNullOrBlank() && !it.password.isNullOrBlank() }
    val pluginValid = pluginAddr.isNotBlank()
    val formValid =
        nameError == null &&
            if (isPluginMode) pluginValid else inlineValid

    LaunchedEffect(currentValue, initialValue) {
        onDirtyChange(currentValue != initialValue)
    }

    val leftPanel: @Composable ColumnScope.() -> Unit = {
        SaaSTableHeader("TEMPLATE TYPE")
        Spacer(Modifier.height(Spacing.sm))

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            TypeSelectTab(
                label = "Inline List",
                description = "Username & password pairs stored in config",
                isSelected = !isPluginMode,
                onClick = {
                    isPluginMode = false
                    focusManager.clearFocus()
                },
            )
            TypeSelectTab(
                label = "External Plugin",
                description = "Authenticate via gRPC or HTTP endpoint",
                isSelected = isPluginMode,
                onClick = {
                    isPluginMode = true
                    focusManager.clearFocus()
                },
            )
        }

        Spacer(Modifier.weight(1f))

        SaaSTableHeader("SUMMARY")
        Spacer(Modifier.height(Spacing.xs))
        Text(
            if (isPluginMode) {
                "Connecting to $pluginType at $pluginAddr"
            } else {
                "Managing ${auths.size} user credentials"
            },
            color = sc.textSecondary.copy(alpha = 0.85f),
            style = GostTextStyles.bodyCompact,
        )
    }

    val onSaveInternal: () -> Unit = save@{
        if (!formValid) return@save
        focusManager.clearFocus()
        onSave(currentValue)
    }

    val mainContent: @Composable ColumnScope.() -> Unit = {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SaaSTableHeader("BASIC INFO")
            Spacer(Modifier.height(Spacing.sm))

            SaaSTextField(
                label = "Template Name *",
                value = name,
                onValueChange = { name = it },
                placeholder = "admin-auth",
                isError = name.isNotBlank() && nameError != null,
                helperText = nameError ?: "Unique name to reference this auth rule",
            )

            Spacer(Modifier.height(Spacing.xl))

            if (!isPluginMode) {
                SaaSTableHeader("USER CREDENTIALS")
                Spacer(Modifier.height(Spacing.sm))

                auths.forEachIndexed { index, auth ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SaaSTextField(
                            value = auth.username ?: "",
                            onValueChange = {
                                val next = auths.toMutableList()
                                next[index] = next[index].copy(username = it)
                                auths = next
                            },
                            modifier = Modifier.weight(0.45f),
                            placeholder = "Username",
                        )
                        SaaSTextField(
                            value = auth.password ?: "",
                            onValueChange = {
                                val next = auths.toMutableList()
                                next[index] = next[index].copy(password = it)
                                auths = next
                            },
                            modifier = Modifier.weight(0.45f),
                            placeholder = "Password",
                            visualTransformation =
                                if (passwordVisibility[index] ==
                                    true
                                ) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                            trailingIcon = {
                                SaaSIconButton(
                                    onClick = {
                                        passwordVisibility[index] = !(passwordVisibility[index] ?: false)
                                        focusManager.clearFocus()
                                    },
                                ) {
                                    Icon(
                                        imageVector =
                                            if (passwordVisibility[index] ==
                                                true
                                            ) {
                                                Icons.Default.VisibilityOff
                                            } else {
                                                Icons.Default.Visibility
                                            },
                                        contentDescription = "Toggle password visibility",
                                        modifier = Modifier.size(GostControlSize.icon),
                                        tint = sc.textMuted,
                                    )
                                }
                            },
                        )
                        IconTooltipButton(
                            tooltip = "Delete user",
                            onClick = {
                                if (auths.size > 1) {
                                    val next = auths.toMutableList()
                                    next.removeAt(index)
                                    auths = next
                                }
                                focusManager.clearFocus()
                            },
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete user",
                                tint = RedStatus,
                                modifier = Modifier.size(GostControlSize.icon),
                            )
                        }
                    }
                }

                if (!inlineValid) {
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        "Each user requires both a username and password.",
                        color = sc.statusError,
                        style = GostTextStyles.microLabel,
                    )
                }

                Spacer(Modifier.height(Spacing.sm))
                SaaSButton(
                    text = "Add User",
                    onClick = {
                        val next = auths.toMutableList()
                        next.add(AuthDto("", ""))
                        auths = next
                        focusManager.clearFocus()
                    },
                    type = SaaSButtonType.SECONDARY,
                    icon = Icons.Default.Add,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                SaaSTableHeader("PLUGIN SETTINGS")
                Spacer(Modifier.height(Spacing.sm))

                SegmentedControl(
                    options = listOf("grpc", "http"),
                    selected = pluginType,
                    onSelect = {
                        pluginType = it
                        focusManager.clearFocus()
                    },
                    label = { it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    equalWidth = true,
                )

                Spacer(Modifier.height(Spacing.lg))

                SaaSTextField(
                    label = "Endpoint Address *",
                    value = pluginAddr,
                    onValueChange = { pluginAddr = it },
                    placeholder = "127.0.0.1:9000",
                    isError = pluginAddr.isBlank(),
                    helperText =
                        if (pluginAddr.isBlank()) {
                            "Plugin endpoint is required"
                        } else {
                            "Network address of the plugin"
                        },
                )
                Spacer(Modifier.height(Spacing.sm))
                SaaSTextField(
                    label = "Security Token",
                    value = pluginToken,
                    onValueChange = { pluginToken = it },
                    placeholder = "Optional token...",
                    helperText = "Bearer token for gRPC/HTTP requests",
                )
            }

            Spacer(Modifier.height(Spacing.xxl))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                SaaSButton(
                    text = "Cancel",
                    onClick = {
                        focusManager.clearFocus()
                        onCancel()
                    },
                    type = SaaSButtonType.SECONDARY,
                    modifier = Modifier.widthIn(max = GostControlSize.dialogActionMaxWidth),
                )
                Spacer(Modifier.width(Spacing.sm))
                SaaSButton(
                    text = "Save Template",
                    onClick = onSaveInternal,
                    enabled = formValid,
                    type = SaaSButtonType.ACTION,
                    icon = Icons.Default.Save,
                    modifier = Modifier.widthIn(max = GostControlSize.dialogActionMaxWidth),
                )
            }
        }
    }

    when (variant) {
        AutherFormVariant.DialogSplit -> {
            SaaSDialog(
                title = title,
                onDismissRequest = onCancel,
                size = SaaSDialogSize.Xl,
                showSplit = true,
                leftContent = leftPanel,
                content = mainContent,
            )
        }

        AutherFormVariant.FullScreen -> {
            Row(modifier = modifier.fillMaxSize()) {
                Column(
                    modifier =
                        Modifier
                            .width(sidePanelWidth)
                            .fillMaxHeight()
                            .background(GostSemantics.colors.surfacePanel)
                            .padding(Spacing.lg),
                ) {
                    leftPanel()
                }
                VerticalDivider(thickness = GostControlSize.borderWidth, color = GostSemantics.colors.borderSubtle)
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(Spacing.xl),
                ) {
                    SaaSScreenHeader(
                        superTitle = "ACCESS",
                        title = title,
                    )
                    Spacer(Modifier.height(Spacing.lg))
                    mainContent()
                }
            }
        }
    }
}

@Composable
private fun TypeSelectTab(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    SaaSModeOption(
        label = label,
        description = description,
        selected = isSelected,
        onClick = onClick,
    )
}
