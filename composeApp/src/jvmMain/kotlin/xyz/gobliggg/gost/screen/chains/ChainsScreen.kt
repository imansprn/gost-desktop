package xyz.gobliggg.gost.screen.chains
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.gobliggg.gost.api.dto.ChainDto
import xyz.gobliggg.gost.api.dto.HopDto
import xyz.gobliggg.gost.api.dto.NodeDto
import xyz.gobliggg.gost.data.AppState
import xyz.gobliggg.gost.data.ConfigBuilder
import xyz.gobliggg.gost.data.TemplateRuntimeSynchronizer
import xyz.gobliggg.gost.data.TemplateTypes
import xyz.gobliggg.gost.ui.ShellFeedback
import xyz.gobliggg.gost.ui.UnsavedChangesGuard
import xyz.gobliggg.gost.ui.components.*
import xyz.gobliggg.gost.ui.theme.*

@OptIn(ExperimentalStdlibApi::class)
class ChainsScreen(
    private val onEditService: (String) -> Unit = {},
) : Screen {
    @Composable
    override fun Content() {
        var templates by remember { mutableStateOf(ConfigBuilder.default().listTemplates(TemplateTypes.CHAINS)) }
        var selectedTemplate by remember { mutableStateOf<String?>(null) }
        var editingChain by remember { mutableStateOf<ChainDto?>(null) }
        var isDirty by remember { mutableStateOf(false) }
        var showCreateDialog by remember { mutableStateOf(false) }
        var newChainName by remember { mutableStateOf("") }
        var deleteTarget by remember { mutableStateOf<String?>(null) }
        var pendingSelection by remember { mutableStateOf<String?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        val settings by AppState.settings.collectAsState()

        LaunchedEffect(isDirty) {
            UnsavedChangesGuard.setDirty(isDirty)
        }
        DisposableEffect(Unit) {
            onDispose { UnsavedChangesGuard.clear() }
        }

        val json =
            remember {
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }
            }

        fun reload() {
            templates = ConfigBuilder.default().listTemplates(TemplateTypes.CHAINS)
        }

        fun removeChain(name: String) {
            val builder = ConfigBuilder.default()
            val dependents = builder.findDependentServices(TemplateTypes.CHAINS, name)
            if (dependents.isNotEmpty()) {
                ShellFeedback.showSnackbar(
                    "Cannot delete '$name'; used by: " + dependents.joinToString(),
                )
                return
            }
            try {
                builder.deleteTemplate(TemplateTypes.CHAINS, name)
                if (selectedTemplate == name) selectedTemplate = null
                reload()
                ShellFeedback.showSnackbar("Chain deleted")
            } catch (e: Exception) {
                ShellFeedback.showSnackbar(e.message ?: "Failed to delete chain")
            }
        }

        LaunchedEffect(selectedTemplate) {
            if (selectedTemplate != null) {
                val content = ConfigBuilder.default().readTemplate(TemplateTypes.CHAINS, selectedTemplate!!)
                editingChain =
                    try {
                        if (content != null) json.decodeFromString<ChainDto>(content) else null
                    } catch (e: Exception) {
                        null
                    }
            } else {
                editingChain = null
            }
            isDirty = false
        }

        fun requestCreateChain() {
            if (isDirty) {
                ShellFeedback.showSnackbar("Save or discard the current chain changes first.")
                return
            }
            newChainName = ""
            showCreateDialog = true
        }

        val filteredTemplates =
            remember(templates, searchQuery) {
                templates.filter { it.contains(searchQuery, ignoreCase = true) }
            }

        val sc = GostSemantics.colors
        val SaASAction = Color(0xFF0F2B2B)
        Box(modifier = Modifier.fillMaxSize().padding(Spacing.xl)) {
            Row(modifier = Modifier.fillMaxSize()) {
                // ── Left: Chain List (35%) ──
                Column(
                    modifier =
                        Modifier
                            .width(320.dp)
                            .fillMaxHeight(),
                ) {
                    SaaSScreenHeader(
                        superTitle = "ROUTING",
                        title = "Chains",
                        actions = {
                            SaaSButton(
                                text = "New Chain",
                                onClick = { requestCreateChain() },
                                type = SaaSButtonType.PRIMARY,
                            )
                        },
                    )
                    SaaSSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search chains...",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.lg))

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (filteredTemplates.isEmpty()) {
                            EmptyState(
                                title = "No chains",
                                description = "Chains route traffic through proxy nodes.",
                                icon = Icons.Default.Link,
                                actionLabel = "Create chain",
                                onAction = { requestCreateChain() },
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(filteredTemplates) { name ->
                                    val isSel = name == selectedTemplate
                                    ChainListItem(
                                        name = name,
                                        isSelected = isSel,
                                        onClick = {
                                            if (isDirty && selectedTemplate != name) {
                                                pendingSelection = name
                                            } else {
                                                selectedTemplate = name
                                            }
                                        },
                                        onDelete = {
                                            if (settings.confirmDeletes) {
                                                deleteTarget = name
                                            } else {
                                                removeChain(name)
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.width(Spacing.xl))

                // ── Right: Chain Editor (65%) ──
                SaaSListContainer(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(Spacing.xl)) {
                        if (editingChain != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = editingChain!!.name ?: "Untitled Chain",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = sc.textPrimary,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                    if (isDirty) {
                                        SaaSButton(
                                            text = "Discard",
                                            onClick = {
                                                val old = selectedTemplate
                                                selectedTemplate = null
                                                selectedTemplate = old
                                            },
                                            type = SaaSButtonType.SECONDARY,
                                        )
                                    }
                                    SaaSButton(
                                        text = "Save Config",
                                        onClick = {
                                            val builder = ConfigBuilder.default()
                                            val name = editingChain!!.name
                                            try {
                                                if (name.isNullOrBlank() || !builder.isValidName(name)) {
                                                    throw IllegalArgumentException(
                                                        "Use only letters, numbers, underscore, and hyphen.",
                                                    )
                                                }
                                                if (name != selectedTemplate && builder.templateExists(TemplateTypes.CHAINS, name)) {
                                                    throw IllegalArgumentException("A chain named '$name' already exists.")
                                                }
                                                if (name != selectedTemplate && selectedTemplate != null) {
                                                    val dependents =
                                                        builder.findDependentServices(
                                                            TemplateTypes.CHAINS,
                                                            selectedTemplate!!,
                                                        )
                                                    if (dependents.isNotEmpty()) {
                                                        throw IllegalStateException(
                                                            "Cannot rename while used by: " + dependents.joinToString(),
                                                        )
                                                    }
                                                }

                                                builder.saveTemplate(
                                                    TemplateTypes.CHAINS,
                                                    name,
                                                    json.encodeToString(editingChain),
                                                )
                                                if (selectedTemplate != null && selectedTemplate != name) {
                                                    builder.deleteTemplate(TemplateTypes.CHAINS, selectedTemplate!!)
                                                }
                                                TemplateRuntimeSynchronizer
                                                    .synchronize(TemplateTypes.CHAINS, name)
                                                    .getOrThrow()
                                                selectedTemplate = name
                                                isDirty = false
                                                reload()
                                                ShellFeedback.showSnackbar("Chain saved")
                                            } catch (e: Exception) {
                                                ShellFeedback.showSnackbar(e.message ?: "Failed to save chain")
                                            }
                                        },
                                        enabled = isDirty,
                                        type = SaaSButtonType.ACTION,
                                    )
                                }
                            }
                            Spacer(Modifier.height(Spacing.xl))

                            ChainVisualEditor(
                                chain = editingChain!!,
                                onUpdate = {
                                    editingChain = it
                                    isDirty = true
                                },
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                EmptyState(
                                    title = "No chain selected",
                                    description = "Select a chain on the left to edit its hops and nodes.",
                                    icon = Icons.Default.LinkOff,
                                )
                            }
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = { requestCreateChain() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Spacing.xl),
                containerColor = SaASAction,
                contentColor = Color.White,
                shape = RoundedCornerShape(GostRadius.md)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New chain")
            }
        }

        if (pendingSelection != null) {
            ConfirmDialog(
                title = "Discard unsaved changes?",
                message = "Switching chains will discard the current edits.",
                onConfirm = {
                    val target = pendingSelection
                    pendingSelection = null
                    isDirty = false
                    selectedTemplate = target
                },
                onDismiss = { pendingSelection = null },
            )
        }

        if (showCreateDialog) {
            SaaSDialog(
                title = "New Chain",
                onDismissRequest = { showCreateDialog = false },
                size = SaaSDialogSize.Sm,
            ) {
                Text(
                    "Enter a unique name for the chain template.",
                    style = MaterialTheme.typography.bodySmall,
                    color = sc.textMuted,
                )
                Spacer(Modifier.height(Spacing.lg))
                val createError =
                    when {
                        newChainName.isBlank() -> "Name is required"
                        !ConfigBuilder.default().isValidName(newChainName) ->
                            "Use only letters, numbers, underscore, and hyphen"
                        ConfigBuilder.default().templateExists(TemplateTypes.CHAINS, newChainName) ->
                            "A chain with this name already exists"
                        else -> null
                    }
                SaaSTextField(
                    value = newChainName,
                    onValueChange = { newChainName = it },
                    label = "Chain Name",
                    modifier = Modifier.fillMaxWidth(),
                    isError = newChainName.isNotBlank() && createError != null,
                    helperText = if (newChainName.isBlank()) null else createError,
                )
                Spacer(Modifier.height(Spacing.xl))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SaaSButton(
                        text = "Cancel",
                        onClick = { showCreateDialog = false },
                        type = SaaSButtonType.SECONDARY,
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    SaaSButton(
                        text = "Create",
                        onClick = {
                            if (createError == null) {
                                try {
                                    val newChain =
                                        ChainDto(
                                            name = newChainName,
                                            hops = listOf(HopDto("hop-1", listOf(NodeDto()))),
                                        )
                                    ConfigBuilder.default().saveTemplate(
                                        TemplateTypes.CHAINS,
                                        newChainName,
                                        json.encodeToString(newChain),
                                    )
                                    reload()
                                    selectedTemplate = newChainName
                                    showCreateDialog = false
                                    ShellFeedback.showSnackbar("Chain created")
                                } catch (e: Exception) {
                                    ShellFeedback.showSnackbar(e.message ?: "Failed to create chain")
                                }
                            }
                        },
                        enabled = createError == null,
                        type = SaaSButtonType.PRIMARY,
                    )
                }
            }
        }

        if (deleteTarget != null) {
            ConfirmDialog(
                title = "Delete Chain",
                message = "Remove \"$deleteTarget\"? Tunnels using this chain will fail.",
                onConfirm = {
                    val target = deleteTarget!!
                    deleteTarget = null
                    removeChain(target)
                },
                onDismiss = { deleteTarget = null },
            )
        }
    }

    @Composable
    private fun ChainListItem(
        name: String,
        isSelected: Boolean,
        onClick: () -> Unit,
        onDelete: () -> Unit,
    ) {
        val sc = GostSemantics.colors
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xs)
                    .clip(RoundedCornerShape(GostRadius.sm))
                    .background(if (isSelected) sc.stateSelected else Color.Transparent)
                    .clickable { onClick() }
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = "Chain",
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) sc.statusSuccess else sc.textMuted,
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                style =
                    GostTextStyles.navItem.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    ),
                color = if (isSelected) sc.textPrimary else sc.textSecondary,
            )
            if (isSelected) {
                IconTooltipButton(
                    tooltip = "Delete chain",
                    onClick = onDelete,
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Delete",
                        modifier = Modifier.size(GostControlSize.icon),
                        tint = sc.textMuted,
                    )
                }
            }
        }
    }
}
