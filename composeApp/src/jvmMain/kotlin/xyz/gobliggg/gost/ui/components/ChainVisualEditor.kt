package xyz.gobliggg.gost.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.gobliggg.gost.api.dto.*
import xyz.gobliggg.gost.ui.theme.*
import xyz.gobliggg.gost.ui.theme.Spacing

@Composable
fun ChainVisualEditor(
    chain: ChainDto,
    onUpdate: (ChainDto) -> Unit,
) {
    val scrollState = rememberScrollState()
    val hops = chain.hops?.toMutableList() ?: mutableListOf()
    val sc = GostSemantics.colors

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        if (hops.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No hops in this chain", color = MaterialTheme.colorScheme.outline)
            }
        }

        hops.forEachIndexed { hopIdx, hop ->
            HopCard(
                hop = hop,
                index = hopIdx,
                isFirst = hopIdx == 0,
                isLast = hopIdx == hops.size - 1,
                onUpdate = { updatedHop ->
                    val newList = hops.toMutableList()
                    newList[hopIdx] = updatedHop
                    onUpdate(chain.copy(hops = newList))
                },
                onDelete = {
                    val newList = hops.toMutableList()
                    newList.removeAt(hopIdx)
                    onUpdate(chain.copy(hops = newList))
                },
                onMoveUp = {
                    if (hopIdx > 0) {
                        val newList = hops.toMutableList()
                        val item = newList.removeAt(hopIdx)
                        newList.add(hopIdx - 1, item)
                        onUpdate(chain.copy(hops = newList))
                    }
                },
                onMoveDown = {
                    if (hopIdx < hops.size - 1) {
                        val newList = hops.toMutableList()
                        val item = newList.removeAt(hopIdx)
                        newList.add(hopIdx + 1, item)
                        onUpdate(chain.copy(hops = newList))
                    }
                },
            )
        }

        OutlinedButton(
            onClick = {
                val newList = (hops + HopDto("hop-${hops.size + 1}", listOf(NodeDto()))).toMutableList()
                onUpdate(chain.copy(hops = newList))
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, sc.borderSubtle),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = sc.textSecondary),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add hop", modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.sm))
            Text("Add Hop")
        }

        Spacer(Modifier.height(Spacing.xxl))
    }
}

@Composable
@Deprecated("Use saasTextFieldColors() for consistent field styling.")
fun darkTextFieldColors() = saasTextFieldColors()

@Composable
private fun HopCard(
    hop: HopDto,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onUpdate: (HopDto) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    val sc = GostSemantics.colors
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(sc.surfaceCard)
                .border(1.dp, sc.borderSubtle, RoundedCornerShape(12.dp))
                .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(20.dp).clip(androidx.compose.foundation.shape.CircleShape).background(sc.borderStrong),
                contentAlignment = Alignment.Center,
            ) {
                Text("${index + 1}", color = sc.textMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Hop ${index + 1}",
                style = MaterialTheme.typography.titleMedium,
                color = sc.textPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.weight(1f))

            // Reordering buttons
            IconButton(onClick = onMoveUp, enabled = !isFirst, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Move Up",
                    modifier = Modifier.size(18.dp),
                    tint = sc.textSecondary,
                )
            }
            IconButton(onClick = onMoveDown, enabled = !isLast, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Move Down",
                    modifier = Modifier.size(18.dp),
                    tint = sc.textSecondary,
                )
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Hop", tint = sc.statusError, modifier = Modifier.size(18.dp))
            }
        }

        Text("Hop Name", fontSize = 11.sp, color = sc.textMuted, fontWeight = FontWeight.SemiBold)
        SaaSTextField(
            value = hop.name ?: "",
            onValueChange = { onUpdate(hop.copy(name = it)) },
            modifier = Modifier.fillMaxWidth().height(40.dp),
            placeholder = "e.g. My Proxy Hop",
        )

        Text("Nodes", style = MaterialTheme.typography.labelSmall, color = sc.textMuted, fontWeight = FontWeight.SemiBold)

        hop.nodes?.forEachIndexed { nodeIdx, node ->
            NodeBlock(
                node = node,
                onUpdate = { updatedNode ->
                    val newList = (hop.nodes ?: emptyList()).toMutableList()
                    newList[nodeIdx] = updatedNode
                    onUpdate(hop.copy(nodes = newList))
                },
                onDelete = {
                    val newList = (hop.nodes ?: emptyList()).toMutableList()
                    newList.removeAt(nodeIdx)
                    onUpdate(hop.copy(nodes = newList))
                },
            )
        }

        TextButton(
            onClick = {
                val newList = ((hop.nodes ?: emptyList<NodeDto>()) + NodeDto()).toMutableList()
                onUpdate(hop.copy(nodes = newList))
            },
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = sc.textMuted),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add node", modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("Add Node", fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun NodeBlock(
    node: NodeDto,
    onUpdate: (NodeDto) -> Unit,
    onDelete: () -> Unit,
) {
    val sc = GostSemantics.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = sc.surfaceInput.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, sc.borderSubtle),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Address", fontSize = 11.sp, color = sc.textMuted, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SaaSTextField(
                        value = node.addr ?: "",
                        onValueChange = { onUpdate(node.copy(addr = it)) },
                        modifier = Modifier.weight(1f).height(40.dp),
                        placeholder = "1.2.3.4:1080",
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Delete Node",
                            tint = sc.statusError,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Connector", style = MaterialTheme.typography.labelSmall, color = sc.textSecondary)
                    TypeDropdown(
                        options = listOf("http", "http2", "socks4", "socks5", "ss", "ssu", "relay", "sni", "sshd", "forward", "ssh"),
                        selected = node.connector?.type ?: "http",
                        onSelect = { onUpdate(node.copy(connector = ConnectorDto(type = it))) },
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("Dialer", style = MaterialTheme.typography.labelSmall, color = sc.textSecondary)
                    TypeDropdown(
                        options =
                            listOf(
                                "tcp",
                                "udp",
                                "tls",
                                "mtls",
                                "ws",
                                "mws",
                                "h2",
                                "h2c",
                                "http2",
                                "grpc",
                                "quic",
                                "kcp",
                                "ssh",
                                "sshd",
                                "icmp",
                                "ohttp",
                                "otls",
                                "ftcp",
                                "http3",
                                "pht",
                            ),
                        selected = node.dialer?.type ?: "tcp",
                        onSelect = {
                            val isSsh = it == "ssh" || it == "sshd"
                            onUpdate(
                                node.copy(
                                    dialer = DialerDto(type = it, auth = if (isSsh) node.dialer?.auth ?: node.auth else null),
                                    auth = if (isSsh) null else node.auth ?: node.dialer?.auth,
                                ),
                            )
                        },
                    )
                }
            }

            if (node.dialer?.type == "ssh" || node.dialer?.type == "sshd") {
                SshAuthBlock(node, onUpdate)
            } else {
                InlineAuthBlock(node, onUpdate)
            }
        }
    }
}

