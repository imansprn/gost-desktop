package xyz.gobliggg.gost.screen.advanced
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
import xyz.gobliggg.gost.api.dto.*
import xyz.gobliggg.gost.data.ConfigBuilder
import xyz.gobliggg.gost.ui.components.*
import xyz.gobliggg.gost.ui.theme.*

enum class AdvancedTab { BYPASS, ADMISSION, RESOLVERS, HOSTS }

@OptIn(ExperimentalStdlibApi::class)
class AdvancedScreen : Screen {
    @Composable
    override fun Content() {
        var activeTab by remember { mutableStateOf(AdvancedTab.BYPASS) }
        var searchQuery by remember { mutableStateOf("") }
        val sc = GostSemantics.colors

        val templateType =
            remember(activeTab) {
                when (activeTab) {
                    AdvancedTab.BYPASS -> "bypasses"
                    AdvancedTab.ADMISSION -> "admissions"
                    AdvancedTab.RESOLVERS -> "resolvers"
                    AdvancedTab.HOSTS -> "hosts"
                }
            }

        var templates by remember { mutableStateOf(ConfigBuilder.listTemplates(templateType)) }
        var showDialog by remember { mutableStateOf(false) }
        var editingObject by remember { mutableStateOf<Any?>(null) }
        var deleteTarget by remember { mutableStateOf<String?>(null) }

        val json =
            remember {
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }
            }

        fun reload() {
            templates = ConfigBuilder.listTemplates(templateType)
        }

        LaunchedEffect(activeTab) {
            reload()
            searchQuery = ""
        }

        val filteredTemplates =
            remember(templates, searchQuery) {
                templates.filter { it.contains(searchQuery, ignoreCase = true) }
            }

        Box(modifier = Modifier.fillMaxSize()) {
            ScreenScaffold(
                header = {
                    SaaSScreenHeader(
                        superTitle = "TEMPLATES",
                        title = "Advanced Objects",
                    )
                },
            ) {
                TabRow(
                    selectedTabIndex = activeTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = sc.focusRing,
                    divider = { HorizontalDivider(color = sc.borderSubtle) },
                ) {
                    AdvancedTab.values().forEach { tab ->
                        Tab(
                            selected = activeTab == tab,
                            onClick = { activeTab = tab },
                            text = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.xl))

                SaaSSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search ${activeTab.name.lowercase()}…",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Spacing.lg))

                SaaSListContainer(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (filteredTemplates.isEmpty()) {
                        EmptyState(
                            title = if (searchQuery.isEmpty()) "No ${activeTab.name.lowercase()} yet" else "No results found",
                            description = "Advanced objects provide support for tunnel rules, DNS resolution, and static hosts.",
                            icon = Icons.Default.Tune,
                            actionLabel = if (searchQuery.isEmpty()) "Create First" else null,
                            onAction = { showDialog = true },
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                            contentPadding = PaddingValues(bottom = Spacing.xl),
                        ) {
                            items(filteredTemplates) { name ->
                                val content = ConfigBuilder.readTemplate(templateType, name)
                                AdvancedObjectRow(
                                    name = name,
                                    activeTab = activeTab,
                                    content = content,
                                    json = json,
                                    onEdit = {
                                        editingObject = parseObject(activeTab, content, json, name)
                                        showDialog = true
                                    },
                                    onDelete = { deleteTarget = name },
                                )
                            }
                        }
                    }
                }
            }

            val fabLabel =
                when (activeTab) {
                    AdvancedTab.BYPASS -> "New bypass"
                    AdvancedTab.ADMISSION -> "New admission"
                    AdvancedTab.RESOLVERS -> "New resolver"
                    AdvancedTab.HOSTS -> "New hosts"
                }
            FloatingActionButton(
                onClick = {
                    editingObject = null
                    showDialog = true
                },
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(Spacing.xl),
                containerColor = SaASAction,
                contentColor = sc.focusRing,
                shape = RoundedCornerShape(GostRadius.md),
            ) {
                Icon(Icons.Default.Add, contentDescription = fabLabel)
            }
        }

        if (showDialog) {
            AdvancedObjectDialog(
                tab = activeTab,
                initialObject = editingObject,
                onSave = { obj ->
                    val name =
                        when (obj) {
                            is BypassDto -> obj.name
                            is AdmissionDto -> obj.name
                            is ResolverDto -> obj.name
                            is HostsDto -> obj.name
                            else -> null
                        }
                    if (!name.isNullOrBlank()) {
                        val stringContent =
                            when (obj) {
                                is BypassDto -> json.encodeToString(obj)
                                is AdmissionDto -> json.encodeToString(obj)
                                is ResolverDto -> json.encodeToString(obj)
                                is HostsDto -> json.encodeToString(obj)
                                else -> ""
                            }
                        ConfigBuilder.saveTemplate(templateType, name, stringContent)
                        reload()
                        showDialog = false
                    }
                },
                onDismiss = { showDialog = false },
            )
        }

        if (deleteTarget != null) {
            ConfirmDialog(
                title = "Delete Object",
                message = "Remove \"$deleteTarget\" from ${activeTab.name.lowercase()}?",
                onConfirm = {
                    val dir = java.io.File(System.getProperty("user.home"), ".gost-manager/templates/$templateType")
                    java.io.File(dir, "$deleteTarget.json").delete()
                    deleteTarget = null
                    reload()
                },
                onDismiss = { deleteTarget = null },
            )
        }
    }

