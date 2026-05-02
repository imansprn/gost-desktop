package xyz.gobliggg.gost.screen.config
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import xyz.gobliggg.gost.ui.GlobalWindowShortcuts
import xyz.gobliggg.gost.ui.components.*
import xyz.gobliggg.gost.ui.components.EmptyState
import xyz.gobliggg.gost.ui.theme.*
import javax.swing.JFileChooser

class ConfigEditorScreen : Screen {
    @Composable
    override fun Content() {
        val model = rememberScreenModel { ConfigEditorScreenModel() }
        val state by model.state.collectAsState()
        var reloadConfirmOpen by remember { mutableStateOf(false) }
        val sc = GostSemantics.colors

        DisposableEffect(model) {
            val save = { model.save() }
            val refresh = { model.reloadFromDisk() }
            GlobalWindowShortcuts.saveHandler = save
            GlobalWindowShortcuts.refreshHandler = refresh
            onDispose {
                if (GlobalWindowShortcuts.saveHandler === save) GlobalWindowShortcuts.saveHandler = null
                if (GlobalWindowShortcuts.refreshHandler === refresh) GlobalWindowShortcuts.refreshHandler = null
            }
        }

        ScreenScaffold(
            header = {
                SaaSScreenHeader(
                    superTitle = "MODIFICATIONS",
                    title = "Config Editor",
                    subtitle = "View and edit raw tunnel configuration files",
                    actions = {
                        if (state.selectedServiceId != null) {
                            SaaSButton(
                                text = "Reload",
                                onClick = {
                                    if (state.isDirty) {
                                        reloadConfirmOpen = true
                                    } else {
                                        model.reloadFromDisk()
                                    }
                                },
                                enabled = !state.isLoading,
                                type = SaaSButtonType.SECONDARY,
                                icon = Icons.Default.Refresh,
                            )
                            SaaSButton(
                                text = "Export…",
                                onClick = {
                                    val chooser = JFileChooser()
                                    chooser.dialogTitle = "Export configuration"
                                    chooser.selectedFile = java.io.File("${state.selectedServiceId}-config.json")
                                    if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                                        model.exportToLocalFile(chooser.selectedFile.absolutePath)
                                    }
                                },
                                enabled = !state.isLoading,
                                type = SaaSButtonType.SECONDARY,
                                icon = Icons.Default.FileDownload,
                            )
                            SaaSButton(
                                text = "Save (Ctrl+S)",
                                onClick = { model.save() },
                                enabled = !state.isSaving && state.isDirty,
                                loading = state.isSaving,
                                type = SaaSButtonType.ACTION,
                                icon = Icons.Default.Save,
                            )
                        }
                    },
                )
            },
            messages = {
                state.errorMessage?.let { Banner(it, type = BannerType.Error) }
                state.successMessage?.let { Banner(it, type = BannerType.Success) }
            },
        ) {
            if (reloadConfirmOpen) {
                SaaSDialog(
                    title = "Reload from disk?",
                    onDismissRequest = { reloadConfirmOpen = false },
                    size = SaaSDialogSize.Sm,
                ) {
                    Text(
                        "You have unsaved edits. Reloading will discard them and load the file from disk.",
                        color = sc.textSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(Spacing.dialogPadding))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        SaaSButton(
                            text = "Cancel",
                            onClick = { reloadConfirmOpen = false },
                            type = SaaSButtonType.SECONDARY,
                            modifier = Modifier.widthIn(max = 160.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        SaaSButton(
                            text = "Discard & Reload",
                            onClick = {
                                reloadConfirmOpen = false
                                model.reloadFromDisk()
                            },
                            type = SaaSButtonType.ACTION,
                            modifier = Modifier.widthIn(max = 160.dp),
                        )
                    }
                }
            }

            // ── Split: service list + editor ──
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Left: service selector
                Column(
                    modifier = Modifier.width(200.dp).fillMaxHeight(),
                ) {
                    SaaSTableHeader("TUNNELS")
                    Spacer(Modifier.height(Spacing.sm))
                    if (state.serviceIds.isEmpty()) {
                        Text(
                            "No tunnels configured yet.",
                            color = sc.textSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(Spacing.sm),
                        )
                    } else {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            state.serviceIds.forEach { id ->
                                val isSel = id == state.selectedServiceId
                                Text(
                                    text = id,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(GostRadius.sm))
                                            .background(
                                                if (isSel) {
                                                    sc.stateSelected
                                                } else {
                                                    Color.Transparent
                                                },
                                            ).border(
                                                width = 1.dp,
                                                color = if (isSel) sc.focusRing else sc.borderSubtle,
                                                shape = RoundedCornerShape(GostRadius.sm),
                                            ).clickable { model.selectService(id) }
                                            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                                    color = if (isSel) sc.textPrimary else sc.textSecondary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.width(Spacing.lg))
                VerticalDivider(color = sc.borderSubtle)
                Spacer(Modifier.width(Spacing.lg))

                // Right: editor
                if (state.selectedServiceId != null) {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(GostRadius.lg))
                                .background(sc.surfaceInput)
                                .border(
                                    1.dp,
                                    if (state.isDirty) sc.focusRing.copy(alpha = 0.4f) else sc.borderSubtle,
                                    RoundedCornerShape(GostRadius.lg),
                                ),
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                Modifier.align(Alignment.Center),
                                color = sc.focusRing,
                            )
                        } else {
                            OutlinedTextField(
                                value = state.content,
                                onValueChange = model::updateContent,
                                modifier = Modifier.fillMaxSize(),
                                colors =
                                    TextFieldDefaults.colors(
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        cursorColor = sc.focusRing,
                                        focusedTextColor = sc.textPrimary,
                                        unfocusedTextColor = sc.textSecondary,
                                    ),
                                textStyle =
                                    androidx.compose.ui.text.TextStyle(
                                        fontFamily = MonoFontFamily,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                    ),
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            title = "No tunnel selected",
                            description = "Select a tunnel from the list to view and edit its configuration file.",
                            icon = Icons.Default.Code,
                        )
                    }
                }
            }
        }
    }
}
