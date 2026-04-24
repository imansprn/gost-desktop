package xyz.gobliggg.gost.screen.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.model.rememberScreenModel
import xyz.gobliggg.gost.ui.components.*
import xyz.gobliggg.gost.ui.theme.Spacing
import xyz.gobliggg.gost.ui.theme.*
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class ConnectionScreen(
    private val onConnected: () -> Unit,
) : Screen {

    @Composable
    override fun Content() {
        val model = rememberScreenModel { ConnectionScreenModel() }
        val state by model.state.collectAsState()
        val canConnect = state.binaryPath.isNotBlank() && state.pathError == null
        val sc = GostSemantics.colors

        SaaSAppBackground(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .padding(Spacing.xl)
                        .clip(RoundedCornerShape(GostRadius.lg))
                        .background(sc.surfacePanel)
                        .border(1.dp, sc.borderSubtle, RoundedCornerShape(GostRadius.lg))
                        .verticalScroll(rememberScrollState())
                        .padding(Spacing.xl),
                ) {
                    SaaSScreenHeader(
                        superTitle = "SETUP",
                        title = "GOST Desktop Setup",
                        subtitle = "Configure your local GOST runtime",
                        leading = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SaASAction),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("G", color = sc.focusRing, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                    )

                    Spacer(Modifier.height(Spacing.xxl))

                    // Binary Path
                    SaaSTextField(
                        value = state.binaryPath,
                        onValueChange = model::updateBinaryPath,
                        label = "GOST Binary Path *",
                        placeholder = "/usr/local/bin/gost",
                        isError = state.pathError != null,
                        helperText = state.pathError,
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
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(Spacing.lg))

                    // Working Directory
                    SaaSTextField(
                        value = state.workingDirectory,
                        onValueChange = model::updateWorkingDirectory,
                        label = "Working Directory",
                        placeholder = "~/.gost-desktop",
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
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(Spacing.lg))

                    // Auto Start
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = state.autoStart,
                            onCheckedChange = model::updateAutoStart,
                            colors = CheckboxDefaults.colors(checkedColor = sc.focusRing, checkmarkColor = Color.Black)
                        )
                        Spacer(Modifier.width(Spacing.xs))
                        Text(
                            "Auto-start last running tunnels",
                            color = sc.textSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Spacer(Modifier.height(Spacing.xxl))

                    SaaSButton(
                        text = "Save & Continue",
                        onClick = { model.saveAndConnect(onConnected) },
                        enabled = canConnect,
                        type = SaaSButtonType.PRIMARY,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
