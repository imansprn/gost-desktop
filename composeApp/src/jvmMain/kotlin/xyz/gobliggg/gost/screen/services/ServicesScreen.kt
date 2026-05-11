package xyz.gobliggg.gost.screen.services
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import xyz.gobliggg.gost.data.ServiceStatus
import xyz.gobliggg.gost.ui.components.*
import xyz.gobliggg.gost.ui.theme.*
import androidx.compose.foundation.shape.RoundedCornerShape

class ServicesScreen(
    private val onCreateService: () -> Unit = {},
    private val onEditService: (String) -> Unit = {},
) : Screen {
    @Composable
    override fun Content() {
        val model = rememberScreenModel { ServicesScreenModel() }
        val SaASAction = Color(0xFF0F2B2B)
        val state by model.state.collectAsState()
        var deleteTarget by remember { mutableStateOf<String?>(null) }
        var rowMenuService by remember { mutableStateOf<String?>(null) }
        val sc = GostSemantics.colors

        Box(modifier = Modifier.fillMaxSize()) {
        ScreenScaffold(
            header = {
                SaaSScreenHeader(
                    superTitle = "GOST RUNTIME",
                    title = "Tunnels",
                    actions = {
                        SaaSSearchBar(
                            query = state.searchQuery,
                            onQueryChange = model::search,
                            placeholder = "Search tunnels…",
                            modifier = Modifier.width(280.dp),
                        )
                        Spacer(Modifier.width(Spacing.md))
                        SaaSButton(
                            text = "Create Tunnel",
                            onClick = onCreateService,
                            type = SaaSButtonType.PRIMARY,
                        )
                    },
                )
            },
            messages = {
                state.errorMessage?.let { Banner(it, type = BannerType.Error) }
            },
        ) {
            if (state.filteredServices.isEmpty()) {
                if (state.searchQuery.isNotBlank()) {
                    EmptyState(
                        title = "No matches",
                        description = "No tunnels match \"${state.searchQuery}\"",
                        icon = Icons.Default.Search,
                    )
                } else {
                    EmptyState(
                        title = "No tunnels yet",
                        description = "Create a tunnel to generate its config and start the process.",
                        icon = Icons.Default.Router,
                        actionLabel = "Create Tunnel",
                        onAction = onCreateService,
                    )
                }
            } else {
                SaaSListContainer(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                ) {
                    // Header
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.tableHeaderRowV),
                    ) {
                        SaaSTableHeader("NAME", Modifier.weight(1f))
                        SaaSTableHeader("LISTEN ADDRESS", Modifier.width(200.dp))
                        SaaSTableHeader("PID", Modifier.width(80.dp))
                        SaaSTableHeader("STATUS", Modifier.width(110.dp))
                        SaaSTableHeader("QUICK ACTIONS", Modifier.width(180.dp))
                        SaaSTableHeader("OPTIONS", Modifier.width(64.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    }
                    HorizontalDivider(color = sc.borderSubtle)

                    state.filteredServices.forEach { svc ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    svc.name,
                                    color = sc.textPrimary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (svc.errorMessage != null) {
                                    Text(
                                        svc.errorMessage,
                                        color = sc.statusError,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }

                            Text(
                                svc.addr,
                                color = sc.textMuted,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(200.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            Text(
                                svc.pid?.toString() ?: "—",
                                color = sc.textMuted,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(80.dp),
                            )

                            Box(Modifier.width(110.dp)) {
                                ServiceStatusPill(status = svc.status)
                            }

                            // Quick Actions
                            Row(Modifier.width(180.dp), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                if (svc.status == ServiceStatus.RUNNING) {
                                    SaaSButton(
                                        text = "Stop",
                                        onClick = { model.stopService(svc.id) },
                                        type = SaaSButtonType.SECONDARY,
                                        modifier = Modifier.width(80.dp),
                                    )
                                } else {
                                    SaaSButton(
                                        text = "Start",
                                        onClick = { model.startService(svc.id) },
                                        type = SaaSButtonType.ACTION,
                                        modifier = Modifier.width(80.dp),
                                    )
                                }

                                SaaSButton(
                                    text = "Restart",
                                    onClick = { model.restartService(svc.id) },
                                    type = SaaSButtonType.SECONDARY,
                                    modifier = Modifier.width(80.dp),
                                )
                            }

                            Box(Modifier.width(64.dp), contentAlignment = Alignment.CenterEnd) {
                                IconTooltipButton(
                                    tooltip = "More actions",
                                    onClick = { rowMenuService = svc.id },
                                ) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "Actions for ${svc.name}",
                                        tint = sc.textMuted,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                DropdownMenu(
                                    expanded = rowMenuService == svc.id,
                                    onDismissRequest = { rowMenuService = null },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit") },
                                        onClick = {
                                            rowMenuService = null
                                            onEditService(svc.name)
                                        },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Edit tunnel") },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete…", color = sc.statusError) },
                                        onClick = {
                                            rowMenuService = null
                                            deleteTarget = svc.id
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete tunnel",
                                                tint = sc.statusError,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = sc.dividerSubtle)
                    }
                }
            }
            if (deleteTarget != null) {
                val tunnelName = state.services.find { it.id == deleteTarget }?.name ?: "Tunnel"
                ConfirmDialog(
                    title = "Delete Tunnel",
                    message = "Delete \"$tunnelName\"? Process will be stopped and config removed.",
                    onConfirm = {
                        model.deleteService(deleteTarget!!)
                        deleteTarget = null
                    },
                    onDismiss = { deleteTarget = null },
                )
            }
        }
            FloatingActionButton(
                onClick = onCreateService,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Spacing.xl),
                containerColor = SaASAction,
                contentColor = Color.White,
                shape = RoundedCornerShape(GostRadius.md)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New tunnel")
            }
        }
    }
}
