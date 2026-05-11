package xyz.gobliggg.gost.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.Json
import xyz.gobliggg.gost.data.ConfigBuilder
import xyz.gobliggg.gost.ui.theme.*

@Composable
fun TemplateEditorLayout(
    templateType: String,
    title: String,
    defaultJson: String,
) {
    var templates by remember { mutableStateOf(ConfigBuilder.default().listTemplates(templateType)) }
    var selectedTemplate by remember { mutableStateOf<String?>(null) }
    var templateContent by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<String?>(null) }
    var jsonError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedTemplate) {
        if (selectedTemplate != null) {
            templateContent = ConfigBuilder.default().readTemplate(templateType, selectedTemplate!!) ?: ""
            isEditing = false
            jsonError = null
        } else {
            templateContent = ""
        }
    }

    fun validateAndSave() {
        if (selectedTemplate != null && templateContent.isNotBlank()) {
            // Validate JSON before saving
            try {
                Json.parseToJsonElement(templateContent)
                jsonError = null
                ConfigBuilder.default().saveTemplate(templateType, selectedTemplate!!, templateContent)
                templates = ConfigBuilder.default().listTemplates(templateType)
                isEditing = false
            } catch (e: Exception) {
                jsonError = "Invalid JSON: ${e.message?.take(120)}"
            }
        }
    }

    fun deleteTemplate(name: String) {
        val dir = java.io.File(System.getProperty("user.home"), ".gost-manager/templates/$templateType")
        val file = java.io.File(dir, "$name.json")
        if (file.exists()) file.delete()
        templates = ConfigBuilder.default().listTemplates(templateType)
        if (selectedTemplate == name) {
            selectedTemplate = null
            templateContent = ""
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // ── Left: Template list ──
        Column(modifier = Modifier.width(240.dp).fillMaxHeight()) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val name = "new-$templateType-${System.currentTimeMillis()}"
                    ConfigBuilder.default().saveTemplate(templateType, name, defaultJson.replace("{name}", name))
                    templates = ConfigBuilder.default().listTemplates(templateType)
                    selectedTemplate = name
                    isEditing = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("New Template")
            }

            Spacer(Modifier.height(16.dp))

            if (templates.isEmpty()) {
                EmptyState(
                    title = "No templates",
                    description = "Create a template to get started.",
                    icon = Icons.Default.Description,
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(templates) { t ->
                        val isSel = t == selectedTemplate
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSel) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.background
                                        },
                                    ).clickable { selectedTemplate = t }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = t,
                                modifier = Modifier.weight(1f),
                                color =
                                    if (isSel) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                fontSize = 13.sp,
                                fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                            )
                            IconButton(
                                onClick = { deleteTarget = t },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete $t",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.width(24.dp))
        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.width(24.dp))

        // ── Right: Editor ──
        if (selectedTemplate != null) {
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        selectedTemplate!!,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isEditing) {
                            OutlinedButton(
                                onClick = {
                                    // Discard edits
                                    templateContent = ConfigBuilder.default().readTemplate(templateType, selectedTemplate!!) ?: ""
                                    isEditing = false
                                    jsonError = null
                                },
                                shape = RoundedCornerShape(8.dp),
                            ) { Text("Cancel") }
                            Button(
                                onClick = { validateAndSave() },
                                shape = RoundedCornerShape(8.dp),
                            ) { Text("Save") }
                        } else {
                            OutlinedButton(
                                onClick = { isEditing = true },
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Edit")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                // JSON validation error
                if (jsonError != null) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(12.dp),
                    ) {
                        Text(
                            jsonError!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = templateContent,
                    onValueChange = {
                        templateContent = it
                        jsonError = null
                    },
                    readOnly = !isEditing,
                    modifier = Modifier.fillMaxSize(),
                    textStyle =
                        LocalTextStyle.current.copy(
                            fontFamily = MonoFontFamily,
                            fontSize = 13.sp,
                        ),
                    colors = saasTextFieldColors(),
                )
            }
        } else {
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    title = "No template selected",
                    description = "Select a template from the list to view or edit its content.",
                    icon = Icons.Default.Description,
                )
            }
        }
    }

    // Delete confirmation
    if (deleteTarget != null) {
        ConfirmDialog(
            title = "Delete Template",
            message = "Remove \"$deleteTarget\"? This cannot be undone.",
            onConfirm = {
                deleteTemplate(deleteTarget!!)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null },
        )
    }
}
