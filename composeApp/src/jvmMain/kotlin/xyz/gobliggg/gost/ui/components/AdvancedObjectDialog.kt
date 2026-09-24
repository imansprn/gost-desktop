package xyz.gobliggg.gost.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.gobliggg.gost.api.dto.*
import xyz.gobliggg.gost.screen.advanced.AdvancedTab
import xyz.gobliggg.gost.ui.theme.*

@Composable
fun AdvancedObjectDialog(
    tab: AdvancedTab,
    initialObject: Any? = null,
    onSave: (Any) -> Unit,
    onDismiss: () -> Unit,
) {
    var editTab by remember { mutableStateOf(0) } // 0: Visual, 1: JSON
    val json =
        remember {
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }
        }
    val sc = GostSemantics.colors
    var rawError by remember { mutableStateOf<String?>(null) }

    // Global name state
    var name by remember {
        mutableStateOf(
            when (initialObject) {
                is BypassDto -> initialObject.name ?: ""
                is AdmissionDto -> initialObject.name ?: ""
                is ResolverDto -> initialObject.name ?: ""
                is HostsDto -> initialObject.name ?: ""
                else -> ""
            },
        )
    }

    // JSON Raw state
    var rawJson by remember {
        mutableStateOf(
            if (initialObject != null) {
                when (initialObject) {
                    is BypassDto -> json.encodeToString(initialObject)
                    is AdmissionDto -> json.encodeToString(initialObject)
                    is ResolverDto -> json.encodeToString(initialObject)
                    is HostsDto -> json.encodeToString(initialObject)
                    else -> ""
                }
            } else {
                ""
            },
        )
    }

    SaaSDialog(
        title = "${if (initialObject == null) "New" else "Edit"} ${tab.name.lowercase().capitalize()}",
        onDismissRequest = onDismiss,
        size = SaaSDialogSize.Xl,
        showSplit = true,
        leftContent = {
            SaaSTableHeader("EDITOR MODE")
            Spacer(Modifier.height(Spacing.sm))

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                ModeSelectTab(
                    label = "Visual Editor",
                    description = "Form-based configuration",
                    isSelected = editTab == 0,
                    onClick = { editTab = 0 },
                )
                ModeSelectTab(
                    label = "Raw JSON",
                    description = "Direct configuration edit",
                    isSelected = editTab == 1,
                    onClick = { editTab = 1 },
                )
            }

            Spacer(Modifier.weight(1f))

            SaaSTableHeader("TEMPLATE INFO")
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Category: ${tab.name}",
                color = sc.textSecondary.copy(alpha = 0.85f),
                style = GostTextStyles.bodyCompact,
            )
        },
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            if (editTab == 0) {
                SaaSTableHeader("BASIC IDENTIFIER")
                Spacer(Modifier.height(Spacing.sm))

                SaaSTextField(
                    label = "Identifier Name *",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "template-id",
                    helperText = "Unique name to reference this object",
                )

                Spacer(Modifier.height(Spacing.xl))

                when (tab) {
                    AdvancedTab.BYPASS -> BypassAdmissionForm(name, (initialObject as? BypassDto), onSave = { onSave(it) })
                    AdvancedTab.ADMISSION ->
                        BypassAdmissionForm(
                            name,
                            (initialObject as? AdmissionDto),
                            isAdmission = true,
                            onSave = { onSave(it) },
                        )
                    AdvancedTab.RESOLVERS -> ResolverForm(name, (initialObject as? ResolverDto), onSave = { onSave(it) })
                    AdvancedTab.HOSTS -> HostsForm(name, (initialObject as? HostsDto), onSave = { onSave(it) })
                }
            } else {
                SaaSTableHeader("RAW CONFIGURATION (JSON)")
                Spacer(Modifier.height(Spacing.sm))

                SaaSTextField(
                    value = rawJson,
                    onValueChange = {
                        rawJson = it
                        rawError = null
                    },
                    modifier = Modifier.fillMaxWidth().height(400.dp),
                    placeholder = "Paste JSON here...",
                    singleLine = false,
                    isError = rawError != null,
                    helperText = rawError,
                )
            }

            Spacer(Modifier.height(Spacing.xxl))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                SaaSButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    type = SaaSButtonType.SECONDARY,
                    modifier = Modifier.widthIn(max = GostControlSize.dialogActionMaxWidth),
                )
                if (editTab == 1) {
                    Spacer(Modifier.width(Spacing.sm))
                    SaaSButton(
                        text = "Save Template",
                        onClick = {
                            try {
                                val parsed =
                                    when (tab) {
                                        AdvancedTab.BYPASS -> json.decodeFromString<BypassDto>(rawJson)
                                        AdvancedTab.ADMISSION -> json.decodeFromString<AdmissionDto>(rawJson)
                                        AdvancedTab.RESOLVERS -> json.decodeFromString<ResolverDto>(rawJson)
                                        AdvancedTab.HOSTS -> json.decodeFromString<HostsDto>(rawJson)
                                    }
                                rawError = null
                                onSave(parsed)
                            } catch (e: Exception) {
                                rawError = e.message ?: "Invalid JSON"
                            }
                        },
                        enabled = rawJson.isNotBlank(),
                        type = SaaSButtonType.ACTION,
                        icon = Icons.Default.Save,
                        modifier = Modifier.widthIn(max = GostControlSize.dialogActionMaxWidth),
                    )
                }
            }
        }
    }
}

