package xyz.gobliggg.gost.screen.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(sc.surfacePanel)
                    .border(1.dp, sc.borderSubtle, RoundedCornerShape(16.dp))
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
                Text(
                    "GOST Binary Path *",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    OutlinedTextField(
                        value = state.binaryPath,
                        onValueChange = model::updateBinaryPath,
                        placeholder = {
                            Text(
                                "/usr/local/bin/gost",
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        singleLine = true,
                        isError = state.pathError != null,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(52.dp),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = saasTextFieldColors(),
                        supportingText = if (state.pathError != null) {
                            { Text(state.pathError!!, color = RedStatus) }
                        } else null,
                    )
                    FilledTonalIconButton(
                        onClick = {
                            val chooser = JFileChooser()
                            chooser.dialogTitle = "Select GOST binary"
                            chooser.fileSelectionMode = JFileChooser.FILES_ONLY
                            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                model.updateBinaryPath(chooser.selectedFile.absolutePath)
                            }
                        },
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = "Browse for GOST binary",
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Working Directory
                Text(
                    "Working Directory",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    OutlinedTextField(
                        value = state.workingDirectory,
                        onValueChange = model::updateWorkingDirectory,
                        placeholder = {
                            Text(
                                "~/.gost-desktop",
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(52.dp),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = saasTextFieldColors()
                    )
                    FilledTonalIconButton(
                        onClick = {
                            val chooser = JFileChooser()
                            chooser.dialogTitle = "Select working directory"
                            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                model.updateWorkingDirectory(chooser.selectedFile.absolutePath)
                            }
                        },
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = "Browse for working directory",
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Auto Start
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.autoStart,
                        onCheckedChange = model::updateAutoStart,
                        colors = CheckboxDefaults.colors(checkedColor = sc.focusRing, checkmarkColor = Color.Black)
                    )
                    Spacer(Modifier.width(4.dp))
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