    private fun parseObject(
        tab: AdvancedTab,
        content: String?,
        json: Json,
        name: String,
    ): Any? {
        if (content == null) return null
        return try {
            when (tab) {
                AdvancedTab.BYPASS -> json.decodeFromString<BypassDto>(content)
                AdvancedTab.ADMISSION -> json.decodeFromString<AdmissionDto>(content)
                AdvancedTab.RESOLVERS -> json.decodeFromString<ResolverDto>(content)
                AdvancedTab.HOSTS -> json.decodeFromString<HostsDto>(content)
            }
        } catch (e: Exception) {
            null
        }
    }

    @Composable
    private fun AdvancedObjectRow(
        name: String,
        activeTab: AdvancedTab,
        content: String?,
        json: Json,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SaASSlate.copy(0.4f))
                    .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(12.dp))
                    .clickable { onEdit() }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector =
                    when (activeTab) {
                        AdvancedTab.BYPASS -> Icons.Default.MoveToInbox
                        AdvancedTab.ADMISSION -> Icons.Default.FactCheck
                        AdvancedTab.RESOLVERS -> Icons.Default.Dns
                        AdvancedTab.HOSTS -> Icons.Default.ViewList
                    },
                contentDescription = "Type: ${activeTab.name.lowercase()}",
                modifier = Modifier.size(18.dp),
                tint = Cyan300,
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)

                // Subtitle info
                val info =
                    remember(content) {
                        if (content == null) {
                            ""
                        } else {
                            try {
                                when (activeTab) {
                                    AdvancedTab.BYPASS -> {
                                        val d = json.decodeFromString<BypassDto>(content)
                                        "${if (d.reverse == true) "Whitelist" else "Blacklist"} • ${d.matchers?.size ?: 0} rules"
                                    }
                                    AdvancedTab.ADMISSION -> {
                                        val d = json.decodeFromString<AdmissionDto>(content)
                                        "${if (d.reverse == true) "Allow" else "Deny"} • ${d.matchers?.size ?: 0} rules"
                                    }
                                    AdvancedTab.RESOLVERS -> {
                                        val d = json.decodeFromString<ResolverDto>(content)
                                        "${d.nameservers?.size ?: 0} servers • TTL ${d.ttl ?: "def"}"
                                    }
                                    AdvancedTab.HOSTS -> {
                                        val d = json.decodeFromString<HostsDto>(content)
                                        "${d.mappings?.size ?: 0} mappings"
                                    }
                                }
                            } catch (e: Exception) {
                                "Error parsing template"
                            }
                        }
                    }
                Text(info, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
            }

            Row {
                IconTooltipButton(
                    tooltip = "Edit",
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                }
                IconTooltipButton(
                    tooltip = "Delete",
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedStatus, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