@Composable
fun BypassAdmissionForm(
    name: String,
    initial: Any?,
    isAdmission: Boolean = false,
    onSave: (Any) -> Unit,
) {
    val sc = GostSemantics.colors
    var reverse by remember {
        mutableStateOf(
            if (isAdmission) {
                (initial as? AdmissionDto)?.reverse ?: false
            } else {
                (initial as? BypassDto)?.reverse ?: false
            },
        )
    }
    var matchers by remember {
        mutableStateOf(
            (
                if (isAdmission) {
                    (initial as? AdmissionDto)?.matchers ?: emptyList()
                } else {
                    (initial as? BypassDto)?.matchers ?: emptyList()
                }
            ).toMutableList(),
        )
    }

    SaaSTableHeader("STRATEGY")
    Spacer(Modifier.height(Spacing.sm))

    SegmentedControl(
        options = listOf(false, true),
        selected = reverse,
        onSelect = { reverse = it },
        label = {
            if (it) {
                if (isAdmission) "ALLOW LIST" else "WHITELIST"
            } else {
                if (isAdmission) "DENY LIST" else "BLACKLIST"
            }
        },
        modifier = Modifier.fillMaxWidth(),
        equalWidth = true,
    )

    Spacer(Modifier.height(Spacing.xl))
    SaaSTableHeader("MATCHING RULES")
    Spacer(Modifier.height(Spacing.sm))

    matchers.forEachIndexed { idx, rule ->
        Row(Modifier.fillMaxWidth().padding(vertical = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
            SaaSTextField(
                value = rule,
                onValueChange = { matchers[idx] = it },
                modifier = Modifier.weight(1f),
                placeholder = "e.g. 192.168.1.0/24 or *.google.com",
            )
            IconTooltipButton(
                tooltip = "Remove rule",
                onClick = { matchers.removeAt(idx) },
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove rule",
                    tint = RedStatus,
                    modifier = Modifier.size(GostControlSize.icon),
                )
            }
        }
    }

    Spacer(Modifier.height(Spacing.sm))
    SaaSButton(
        text = "Add Rule",
        onClick = { matchers.add("") },
        type = SaaSButtonType.SECONDARY,
        icon = Icons.Default.Add,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(Spacing.xl))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        SaaSButton(
            text = "Save Template",
            onClick = {
                val cleaned = matchers.map { it.trim() }.filter { it.isNotBlank() }
                if (isAdmission) {
                    onSave(AdmissionDto(name = name, reverse = reverse, matchers = cleaned))
                } else {
                    onSave(BypassDto(name = name, reverse = reverse, matchers = cleaned))
                }
            },
            enabled = name.matches(Regex("^[a-zA-Z0-9_-]+$")),
            type = SaaSButtonType.ACTION,
            icon = Icons.Default.Save,
        )
    }
}

@Composable
fun ResolverForm(
    name: String,
    initial: ResolverDto?,
    onSave: (ResolverDto) -> Unit,
) {
    val sc = GostSemantics.colors
    var ttl by remember { mutableStateOf(initial?.ttl ?: "") }
    var prefer by remember { mutableStateOf(initial?.prefer ?: "ipv4") }
    var nameservers by remember {
        mutableStateOf(
            (initial?.nameservers ?: listOf(NameserverDto("", ""))).toMutableList(),
        )
    }

    SaaSTableHeader("RESOLVER SETTINGS")
    Spacer(Modifier.height(Spacing.sm))

    SaaSTextField(label = "TTL Override", value = ttl, onValueChange = { ttl = it }, placeholder = "60s")

    Spacer(Modifier.height(Spacing.lg))

    Text("Preference", color = sc.textMuted, style = GostTextStyles.bodyCompact.copy(fontWeight = FontWeight.SemiBold))
    Spacer(Modifier.height(Spacing.sm))
    SegmentedControl(
        options = listOf("ipv4", "ipv6"),
        selected = prefer,
        onSelect = { prefer = it },
        label = { it.uppercase() },
        modifier = Modifier.fillMaxWidth(),
        equalWidth = true,
    )

    Spacer(Modifier.height(Spacing.xl))
    SaaSTableHeader("NAMESERVERS")
    Spacer(Modifier.height(Spacing.sm))

    nameservers.forEachIndexed { idx, ns ->
        Row(Modifier.fillMaxWidth().padding(vertical = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
            SaaSTextField(
                value = ns.addr ?: "",
                onValueChange = { nameservers[idx] = ns.copy(addr = it) },
                modifier = Modifier.weight(0.55f),
                placeholder = "8.8.8.8:53",
            )
            Spacer(Modifier.width(Spacing.sm))
            SaaSTextField(
                value = ns.chain ?: "",
                onValueChange = { nameservers[idx] = ns.copy(chain = it) },
                modifier = Modifier.weight(0.35f),
                placeholder = "Chain ID",
            )
            IconTooltipButton(
                tooltip = "Remove nameserver",
                onClick = { nameservers.removeAt(idx) },
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove nameserver",
                    tint = RedStatus,
                    modifier = Modifier.size(GostControlSize.icon),
                )
            }
        }
    }

    Spacer(Modifier.height(Spacing.sm))
    SaaSButton(
        text = "Add Nameserver",
        onClick = { nameservers.add(NameserverDto("", "")) },
        type = SaaSButtonType.SECONDARY,
        icon = Icons.Default.Add,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(Spacing.xl))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        val cleaned = nameservers.filter { !it.addr.isNullOrBlank() }
        SaaSButton(
            text = "Save Template",
            onClick = {
                onSave(
                    ResolverDto(
                        name = name,
                        nameservers = cleaned,
                        ttl = ttl.ifBlank { null },
                        prefer = prefer,
                    ),
                )
            },
            enabled = name.matches(Regex("^[a-zA-Z0-9_-]+$")) && cleaned.isNotEmpty(),
            type = SaaSButtonType.ACTION,
            icon = Icons.Default.Save,
        )
    }
}

