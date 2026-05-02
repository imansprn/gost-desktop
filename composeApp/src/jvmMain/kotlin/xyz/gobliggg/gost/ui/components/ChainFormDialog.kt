package xyz.gobliggg.gost.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.gobliggg.gost.api.dto.AuthDto
import xyz.gobliggg.gost.api.dto.ChainDto
import xyz.gobliggg.gost.api.dto.ConnectorDto
import xyz.gobliggg.gost.api.dto.DialerDto
import xyz.gobliggg.gost.api.dto.HopDto
import xyz.gobliggg.gost.api.dto.NodeDto
import xyz.gobliggg.gost.ui.theme.*

private fun isSshLikeDialer(type: String?): Boolean = type == "ssh" || type == "sshd"

private fun mergedDialerAuth(node: NodeDto): AuthDto = node.dialer?.auth ?: node.auth ?: AuthDto()

private fun mergedDialerMetadata(node: NodeDto): Map<String, String> = node.dialer?.metadata ?: emptyMap()

@Composable
fun ChainFormDialog(
    initialChain: ChainDto?,
    onSave: (ChainDto) -> Unit,
    onDismiss: () -> Unit,
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var name by remember { mutableStateOf(initialChain?.name ?: "") }
    val isEdit = initialChain != null
    var hops by remember {
        mutableStateOf(
            initialChain?.hops?.toMutableList()
                ?: mutableListOf(HopDto(name = "hop-1", nodes = mutableListOf(NodeDto()))),
        )
    }
    val sc = GostSemantics.colors

    SaaSDialog(
        title = if (isEdit) "Edit Chain" else "New Chain",
        onDismissRequest = onDismiss,
        size = SaaSDialogSize.Xl,
        showSplit = true,
        leftContent = {
            SaaSTableHeader("WIZARD STEPS")
            Spacer(Modifier.height(Spacing.md))

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                WizardStep(1, "Basic Configuration", true)
                WizardStep(2, "Hop Rules & Nodes", true)
            }

            Spacer(Modifier.weight(1f))

            SaaSTableHeader("SUMMARY")
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "${hops.size} hops, ${hops.sumOf { it.nodes?.size ?: 0 }} nodes",
                color = sc.textSecondary.copy(alpha = 0.85f),
                fontSize = 12.sp,
            )
        },
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SaaSTableHeader("BASIC IDENTIFIER")
            Spacer(Modifier.height(Spacing.md))

            SaaSTextField(
                label = "Chain Name *",
                value = name,
                onValueChange = { name = it },
                enabled = !isEdit,
                placeholder = "my-chain",
                helperText = "Unique name to reference this chain",
            )

            Spacer(Modifier.height(Spacing.xl))
            SaaSTableHeader("HOPS CONFIGURATION")
            Spacer(Modifier.height(Spacing.md))

            hops.forEachIndexed { hopIdx, hop ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(sc.surfaceCard)
                        .border(1.dp, sc.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "HOP ${hopIdx + 1}",
                            color = sc.statusSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = {
                                val newList = hops.toMutableList()
                                newList.removeAt(hopIdx)
                                hops = newList
                                focusManager.clearFocus()
                            },
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remove Hop",
                                tint = RedStatus,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    SaaSTextField(
                        value = hop.name ?: "",
                        onValueChange = {
                            val newList = hops.toMutableList()
                            newList[hopIdx] = hop.copy(name = it)
                            hops = newList
                        },
                        placeholder = "Hop Name",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        "NODES IN THIS HOP",
                        color = sc.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    hop.nodes?.forEachIndexed { nodeIdx, node ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(sc.surfaceInput)
                                .border(1.dp, sc.borderSubtle, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                SaaSTextField(
                                    value = node.addr ?: "",
                                    onValueChange = {
                                        val newHops = hops.toMutableList()
                                        val newNodes = (hop.nodes ?: emptyList()).toMutableList()
                                        newNodes[nodeIdx] = node.copy(addr = it)
                                        newHops[hopIdx] = hop.copy(nodes = newNodes)
                                        hops = newHops
                                    },
                                    placeholder = "Endpoint (e.g. 1.2.3.4:1080)",
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(
                                    onClick = {
                                        val newHops = hops.toMutableList()
                                        val newNodes = (hop.nodes ?: emptyList()).toMutableList()
                                        newNodes.removeAt(nodeIdx)
                                        newHops[hopIdx] = hop.copy(nodes = newNodes)
                                        hops = newHops
                                        focusManager.clearFocus()
                                    },
                                ) {
                                    Icon(
                                        Icons.Default.RemoveCircle,
                                        contentDescription = "Remove Node",
                                        tint = RedStatus,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "Connector",
                                        color = sc.textMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    ChainHopSimpleDropdown(
                                        options = listOf("http", "socks4", "socks5", "relay", "ss", "forward", "sshd", "snid"),
                                        selected = node.connector?.type ?: "http",
                                        onSelect = {
                                            val newHops = hops.toMutableList()
                                            val newNodes = (hop.nodes ?: emptyList()).toMutableList()
                                            newNodes[nodeIdx] = node.copy(connector = ConnectorDto(type = it))
                                            newHops[hopIdx] = hop.copy(nodes = newNodes)
                                            hops = newHops
                                        },
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "Dialer",
                                        color = sc.textMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    ChainHopSimpleDropdown(
                                        options =
                                            listOf(
                                                "tcp",
                                                "udp",
                                                "tls",
                                                "mtls",
                                                "ws",
                                                "grpc",
                                                "quic",
                                                "kcp",
                                                "ssh",
                                                "sshd",
                                                "icmp",
                                                "ohttp",
                                            ),
                                        selected = node.dialer?.type ?: "tcp",
                                        onSelect = {
                                            val newHops = hops.toMutableList()
                                            val newNodes = (hop.nodes ?: emptyList()).toMutableList()
                                            val newDialer =
                                                if (isSshLikeDialer(it)) {
                                                    DialerDto(
                                                        type = it,
                                                        auth = node.dialer?.auth ?: node.auth,
                                                        metadata = node.dialer?.metadata,
                                                    )
                                                } else {
                                                    DialerDto(type = it)
                                                }
                                            newNodes[nodeIdx] =
                                                node.copy(
                                                    dialer = newDialer,
                                                    auth = if (isSshLikeDialer(it)) null else node.auth,
                                                )
                                            newHops[hopIdx] = hop.copy(nodes = newNodes)
                                            hops = newHops
                                        },
                                    )
                                }
                            }

                            if (isSshLikeDialer(node.dialer?.type)) {
                                SshDialerAuthBlock(
                                    node = node,
                                    onUpdate = { updated ->
                                        val newHops = hops.toMutableList()
                                        val newNodes = (hop.nodes ?: emptyList()).toMutableList()
                                        newNodes[nodeIdx] = updated
                                        newHops[hopIdx] = hop.copy(nodes = newNodes)
                                        hops = newHops
                                    },
                                )
                            }
                        }
                    }

                    SaaSButton(
                        text = "Add Node",
                        onClick = {
                            val newHops = hops.toMutableList()
                            newHops[hopIdx] = hop.copy(nodes = (hop.nodes ?: emptyList()) + NodeDto())
                            hops = newHops
                        },
                        type = SaaSButtonType.SECONDARY,
                        icon = Icons.Default.Add,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))
            SaaSButton(
                text = "Add Next Hop",
                onClick = {
                    hops = (hops + HopDto(name = "hop-${hops.size + 1}", nodes = mutableListOf(NodeDto()))).toMutableList()
                },
                type = SaaSButtonType.SECONDARY,
                icon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth(),
            )

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
                    text = "Save Chain",
                    onClick = { onSave(ChainDto(name = name, hops = hops)) },
                    enabled =
                        name.isNotBlank() &&
                            hops.isNotEmpty() &&
                            hops.all { h -> !h.nodes.isNullOrEmpty() && h.nodes!!.all { !it.addr.isNullOrBlank() } },
                    type = SaaSButtonType.ACTION,
                    icon = Icons.Default.Save,
                    modifier = Modifier.widthIn(max = 160.dp),
                )
            }
        }
    }
}

