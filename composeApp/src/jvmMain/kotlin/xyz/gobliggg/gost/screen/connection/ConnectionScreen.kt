package xyz.gobliggg.gost.screen.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import xyz.gobliggg.gost.ui.components.*
import xyz.gobliggg.gost.ui.theme.*
import xyz.gobliggg.gost.ui.theme.Spacing
import javax.swing.JFileChooser

class ConnectionScreen(
    private val onConnected: () -> Unit,
) : Screen {
    @Composable
    override fun Content() {
        val model = rememberScreenModel { ConnectionScreenModel() }
        val state by model.state.collectAsState()
        val canConnect = state.binaryPath.isNotBlank() && state.pathError == null
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
                            top = 64.dp
                        ),
            ) {
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                ) {
                    SaaSScreenHeader(
                        superTitle = "SETUP",
                        title = "GOST Desktop Setup",
                        subtitle = "Configure your local GOST runtime",
                        bottomSpacing = Spacing.xxxl,
                        leading = {
                            Box(
                                modifier =
                                    Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(SaASAction),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("G", color = sc.focusRing, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        },
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
                            IconButton(
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
                                    modifier = Modifier.size(20.dp),
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
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(
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
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        },
                    )

                    Spacer(Modifier.height(Spacing.xxl))

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
                Spacer(Modifier.height(80.dp))

                SaaSButton(
                    text = "Save & Continue",
                    onClick = { model.saveAndConnect(onConnected) },
                    enabled = canConnect,
                    type = SaaSButtonType.PRIMARY,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                )
            }
        }
    }
}
