package xyz.gobliggg.gost.screen.authers
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.serialization.json.Json
import xyz.gobliggg.gost.api.dto.AutherDto
import xyz.gobliggg.gost.data.ConfigBuilder
import xyz.gobliggg.gost.ui.components.*
import xyz.gobliggg.gost.ui.theme.*
import xyz.gobliggg.gost.ui.theme.Spacing

class AuthersScreen(
    private val onCreateAuther: () -> Unit,
    private val onEditAuther: (String) -> Unit,
) : Screen {
    @Composable
    override fun Content() {
        var templates by remember { mutableStateOf(ConfigBuilder.default().listTemplates("authers")) }
        var deleteTarget by remember { mutableStateOf<String?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        val sc = GostSemantics.colors
        val cs = MaterialTheme.colorScheme
        val isLightShell = cs.background.luminance() > 0.5f

        val json =
            remember {
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }
            }

        fun reload() {
            templates = ConfigBuilder.default().listTemplates("authers")
        }

        val filteredTemplates =
            remember(templates, searchQuery) {
                templates.filter { it.contains(searchQuery, ignoreCase = true) }
            }

        Box(modifier = Modifier.fillMaxSize()) {
            ScreenScaffold(
                header = {
                    SaaSScreenHeader(
                        superTitle = "ACCESS",
                        title = "Auth Rules",
                    )
                },
            ) {
                SaaSSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search auth rules…",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Spacing.lg))

                SaaSListContainer(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (filteredTemplates.isEmpty()) {
                        EmptyState(
                            title = if (searchQuery.isEmpty()) "No authers yet" else "No results found",
                            description =
                                if (searchQuery.isEmpty()) {
                                    "Authers define sets of credentials for tunnel authentication."
                                } else {
                                    "No auther matches your search query."
                                },
                            icon = Icons.Default.Security,
                            actionLabel = if (searchQuery.isEmpty()) "Create First Auther" else null,
                            onAction = onCreateAuther,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                            contentPadding = PaddingValues(bottom = Spacing.xl),
                        ) {
                            item {
                                AutherTableHeader()
                            }
                            items(filteredTemplates) { name ->
                                val content = ConfigBuilder.default().readTemplate("authers", name)
                                val dto =
                                    remember(content) {
                                        try {
                                            if (content != null) json.decodeFromString<AutherDto>(content) else null
                                        } catch (_: Exception) {
                                            AutherDto(name = name) // Fallback
                                        }
                                    }

                                if (dto != null) {
                                    AutherRow(
                                        dto = dto,
                                        onEdit = { onEditAuther(name) },
                                        onDelete = { deleteTarget = name },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    onCreateAuther()
                },
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(Spacing.xl),
                containerColor = if (isLightShell) cs.primaryContainer else SaASAction,
                contentColor = if (isLightShell) cs.onPrimaryContainer else sc.focusRing,
                shape = RoundedCornerShape(GostRadius.md),
            ) {
                Icon(Icons.Default.Add, contentDescription = "New auther template")
            }
        }

        if (deleteTarget != null) {
            ConfirmDialog(
                title = "Delete Auther",
                message = "Are you sure you want to delete \"$deleteTarget\"?",
                onConfirm = {
                    ConfigBuilder.default().deleteTemplate("authers", deleteTarget!!)
                    deleteTarget = null
                    reload()
                },
                onDismiss = { deleteTarget = null },
            )
        }
    }

    @Composable
    private fun AutherTableHeader() {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.tableHeaderRowV),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SaaSTableHeader("NAME", modifier = Modifier.weight(1f))
            SaaSTableHeader("TYPE", modifier = Modifier.width(100.dp))
            SaaSTableHeader("USERS / TARGET", modifier = Modifier.width(150.dp))
            SaaSTableHeader("OPTIONS", modifier = Modifier.width(64.dp), textAlign = TextAlign.End)
        }
    }

    @Composable
    private fun AutherRow(
        dto: AutherDto,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
    ) {
        val isPlugin = dto.plugin != null
        val sc = GostSemantics.colors

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(GostRadius.md))
                    .background(sc.surfaceCard)
                    .border(1.dp, sc.borderSubtle, RoundedCornerShape(GostRadius.md))
                    .clickable { onEdit() }
                    .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                dto.name ?: "Unknown",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = sc.textPrimary,
            )

            Box(
                modifier = Modifier.width(100.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(GostRadius.xs))
                            .background(if (isPlugin) sc.statusInfoContainer else sc.stateSelected)
                            .padding(horizontal = Spacing.sm, vertical = 4.dp),
                ) {
                    Text(
                        if (isPlugin) "PLUGIN" else "INLINE",
                        color = if (isPlugin) sc.statusInfo else sc.statusSuccess,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Info column
            Text(
                if (isPlugin) dto.plugin?.addr ?: "-" else "${dto.auths?.size ?: 0} users",
                modifier = Modifier.width(150.dp),
                fontSize = 13.sp,
                color = sc.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Box(Modifier.width(64.dp), contentAlignment = Alignment.CenterEnd) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = sc.textSecondary)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Delete", tint = sc.statusError, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
