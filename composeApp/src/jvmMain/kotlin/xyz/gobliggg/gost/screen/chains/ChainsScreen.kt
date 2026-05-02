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
import xyz.gobliggg.gost.data.ConfigBuilder
import xyz.gobliggg.gost.ui.components.*
import xyz.gobliggg.gost.ui.theme.*

@OptIn(ExperimentalStdlibApi::class)
class ChainsScreen(
    private val onEditService: (String) -> Unit = {},
) : Screen {
    @Composable
    override fun Content() {
        var templates by remember { mutableStateOf(ConfigBuilder.listTemplates("chains")) }
        var selectedTemplate by remember { mutableStateOf<String?>(null) }
        var editingChain by remember { mutableStateOf<ChainDto?>(null) }
        var isDirty by remember { mutableStateOf(false) }
        var showCreateDialog by remember { mutableStateOf(false) }
        var newChainName by remember { mutableStateOf("") }
        var deleteTarget by remember { mutableStateOf<String?>(null) }
        var searchQuery by remember { mutableStateOf("") }

        val json =
            remember {
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }
            }

        fun reload() {
            templates = ConfigBuilder.listTemplates("chains")
        }

        LaunchedEffect(selectedTemplate) {
            if (selectedTemplate != null) {
                val content = ConfigBuilder.readTemplate("chains", selectedTemplate!!)
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

        val filteredTemplates =
            remember(templates, searchQuery) {
                templates.filter { it.contains(searchQuery, ignoreCase = true) }
            }

        val sc = GostSemantics.colors
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
                                onClick = {
                                    newChainName = ""
                                    showCreateDialog = true
                                },
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
                                onAction = {
                                    newChainName = ""
                                    showCreateDialog = true
                                },
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(filteredTemplates) { name ->
                                    val isSel = name == selectedTemplate
                                    ChainListItem(
                                        name = name,
                                        isSelected = isSel,
                                        onClick = { selectedTemplate = name },
                                        onDelete = { deleteTarget = name },
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
                                            val name = editingChain!!.name
                                            if (!name.isNullOrBlank()) {
                                                ConfigBuilder.saveTemplate("chains", name, json.encodeToString(editingChain))
                                                isDirty = false
                                                reload()
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
                SaaSTextField(
                    value = newChainName,
                    onValueChange = { newChainName = it },
                    label = "Chain Name",
                    modifier = Modifier.fillMaxWidth(),
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
                    Spacer(Modifier.width(Spacing.md))
                    SaaSButton(
                        text = "Create",
                        onClick = {
                            if (newChainName.isNotBlank()) {
                                val newChain = ChainDto(name = newChainName, hops = listOf(HopDto("hop-1", listOf(NodeDto()))))
                                ConfigBuilder.saveTemplate("chains", newChainName, json.encodeToString(newChain))
                                reload()
                                selectedTemplate = newChainName
                                showCreateDialog = false
                            }
                        },
                        enabled = newChainName.isNotBlank(),
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
                    val dir = java.io.File(System.getProperty("user.home"), ".gost-manager/templates/chains")
                    java.io.File(dir, "$deleteTarget.json").delete()
                    if (selectedTemplate == deleteTarget) selectedTemplate = null
                    deleteTarget = null
                    reload()
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
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = "Chain",
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) sc.statusSuccess else sc.textMuted,
            )
            Spacer(Modifier.width(Spacing.md))
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) sc.textPrimary else sc.textSecondary,
            )
            if (isSelected) {
                IconTooltipButton(
                    tooltip = "Delete chain",
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = sc.textMuted)
                }
            }
        }
    }
}
