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
            Spacer(Modifier.height(Spacing.md))

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
                fontSize = 12.sp,
            )
        },
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            if (editTab == 0) {
                SaaSTableHeader("BASIC IDENTIFIER")
                Spacer(Modifier.height(Spacing.md))

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
                Spacer(Modifier.height(Spacing.md))

                SaaSTextField(
                    value = rawJson,
                    onValueChange = { rawJson = it },
                    modifier = Modifier.fillMaxWidth().height(400.dp),
                    placeholder = "Paste JSON here...",
                    singleLine = false,
                )
            }

            Spacer(Modifier.height(Spacing.xxl))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                SaaSButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    type = SaaSButtonType.SECONDARY,
                    modifier = Modifier.widthIn(max = 160.dp),
                )
                Spacer(Modifier.width(Spacing.md))
                SaaSButton(
                    text = "Save Template",
                    onClick = {
                        if (editTab == 1) {
                            try {
                                val parsed =
                                    when (tab) {
                                        AdvancedTab.BYPASS -> json.decodeFromString<BypassDto>(rawJson)
                                        AdvancedTab.ADMISSION -> json.decodeFromString<AdmissionDto>(rawJson)
                                        AdvancedTab.RESOLVERS -> json.decodeFromString<ResolverDto>(rawJson)
                                        AdvancedTab.HOSTS -> json.decodeFromString<HostsDto>(rawJson)
                                    }
                                onSave(parsed)
                            } catch (e: Exception) {
                                // Error handling
                            }
                        }
                    },
                    enabled = name.isNotBlank() || editTab == 1,
                    type = SaaSButtonType.ACTION,
                    icon = Icons.Default.Save,
                    modifier = Modifier.widthIn(max = 160.dp),
                )
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
    Spacer(Modifier.height(Spacing.md))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        listOf(false to if (isAdmission) "Deny list" else "Blacklist", true to if (isAdmission) "Allow list" else "Whitelist").forEach { (valBool, label) ->
            val active = reverse == valBool
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) sc.stateSelected else sc.surfaceInput)
                    .border(1.dp, if (active) sc.statusSuccess else sc.borderSubtle, RoundedCornerShape(8.dp))
                    .clickable { reverse = valBool }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label.uppercase(),
                    color = if (active) sc.statusSuccess else sc.textSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }
        }
    }

    Spacer(Modifier.height(Spacing.xl))
    SaaSTableHeader("MATCHING RULES")
    Spacer(Modifier.height(Spacing.md))

    matchers.forEachIndexed { idx, rule ->
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            SaaSTextField(
                value = rule,
                onValueChange = { matchers[idx] = it },
                modifier = Modifier.weight(1f),
                placeholder = "e.g. 192.168.1.0/24 or *.google.com",
            )
            IconButton(onClick = { matchers.removeAt(idx) }) {
                Icon(Icons.Default.Close, contentDescription = null, tint = RedStatus, modifier = Modifier.size(16.dp))
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
    Spacer(Modifier.height(Spacing.md))

    SaaSTextField(label = "TTL Override", value = ttl, onValueChange = { ttl = it }, placeholder = "60s")

    Spacer(Modifier.height(Spacing.lg))

    Text("Preference", color = sc.textMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(Spacing.sm))
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        listOf("ipv4", "ipv6").forEach { p ->
            val active = prefer == p
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) sc.stateSelected else sc.surfaceInput)
                    .border(1.dp, if (active) sc.statusSuccess else sc.borderSubtle, RoundedCornerShape(8.dp))
                    .clickable { prefer = p }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    p.uppercase(),
                    color = if (active) sc.statusSuccess else sc.textSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }
        }
    }

    Spacer(Modifier.height(Spacing.xl))
    SaaSTableHeader("NAMESERVERS")
    Spacer(Modifier.height(Spacing.md))

    nameservers.forEachIndexed { idx, ns ->
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            SaaSTextField(
                value = ns.addr ?: "",
                onValueChange = { nameservers[idx] = ns.copy(addr = it) },
                modifier = Modifier.weight(0.55f),
                placeholder = "8.8.8.8:53",
            )
            Spacer(Modifier.width(8.dp))
            SaaSTextField(
                value = ns.chain ?: "",
                onValueChange = { nameservers[idx] = ns.copy(chain = it) },
                modifier = Modifier.weight(0.35f),
                placeholder = "Chain ID",
            )
            IconButton(onClick = { nameservers.removeAt(idx) }) {
                Icon(Icons.Default.Close, contentDescription = null, tint = RedStatus, modifier = Modifier.size(16.dp))
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
    Spacer(Modifier.height(Spacing.md))

    mappings.forEachIndexed { idx, mapping ->
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(sc.surfaceCard)
                    .border(1.dp, sc.borderSubtle, RoundedCornerShape(12.dp))
                    .padding(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SaaSTextField(
                    value = mapping.ip ?: "",
                    onValueChange = { mappings[idx] = mapping.copy(ip = it) },
                    modifier = Modifier.weight(1f),
                    placeholder = "IP Address (e.g. 1.2.3.4)",
                )
                IconButton(onClick = { mappings.removeAt(idx) }) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp), tint = RedStatus)
                }
            }
            Spacer(Modifier.height(12.dp))
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
                        modifier = Modifier.weight(1f).padding(start = 16.dp),
                        placeholder = "example.com",
                    )
                    IconButton(onClick = {
                        val newHosts = mapping.hostnames!!.toMutableList()
                        if (newHosts.size > 1) newHosts.removeAt(hIdx)
                        mappings[idx] = mapping.copy(hostnames = newHosts)
                    }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = RedStatus, modifier = Modifier.size(14.dp))
                    }
                }
            }

            TextButton(
                onClick = {
                    val newHosts = mapping.hostnames!!.toMutableList()
                    newHosts.add("")
                    mappings[idx] = mapping.copy(hostnames = newHosts)
                },
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = sc.statusSuccess)
                Spacer(Modifier.width(4.dp))
                Text("Add Hostname", fontSize = 11.sp, color = sc.statusSuccess)
            }
        }
    }

    Spacer(Modifier.height(Spacing.md))
    SaaSButton(
        text = "Add New IP Mapping",
        onClick = { mappings.add(HostMappingDto("", listOf(""))) },
        type = SaaSButtonType.SECONDARY,
        icon = Icons.Default.Add,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ModeSelectTab(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val sc = GostSemantics.colors
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) sc.stateSelected else Color.Transparent)
                .border(1.dp, if (isSelected) sc.statusSuccess.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(Spacing.md),
    ) {
        Column {
            Text(label, color = if (isSelected) sc.textPrimary else sc.textSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(description, color = sc.textMuted, fontSize = 11.sp)
        }
    }
}