@Composable
private fun TypeDropdown(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    val sc = GostSemantics.colors
    SearchableStringDropdown(
        selected = selected,
        options = options,
        onSelect = onSelect,
        modifier = Modifier.fillMaxWidth().height(40.dp),
        searchPlaceholder = "Search types...",
    ) { onOpen ->
        Surface(
            modifier = Modifier.fillMaxWidth().height(40.dp).clickable { onOpen() },
            shape = RoundedCornerShape(8.dp),
            color = sc.surfaceInput,
            border = BorderStroke(1.dp, sc.borderSubtle),
        ) {
            Row(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                Text(selected, fontSize = 13.sp, color = sc.textPrimary, modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Open dropdown",
                    tint = sc.textSecondary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun InlineAuthBlock(
    node: NodeDto,
    onUpdate: (NodeDto) -> Unit,
) {
    val sc = GostSemantics.colors
    val auth = node.auth ?: AuthDto()

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            Text("User", fontSize = 11.sp, color = sc.textMuted, fontWeight = FontWeight.SemiBold)
            SaaSTextField(
                value = auth.username ?: "",
                onValueChange = { onUpdate(node.copy(auth = auth.copy(username = it.ifBlank { null }))) },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                placeholder = "username",
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            Text("Pass", fontSize = 11.sp, color = sc.textMuted, fontWeight = FontWeight.SemiBold)
            SaaSTextField(
                value = auth.password ?: "",
                onValueChange = { onUpdate(node.copy(auth = auth.copy(password = it.ifBlank { null }))) },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                placeholder = "password",
                visualTransformation = PasswordVisualTransformation(),
            )
        }
    }
}

@Composable
private fun SshAuthBlock(
    node: NodeDto,
    onUpdate: (NodeDto) -> Unit,
) {
    val sc = GostSemantics.colors
    val dial = node.dialer ?: DialerDto(type = "ssh")
    val auth = dial.auth ?: node.auth ?: AuthDto()
    val meta = dial.metadata ?: emptyMap()
    val useKey = meta.containsKey("privateKeyFile")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("SSH Authentication", style = MaterialTheme.typography.labelSmall, color = sc.focusRing, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                Text("User", fontSize = 11.sp, color = sc.textMuted, fontWeight = FontWeight.SemiBold)
                SaaSTextField(
                    value = auth.username ?: "",
                    onValueChange = {
                        onUpdate(node.copy(dialer = dial.copy(auth = auth.copy(username = it.ifBlank { null }))))
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    placeholder = "username",
                )
            }
            if (!useKey) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                    Text("Pass", fontSize = 11.sp, color = sc.textMuted, fontWeight = FontWeight.SemiBold)
                    SaaSTextField(
                        value = auth.password ?: "",
                        onValueChange = {
                            onUpdate(node.copy(dialer = dial.copy(auth = auth.copy(password = it.ifBlank { null }))))
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        placeholder = "password",
                        visualTransformation = PasswordVisualTransformation(),
                    )
                }
            } else {
                Spacer(Modifier.weight(1f))
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = useKey,
                onCheckedChange = { checked ->
                    val newMeta = if (checked) mapOf("privateKeyFile" to "") else emptyMap()
                    onUpdate(node.copy(dialer = dial.copy(metadata = newMeta, auth = if (checked) auth.copy(password = null) else auth)))
                },
                colors = CheckboxDefaults.colors(checkedColor = sc.focusRing, checkmarkColor = Color.Black),
            )
            Text("Use Private Key File", fontSize = 11.sp, color = sc.textSecondary)
        }

        if (useKey) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Key File Path", fontSize = 11.sp, color = sc.textMuted, fontWeight = FontWeight.SemiBold)
                SaaSTextField(
                    value = meta["privateKeyFile"] ?: "",
                    onValueChange = {
                        val newMeta = meta.toMutableMap()
                        newMeta["privateKeyFile"] = it
                        onUpdate(node.copy(dialer = dial.copy(metadata = newMeta)))
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    placeholder = "/path/to/private.key",
                )
            }
        }
    }
}
