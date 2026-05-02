package xyz.gobliggg.gost.screen.settings
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import xyz.gobliggg.gost.data.AppState
import xyz.gobliggg.gost.ui.components.*
import xyz.gobliggg.gost.ui.theme.*
import xyz.gobliggg.gost.ui.theme.Spacing

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val settings by AppState.settings.collectAsState()
        val sc = GostSemantics.colors

        ScreenScaffold(
            header = {
                SaaSScreenHeader(
                    superTitle = "PREFERENCES",
                    title = "Settings",
                )
            },
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            // ── Appearance ──
            SectionHeader("Appearance")
            SectionCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.clickable {
                            AppState.updateSettings { s -> s.copy(sidebarCollapsed = !s.sidebarCollapsed) }
                        },
                ) {
                    Checkbox(
                        checked = settings.sidebarCollapsed,
                        onCheckedChange = { AppState.updateSettings { s -> s.copy(sidebarCollapsed = it) } },
                        colors = CheckboxDefaults.colors(checkedColor = sc.focusRing, checkmarkColor = Color.Black),
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
                Spacer(Modifier.height(Spacing.sm))
                SaaSToggleGroup(
                    options = listOf(5, 10, 30, 60),
                    selectedOption = settings.defaultPollIntervalSeconds,
                    onOptionSelected = { sec -> AppState.updateSettings { it.copy(defaultPollIntervalSeconds = sec) } },
                    labelModifier = { "${it}s" },
                )
                Spacer(Modifier.height(Spacing.md))
                Text(
                    "Log buffer size",
                    color = sc.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.height(Spacing.sm))
                SaaSToggleGroup(
                    options = listOf(500, 1000, 5000, 10000),
                    selectedOption = settings.logBufferSize,
                    onOptionSelected = { size -> AppState.updateSettings { it.copy(logBufferSize = size) } },
                )
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
                        colors = CheckboxDefaults.colors(checkedColor = sc.focusRing, checkmarkColor = Color.Black),
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
                SaaSInfoRow("Binary Path", settings.gostRuntime.binaryPath.ifBlank { "(not set)" })
                SaaSInfoRow("Working Directory", settings.gostRuntime.workingDirectory.ifBlank { "(default)" })
                SaaSInfoRow("Auto-start", if (settings.gostRuntime.autoStart) "Yes" else "No")
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.lg),
                color = sc.borderSubtle,
            )

            // ── About ──
            SectionHeader("About")
            SectionCard {
                SaaSInfoRow("App Version", "1.0.0")
                SaaSInfoRow("Target GOST", "≥ 3.1.0")
                SaaSInfoRow("Runtime", System.getProperty("java.version", "?"))
                SaaSInfoRow("Platform", "${System.getProperty("os.name")} ${System.getProperty("os.arch")}")
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
        Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
            content = content,
        )
    }
}
