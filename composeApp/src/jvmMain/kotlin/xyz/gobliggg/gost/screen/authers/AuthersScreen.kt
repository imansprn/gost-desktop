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
import xyz.gobliggg.gost.data.AppState
import xyz.gobliggg.gost.data.ConfigBuilder
import xyz.gobliggg.gost.data.TemplateTypes
import xyz.gobliggg.gost.ui.ShellFeedback
import xyz.gobliggg.gost.ui.components.*
import xyz.gobliggg.gost.ui.theme.*
import xyz.gobliggg.gost.ui.theme.Spacing

private object AutherTableDimensions {
    val typeWidth = 100.dp
    val targetWidth = 150.dp
    val optionsWidth = 64.dp
}

class AuthersScreen(
    private val onCreateAuther: () -> Unit,
    private val onEditAuther: (String) -> Unit,
) : Screen {
    @Composable
    override fun Content() {
        var templates by remember { mutableStateOf(ConfigBuilder.default().listTemplates(TemplateTypes.AUTHERS)) }
        var deleteTarget by remember { mutableStateOf<String?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        val settings by AppState.settings.collectAsState()
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
            templates = ConfigBuilder.default().listTemplates(TemplateTypes.AUTHERS)
        }

        fun removeAuther(name: String) {
            val builder = ConfigBuilder.default()
            val dependents = builder.findDependentServices(TemplateTypes.AUTHERS, name)
            if (dependents.isNotEmpty()) {
                ShellFeedback.showSnackbar(
                    "Cannot delete '$name'; used by: " + dependents.joinToString(),
                )
                return
            }
            try {
                builder.deleteTemplate(TemplateTypes.AUTHERS, name)
                reload()
                ShellFeedback.showSnackbar("Auth rule deleted")
            } catch (e: Exception) {
                ShellFeedback.showSnackbar(e.message ?: "Failed to delete auth rule")
            }
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
                                val content = ConfigBuilder.default().readTemplate(TemplateTypes.AUTHERS, name)
                                val dto =
                                    remember(content) {
                                        runCatching {
                                            if (content != null) json.decodeFromString<AutherDto>(content) else null
                                        }.getOrNull()
                                    }

                                if (dto != null) {
                                    AutherRow(
                                        dto = dto,
                                        onEdit = { onEditAuther(name) },
                                        onDelete = {
                                            if (settings.confirmDeletes) {
                                                deleteTarget = name
                                            } else {
                                                removeAuther(name)
                                            }
                                        },
                                    )
                                } else {
                                    CorruptAutherRow(
                                        name = name,
                                        onDelete = {
                                            if (settings.confirmDeletes) {
                                                deleteTarget = name
                                            } else {
                                                removeAuther(name)
                                            }
                                        },
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
                    val target = deleteTarget!!
                    deleteTarget = null
                    removeAuther(target)
                },
                onDismiss = { deleteTarget = null },
            )
        }
    }

    @Composable
    private fun CorruptAutherRow(
        name: String,
        onDelete: () -> Unit,
    ) {
        val sc = GostSemantics.colors
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(GostRadius.md))
                    .background(sc.statusErrorContainer.copy(alpha = 0.25f))
                    .border(
                        GostControlSize.borderWidth,
                        sc.statusError.copy(alpha = 0.25f),
                        RoundedCornerShape(GostRadius.md),
                    )
                    .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = sc.statusError)
            Spacer(Modifier.width(Spacing.sm))
            Column(Modifier.weight(1f)) {
                Text(name, color = sc.textPrimary, fontWeight = FontWeight.SemiBold)
                Text("Invalid JSON template", color = sc.statusError, style = GostTextStyles.pillLabel)
            }
            IconTooltipButton(
                tooltip = "Delete invalid auth rule",
                onClick = onDelete,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete invalid auth rule",
                    tint = sc.statusError,
                    modifier = Modifier.size(GostControlSize.icon),
                )
            }
        }
    }

    @Composable
    private fun AutherTableHeader() {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.tableHeaderRowV),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SaaSTableHeader("NAME", modifier = Modifier.weight(1f))
            SaaSTableHeader("TYPE", modifier = Modifier.width(AutherTableDimensions.typeWidth))
            SaaSTableHeader("USERS / TARGET", modifier = Modifier.width(AutherTableDimensions.targetWidth))
            SaaSTableHeader("OPTIONS", modifier = Modifier.width(AutherTableDimensions.optionsWidth), textAlign = TextAlign.End)
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
                    .border(GostControlSize.borderWidth, sc.borderSubtle, RoundedCornerShape(GostRadius.md))
                    .clickable { onEdit() }
                    .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                dto.name ?: "Unknown",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                style = GostTextStyles.rowTitle,
                color = sc.textPrimary,
            )

            Box(
                modifier = Modifier.width(AutherTableDimensions.typeWidth),
            ) {
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(GostRadius.xs))
                            .background(if (isPlugin) sc.statusInfoContainer else sc.stateSelected)
                            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                ) {
                    Text(
                        if (isPlugin) "PLUGIN" else "INLINE",
                        color = if (isPlugin) sc.statusInfo else sc.statusSuccess,
                        style = GostTextStyles.microLabel.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            // Info column
            Text(
                if (isPlugin) dto.plugin?.addr ?: "-" else "${dto.auths?.size ?: 0} users",
                modifier = Modifier.width(AutherTableDimensions.targetWidth),
                style = GostTextStyles.navItem,
                color = sc.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Box(Modifier.width(AutherTableDimensions.optionsWidth), contentAlignment = Alignment.CenterEnd) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    IconTooltipButton(tooltip = "Edit auth rule", onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit auth rule",
                            modifier = Modifier.size(GostControlSize.icon),
                            tint = sc.textSecondary,
                        )
                    }
                    IconTooltipButton(tooltip = "Delete auth rule", onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete auth rule",
                            tint = sc.statusError,
                            modifier = Modifier.size(GostControlSize.icon),
                        )
                    }
                }
            }
        }
    }
}
