package xyz.gobliggg.gost.screen.settings
import xyz.gobliggg.gost.ui.components.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import xyz.gobliggg.gost.data.AppState
import xyz.gobliggg.gost.model.ThemeMode
import xyz.gobliggg.gost.ui.theme.Spacing
import xyz.gobliggg.gost.ui.theme.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalStdlibApi::class)
class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val settings by AppState.settings.collectAsState()
        val sc = GostSemantics.colors

        ScreenScaffold(
            header = {
                SaaSScreenHeader(
                    superTitle = "PREFERENCES",
                    title = "Settings"
                )
            },
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {

            // ── Appearance ──
            SectionHeader("Appearance")
            SectionCard {
                Text(
                    "Theme",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        val isActive = settings.theme == mode
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isActive) SaaSSelection
                                    else Color.Transparent,
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isActive) sc.focusRing else sc.borderSubtle,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { AppState.updateSettings { it.copy(theme = mode) } }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = if (isActive) sc.focusRing else sc.textSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = settings.sidebarCollapsed,
                        onCheckedChange = { AppState.updateSettings { s -> s.copy(sidebarCollapsed = it) } },
                        colors = CheckboxDefaults.colors(checkedColor = sc.focusRing, checkmarkColor = Color.Black)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Collapse sidebar by default",
                        color = sc.textSecondary,
                        fontSize = 12.sp,
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.lg),
                color = sc.borderSubtle,
            )

            // ── Polling ──
            SectionHeader("Polling")
            SectionCard {
                Text(
                    "Default poll interval",
                    color = sc.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 30, 60).forEach { sec ->
                        val isActive = settings.defaultPollIntervalSeconds == sec
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isActive) SaaSSelection
                                    else SaaSInputBg,
                                )
                                .clickable { AppState.updateSettings { it.copy(defaultPollIntervalSeconds = sec) } }
                                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                        ) {
                            Text(
                                "${sec}s",
                                color = if (isActive) sc.focusRing else sc.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Log buffer size",
                    color = sc.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(500, 1000, 5000, 10000).forEach { size ->
                        val isActive = settings.logBufferSize == size
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isActive) SaaSSelection
                                    else SaaSInputBg,
                                )
                                .clickable { AppState.updateSettings { it.copy(logBufferSize = size) } }
                                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                        ) {
                            Text(
                                "$size",
                                color = if (isActive) sc.focusRing else sc.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.lg),
                color = sc.borderSubtle,
            )

            // ── Safety ──
            SectionHeader("Safety")
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = settings.confirmDeletes,
                        onCheckedChange = { AppState.updateSettings { s -> s.copy(confirmDeletes = it) } },
                        colors = CheckboxDefaults.colors(checkedColor = sc.focusRing, checkmarkColor = Color.Black)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Require confirmation for delete actions",
                        color = sc.textSecondary,
                        fontSize = 12.sp,
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.lg),
                color = sc.borderSubtle,
            )

            // ── Runtime ──
            SectionHeader("GOST Runtime")
            SectionCard {
                InfoRow("Binary Path", settings.gostRuntime.binaryPath.ifBlank { "(not set)" })
                InfoRow("Working Directory", settings.gostRuntime.workingDirectory.ifBlank { "(default)" })
                InfoRow("Auto-start", if (settings.gostRuntime.autoStart) "Yes" else "No")
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.lg),
                color = sc.borderSubtle,
            )

            // ── About ──
            SectionHeader("About")
            SectionCard {
                InfoRow("App Version", "1.0.0")
                InfoRow("Target GOST", "≥ 3.1.0")
                InfoRow("Runtime", System.getProperty("java.version", "?"))
                InfoRow("Platform", "${System.getProperty("os.name")} ${System.getProperty("os.arch")}")
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    SaaSTableHeader(title.uppercase(), modifier = Modifier.padding(bottom = Spacing.sm))
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    SaaSListContainer(
        Modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        content = content,
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    val sc = GostSemantics.colors
    androidx.compose.foundation.text.selection.SelectionContainer {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                label,
                color = sc.textSecondary,
                fontSize = 13.sp,
            )
            Text(
                value,
                color = sc.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