@Composable
private fun WizardStep(
    num: Int,
    label: String,
    isActive: Boolean,
) {
    val sc = GostSemantics.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isActive) sc.stateSelected else sc.surfaceInput),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$num",
                color = if (isActive) sc.statusSuccess else sc.textDisabled,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(label, color = if (isActive) sc.textPrimary else sc.textSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun SshDialerAuthBlock(
    node: NodeDto,
    onUpdate: (NodeDto) -> Unit,
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val dial = node.dialer ?: DialerDto(type = "ssh")
    val auth = mergedDialerAuth(node)
    val meta = mergedDialerMetadata(node).toMutableMap()
    val usePublicKey = meta.containsKey("privateKeyFile")
    val keyFile = meta["privateKeyFile"].orEmpty()
    val passphrase = meta["passphrase"].orEmpty()
    val sc = GostSemantics.colors

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(sc.surfaceCard)
                .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "SSH DIALER AUTH",
            color = sc.textMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )

        SaaSTextField(
            value = auth.username.orEmpty(),
            onValueChange = { v ->
                onUpdate(
                    node.copy(
                        dialer = dial.copy(auth = auth.copy(username = v.ifBlank { null })),
                        auth = null,
                    ),
                )
            },
            placeholder = "Username",
            modifier = Modifier.fillMaxWidth(),
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(false to "Password", true to "Private Key").forEach { (isKey, label) ->
                val active = usePublicKey == isKey
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (active) sc.stateSelected else sc.surfaceInput)
                        .border(1.dp, if (active) sc.statusSuccess.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(6.dp))
                        .clickable {
                            if (isKey) {
                                val m = meta.toMutableMap()
                                if (!m.containsKey("privateKeyFile")) m["privateKeyFile"] = ""
                                onUpdate(node.copy(dialer = dial.copy(auth = auth.copy(password = null), metadata = m), auth = null))
                            } else {
                                val m = meta.filterKeys { it != "privateKeyFile" && it != "passphrase" }.toMutableMap()
                                onUpdate(
                                    node.copy(
                                        dialer =
                                            dial.copy(
                                                auth = auth.copy(password = auth.password),
                                                metadata = m.takeIf { it.isNotEmpty() },
                                            ),
                                        auth = null,
                                    ),
                                )
                            }
                            focusManager.clearFocus()
                        }.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        color = if (active) sc.statusSuccess else sc.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        if (!usePublicKey) {
            SaaSTextField(
                value = auth.password.orEmpty(),
                onValueChange = { v ->
                    onUpdate(
                        node.copy(
                            dialer = dial.copy(auth = auth.copy(password = v.ifBlank { null })),
                            auth = null,
                        ),
                    )
                },
                placeholder = "Password (optional)",
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
            )
        } else {
            SaaSTextField(
                value = keyFile,
                onValueChange = { v ->
                    val m = mergedDialerMetadata(node).toMutableMap()
                    m["privateKeyFile"] = v
                    if (m["passphrase"].isNullOrBlank()) m.remove("passphrase")
                    onUpdate(
                        node.copy(
                            dialer = dial.copy(auth = auth.copy(username = auth.username, password = null), metadata = m),
                            auth = null,
                        ),
                    )
                },
                placeholder = "Private key file path",
                modifier = Modifier.fillMaxWidth(),
            )
            SaaSTextField(
                value = passphrase,
                onValueChange = { v ->
                    val m = mergedDialerMetadata(node).toMutableMap()
                    if (!m.containsKey("privateKeyFile")) m["privateKeyFile"] = keyFile
                    if (v.isBlank()) m.remove("passphrase") else m["passphrase"] = v
                    onUpdate(
                        node.copy(
                            dialer = dial.copy(auth = auth.copy(username = auth.username, password = null), metadata = m),
                            auth = null,
                        ),
                    )
                },
                placeholder = "Key passphrase (optional)",
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
            )
        }
    }
}

@Composable
private fun ChainHopSimpleDropdown(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val sc = GostSemantics.colors
    SearchableStringDropdown(
        selected = selected,
        options = options,
        onSelect = onSelect,
        modifier = Modifier.fillMaxWidth(),
        menuWidthMin = 200,
        menuMaxHeight = 240,
        searchPlaceholder = "Search types…",
    ) { onOpen ->
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(sc.surfaceInput)
                    .border(1.dp, sc.borderSubtle, RoundedCornerShape(8.dp))
                    .clickable {
                        onOpen()
                        focusManager.clearFocus()
                    }.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    selected,
                    color = sc.textPrimary,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = sc.textSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
