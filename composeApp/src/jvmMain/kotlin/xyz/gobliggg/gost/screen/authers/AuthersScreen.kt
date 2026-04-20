package xyz.gobliggg.gost.screen.authers
import xyz.gobliggg.gost.ui.components.*

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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
import xyz.gobliggg.gost.api.dto.AutherDto
import xyz.gobliggg.gost.data.ConfigBuilder
import xyz.gobliggg.gost.ui.theme.Cyan300
import xyz.gobliggg.gost.ui.theme.Spacing
import xyz.gobliggg.gost.ui.theme.*

class AuthersScreen(
    private val onCreateAuther: () -> Unit,
    private val onEditAuther: (String) -> Unit,
) : Screen {

    @Composable
    override fun Content() {
        var templates by remember { mutableStateOf(ConfigBuilder.listTemplates("authers")) }
        var deleteTarget by remember { mutableStateOf<String?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        val sc = GostSemantics.colors

        val json = remember { Json { ignoreUnknownKeys = true; prettyPrint = true } }

        fun reload() {
            templates = ConfigBuilder.listTemplates("authers")
        }

        val filteredTemplates = remember(templates, searchQuery) {
            templates.filter { it.contains(searchQuery, ignoreCase = true) }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            ScreenScaffold(
                header = {
                    SaaSScreenHeader(
                        superTitle = "ACCESS",
                        title = "Auth Rules"
                    )
                }
            ) {
                SaaSSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search auth rules…",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(Spacing.lg))

                SaaSListContainer(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (filteredTemplates.isEmpty()) {
                        EmptyState(
                            title = if (searchQuery.isEmpty()) "No authers yet" else "No results found",
                            description = if (searchQuery.isEmpty())
                                "Authers define sets of credentials for tunnel authentication."
                            else "No auther matches your search query.",
                            icon = Icons.Default.Security,
                            actionLabel = if (searchQuery.isEmpty()) "Create First Auther" else null,
                            onAction = onCreateAuther
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                            contentPadding = PaddingValues(bottom = Spacing.xl)
                        ) {
                            item {
                                AutherTableHeader()
                            }
                            items(filteredTemplates) { name ->
                                val content = ConfigBuilder.readTemplate("authers", name)
                                val dto = remember(content) {
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
                                        onDelete = { deleteTarget = name }
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
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Spacing.xl),
                containerColor = SaASAction,
                contentColor = sc.focusRing,
                shape = RoundedCornerShape(GostRadius.md)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New auther template")
            }
        }

        if (deleteTarget != null) {
            ConfirmDialog(
                title = "Delete Auther",
                message = "Are you sure you want to delete \"$deleteTarget\"?",
                onConfirm = {
                    ConfigBuilder.saveTemplate("authers", deleteTarget!!, "") // Or add real delete to ConfigBuilder
                    // For now I'll use the hack since I saw file deletion in TemplateEditorLayout
                    val dir = java.io.File(System.getProperty("user.home"), ".gost-manager/templates/authers")
                    java.io.File(dir, "$deleteTarget.json").delete()
                    deleteTarget = null
                    reload()
                },
                onDismiss = { deleteTarget = null }
            )
        }
    }

    @Composable
    private fun AutherTableHeader() {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SaaSTableHeader("NAME", modifier = Modifier.weight(1f))
            SaaSTableHeader("TYPE", modifier = Modifier.width(100.dp))
            SaaSTableHeader("USERS/TARGET", modifier = Modifier.width(120.dp))
            Spacer(Modifier.width(80.dp)) // Action column
        }
    }

    @Composable
    private fun AutherRow(
        dto: AutherDto,
        onEdit: () -> Unit,
        onDelete: () -> Unit
    ) {
        val isPlugin = dto.plugin != null
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SaASSlate.copy(0.4f))
                .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(12.dp))
                .clickable { onEdit() }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                dto.name ?: "Unknown",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
            
            Box(
                modifier = Modifier.width(100.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPlugin) Color(0xFF1E3A4A) else SaaSSelection)
                        .padding(horizontal = Spacing.sm, vertical = 4.dp)
                ) {
                    Text(
                        if (isPlugin) "PLUGIN" else "INLINE",
                        color = if (isPlugin) Cyan300 else GreenBright,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info column
            Text(
                if (isPlugin) dto.plugin?.addr ?: "-" else "${dto.auths?.size ?: 0} users",
                modifier = Modifier.width(120.dp),
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Row(modifier = Modifier.width(80.dp), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedStatus, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
