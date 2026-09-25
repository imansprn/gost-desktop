package xyz.gobliggg.gost.screen.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import xyz.gobliggg.gost.ui.components.*
import xyz.gobliggg.gost.ui.theme.*
import xyz.gobliggg.gost.ui.theme.Spacing
import javax.swing.JFileChooser

private object ConnectionLayoutDimensions {
    val topPadding = 64.dp
    val footerGap = 80.dp
}

class ConnectionScreen(
    private val onConnected: () -> Unit,
    private val onCancel: (() -> Unit)? = null,
) : Screen {
    @Composable
    override fun Content() {
        val model = rememberScreenModel { ConnectionScreenModel() }
        val state by model.state.collectAsState()
        val persistenceIssue by xyz.gobliggg.gost.data.AppState.persistenceIssue.collectAsState()
        val canConnect =
            state.binaryPath.isNotBlank() &&
                state.pathError == null &&
                state.workingDirectoryError == null
        val sc = GostSemantics.colors

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(sc.surfacePanel),
        ) {


            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = Spacing.xxl,
                            end = Spacing.xxl,
                            bottom = Spacing.xxl,
                            top = ConnectionLayoutDimensions.topPadding
                        ),
            ) {
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                ) {
                    GostBrandLockup()
                    Spacer(Modifier.height(Spacing.xl))
                    SaaSScreenHeader(
                        superTitle = "SETUP",
                        title = "Runtime Setup",
                        subtitle = "Configure your local GOST runtime",
                        bottomSpacing = Spacing.xxl,
                    )

                    // Binary Path
                    SaaSTextField(
                        value = state.binaryPath,
                        onValueChange = model::updateBinaryPath,
                        label = "GOST Binary Path *",
                        placeholder = state.suggestedBinaryPath ?: "/usr/local/bin/gost",
                        isError = state.pathError != null,
                        helperText = state.pathError,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            SaaSIconButton(
                                onClick = {
                                    val chooser = JFileChooser()
                                    chooser.dialogTitle = "Select GOST binary"
                                    chooser.fileSelectionMode = JFileChooser.FILES_ONLY
                                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                        model.updateBinaryPath(chooser.selectedFile.absolutePath)
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Default.FolderOpen,
                                    contentDescription = "Browse for GOST binary",
                                    tint = sc.textMuted,
                                    modifier = Modifier.size(GostControlSize.iconLarge),
                                )
                            }
                        },
                    )

                    Spacer(Modifier.height(Spacing.xl))

                    // Working Directory
                    SaaSTextField(
                        value = state.workingDirectory,
                        onValueChange = model::updateWorkingDirectory,
                        label = "Working Directory",
                        placeholder = "~/.gost-desktop",
                        isError = state.workingDirectoryError != null,
                        helperText = state.workingDirectoryError ?: "Optional. Leave blank to use the application default.",
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            SaaSIconButton(
                                onClick = {
                                    val chooser = JFileChooser()
                                    chooser.dialogTitle = "Select working directory"
                                    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                        model.updateWorkingDirectory(chooser.selectedFile.absolutePath)
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Default.FolderOpen,
                                    contentDescription = "Browse for working directory",
                                    tint = sc.textMuted,
                                    modifier = Modifier.size(GostControlSize.iconLarge),
                                )
                            }
                        },
                    )

                    Spacer(Modifier.height(Spacing.xxl))

                    persistenceIssue?.let { message ->
                        Banner(message, type = BannerType.Warning)
                        Spacer(Modifier.height(Spacing.xl))
                    }

                    // Auto Start
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = state.autoStart,
                            onCheckedChange = model::updateAutoStart,
                            colors = CheckboxDefaults.colors(
                                checkedColor = sc.focusRing,
                                uncheckedColor = sc.borderStrong,
                                checkmarkColor = Color.Black
                            ),
                        )
                        Spacer(Modifier.width(Spacing.xs))
                        Text(
                            "Auto-start last running tunnels",
                            color = sc.textSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                // Big gap before the button
                Spacer(Modifier.height(ConnectionLayoutDimensions.footerGap))

                if (onCancel == null) {
                    SaaSButton(
                        text = "Save & Continue",
                        onClick = { model.saveAndConnect(onConnected) },
                        enabled = canConnect,
                        type = SaaSButtonType.PRIMARY,
                        modifier = Modifier.fillMaxWidth(),
                        size = SaaSButtonSize.Large,
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        SaaSButton(
                            text = "Cancel",
                            onClick = onCancel,
                            type = SaaSButtonType.SECONDARY,
                            modifier = Modifier.weight(1f),
                            size = SaaSButtonSize.Large,
                        )
                        SaaSButton(
                            text = "Save Runtime",
                            onClick = { model.saveAndConnect(onConnected) },
                            enabled = canConnect,
                            type = SaaSButtonType.PRIMARY,
                            modifier = Modifier.weight(1f),
                            size = SaaSButtonSize.Large,
                        )
                    }
                }
            }
        }
    }
}