@Composable
fun HostsForm(
    name: String,
    initial: HostsDto?,
    onSave: (HostsDto) -> Unit,
) {
    val sc = GostSemantics.colors
    var mappings by remember {
        mutableStateOf(
            (initial?.mappings ?: listOf(HostMappingDto("", listOf("")))).toMutableList(),
        )
    }

    SaaSTableHeader("HOST MAPPINGS")
    Spacer(Modifier.height(Spacing.sm))

    mappings.forEachIndexed { idx, mapping ->
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm)
                    .clip(RoundedCornerShape(GostRadius.md))
                    .background(sc.surfaceCard)
                    .border(GostControlSize.borderWidth, sc.borderSubtle, RoundedCornerShape(GostRadius.md))
                    .padding(Spacing.lg),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SaaSTextField(
                    value = mapping.ip ?: "",
                    onValueChange = { mappings[idx] = mapping.copy(ip = it) },
                    modifier = Modifier.weight(1f),
                    placeholder = "IP Address (e.g. 1.2.3.4)",
                )
                IconTooltipButton(
                    tooltip = "Remove mapping",
                    onClick = { mappings.removeAt(idx) },
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove mapping",
                        modifier = Modifier.size(GostControlSize.icon),
                        tint = RedStatus,
                    )
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            // Hostnames for this IP
            mapping.hostnames?.forEachIndexed { hIdx, host ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SaaSTextField(
                        value = host,
                        onValueChange = {
                            val newHosts = mapping.hostnames!!.toMutableList()
                            newHosts[hIdx] = it
                            mappings[idx] = mapping.copy(hostnames = newHosts)
                        },
                        modifier = Modifier.weight(1f).padding(start = Spacing.xl),
                        placeholder = "example.com",
                    )
                    IconTooltipButton(
                        tooltip = "Remove hostname",
                        onClick = {
                            val newHosts = mapping.hostnames!!.toMutableList()
                            if (newHosts.size > 1) newHosts.removeAt(hIdx)
                            mappings[idx] = mapping.copy(hostnames = newHosts)
                        },
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove hostname",
                            tint = RedStatus,
                            modifier = Modifier.size(GostControlSize.icon),
                        )
                    }
                }
            }

            SaaSButton(
                text = "Add Hostname",
                onClick = {
                    val newHosts = mapping.hostnames!!.toMutableList()
                    newHosts.add("")
                    mappings[idx] = mapping.copy(hostnames = newHosts)
                },
                modifier = Modifier.padding(start = Spacing.sm),
                type = SaaSButtonType.SECONDARY,
                icon = Icons.Default.Add,
            )
        }
    }

    Spacer(Modifier.height(Spacing.sm))
    SaaSButton(
        text = "Add New IP Mapping",
        onClick = { mappings.add(HostMappingDto("", listOf(""))) },
        type = SaaSButtonType.SECONDARY,
        icon = Icons.Default.Add,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(Spacing.xl))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        val cleaned =
            mappings
                .filter { !it.ip.isNullOrBlank() }
                .map { mapping ->
                    mapping.copy(
                        hostnames = mapping.hostnames?.map { it.trim() }?.filter { it.isNotBlank() },
                    )
                }
                .filter { !it.hostnames.isNullOrEmpty() }

        SaaSButton(
            text = "Save Template",
            onClick = { onSave(HostsDto(name = name, mappings = cleaned)) },
            enabled = name.matches(Regex("^[a-zA-Z0-9_-]+$")) && cleaned.isNotEmpty(),
            type = SaaSButtonType.ACTION,
            icon = Icons.Default.Save,
        )
    }
}

@Composable
private fun ModeSelectTab(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    SaaSModeOption(
        label = label,
        description = description,
        selected = isSelected,
        onClick = onClick,
    )
}
