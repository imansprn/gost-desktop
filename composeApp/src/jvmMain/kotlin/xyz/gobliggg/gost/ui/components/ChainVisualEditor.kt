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

        SaaSButton(
            text = "Add Hop",
            onClick = {
                val newList = (hops + HopDto("hop-${hops.size + 1}", listOf(NodeDto()))).toMutableList()
                onUpdate(chain.copy(hops = newList))
            },
            modifier = Modifier.fillMaxWidth(),
            type = SaaSButtonType.SECONDARY,
            icon = Icons.Default.Add,
        )

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
                .clip(RoundedCornerShape(GostRadius.md))
                .background(sc.surfaceCard)
                .border(
                    GostControlSize.borderWidth,
                    sc.borderSubtle,
                    RoundedCornerShape(GostRadius.md),
                )
                .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(20.dp).clip(androidx.compose.foundation.shape.CircleShape).background(sc.borderStrong),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "${index + 1}",
                    color = sc.textMuted,
                    style = GostTextStyles.microLabel.copy(fontWeight = FontWeight.Bold),
                )
            }
            Spacer(Modifier.width(Spacing.lg))
            Text(
                "Hop ${index + 1}",
                style = MaterialTheme.typography.titleMedium,
                color = sc.textPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.weight(1f))

            // Reordering buttons
            IconTooltipButton(tooltip = "Move hop up", onClick = onMoveUp, enabled = !isFirst) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Move Hop Up",
                    modifier = Modifier.size(GostControlSize.iconMedium),
                    tint = sc.textSecondary,
                )
            }
            IconTooltipButton(tooltip = "Move hop down", onClick = onMoveDown, enabled = !isLast) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Move Hop Down",
                    modifier = Modifier.size(GostControlSize.iconMedium),
                    tint = sc.textSecondary,
                )
            }
            Spacer(Modifier.width(Spacing.xs))
            IconTooltipButton(tooltip = "Delete hop", onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete Hop",
                    tint = sc.statusError,
                    modifier = Modifier.size(GostControlSize.iconMedium),
                )
            }
        }

        Text("Hop Name", color = sc.textMuted, style = GostTextStyles.pillLabel)
        SaaSTextField(
            value = hop.name ?: "",
            onValueChange = { onUpdate(hop.copy(name = it)) },
            modifier = Modifier.fillMaxWidth(),
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

        SaaSButton(
            text = "Add Node",
            onClick = {
                val newList = ((hop.nodes ?: emptyList<NodeDto>()) + NodeDto()).toMutableList()
                onUpdate(hop.copy(nodes = newList))
            },
            type = SaaSButtonType.SECONDARY,
            icon = Icons.Default.Add,
        )
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
        shape = RoundedCornerShape(GostRadius.md),
        color = sc.surfaceInput.copy(alpha = 0.5f),
        border = BorderStroke(GostControlSize.borderWidth, sc.borderSubtle),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("Address", color = sc.textMuted, style = GostTextStyles.pillLabel)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    SaaSTextField(
                        value = node.addr ?: "",
                        onValueChange = { onUpdate(node.copy(addr = it)) },
                        modifier = Modifier.weight(1f),
                        placeholder = "1.2.3.4:1080",
                    )
                    IconTooltipButton(tooltip = "Delete node", onClick = onDelete) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Delete Node",
                            tint = sc.statusError,
                            modifier = Modifier.size(GostControlSize.icon),
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
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
    DropdownField(
        value = selected,
        options = options,
        onSelect = onSelect,
        modifier = Modifier.fillMaxWidth(),
        searchable = options.size >= 10,
        contentDescription = "Select type",
    )
}

@Composable
private fun InlineAuthBlock(
    node: NodeDto,
    onUpdate: (NodeDto) -> Unit,
) {
    val sc = GostSemantics.colors
    val auth = node.auth ?: AuthDto()

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.xl)) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.weight(1f)) {
            Text("User", color = sc.textMuted, style = GostTextStyles.pillLabel)
            SaaSTextField(
                value = auth.username ?: "",
                onValueChange = { onUpdate(node.copy(auth = auth.copy(username = it.ifBlank { null }))) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "username",
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.weight(1f)) {
            Text("Pass", color = sc.textMuted, style = GostTextStyles.pillLabel)
            SaaSTextField(
                value = auth.password ?: "",
                onValueChange = { onUpdate(node.copy(auth = auth.copy(password = it.ifBlank { null }))) },
                modifier = Modifier.fillMaxWidth(),
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

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xl)) {
        Text("SSH Authentication", style = MaterialTheme.typography.labelSmall, color = sc.focusRing, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xl)) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.weight(1f)) {
                Text("User", color = sc.textMuted, style = GostTextStyles.pillLabel)
                SaaSTextField(
                    value = auth.username ?: "",
                    onValueChange = {
                        onUpdate(node.copy(dialer = dial.copy(auth = auth.copy(username = it.ifBlank { null }))))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "username",
                )
            }
            if (!useKey) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.weight(1f)) {
                    Text("Pass", color = sc.textMuted, style = GostTextStyles.pillLabel)
                    SaaSTextField(
                        value = auth.password ?: "",
                        onValueChange = {
                            onUpdate(node.copy(dialer = dial.copy(auth = auth.copy(password = it.ifBlank { null }))))
                        },
                        modifier = Modifier.fillMaxWidth(),
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
            Text("Use Private Key File", color = sc.textSecondary, style = GostTextStyles.pillLabel)
        }

        if (useKey) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("Key File Path", color = sc.textMuted, style = GostTextStyles.pillLabel)
                SaaSTextField(
                    value = meta["privateKeyFile"] ?: "",
                    onValueChange = {
                        val newMeta = meta.toMutableMap()
                        newMeta["privateKeyFile"] = it
                        onUpdate(node.copy(dialer = dial.copy(metadata = newMeta)))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "/path/to/private.key",
                )
            }
        }
    }
}
