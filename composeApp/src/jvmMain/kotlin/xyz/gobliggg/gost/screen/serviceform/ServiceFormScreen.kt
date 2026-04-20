package xyz.gobliggg.gost.screen.serviceform
import xyz.gobliggg.gost.ui.components.*

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import xyz.gobliggg.gost.ui.GlobalWindowShortcuts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import xyz.gobliggg.gost.ui.components.ChainFormDialog
import xyz.gobliggg.gost.ui.components.FormField
import xyz.gobliggg.gost.ui.components.SearchableStringDropdown
import xyz.gobliggg.gost.ui.theme.Spacing
import xyz.gobliggg.gost.ui.theme.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class ServiceFormScreen(
    /** Matches the shell stack segment so Voyager does not reuse this screen's model for another wizard. */
    private val routeId: String,
    private val editName: String? = null,
    private val onDone: () -> Unit = {},
    private val onCancel: () -> Unit = {},
) : Screen {

    override val key: ScreenKey = routeId



    @Composable
    override fun Content() {
        val model = rememberScreenModel { ServiceFormScreenModel(editName) }
        val state by model.state.collectAsState()
        var chainDialogOpen by remember { mutableStateOf(false) }
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        val sc = GostSemantics.colors

        DisposableEffect(model, onDone, state.currentStep) {
            if (state.currentStep == 2) {
                val save = { model.save(onDone) }
                GlobalWindowShortcuts.saveHandler = save
                onDispose {
                    if (GlobalWindowShortcuts.saveHandler === save) GlobalWindowShortcuts.saveHandler = null
                }
            } else {
                onDispose { }
            }
        }

        DisposableEffect(editName) {
            onDispose { model.persistDraftToDiskIfNeeded() }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // ── Left: Wizard form ──
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(Spacing.xl),
            ) {
                SaaSScreenHeader(
                    superTitle = "WIZARD",
                    title = if (state.isEditMode) "Edit Tunnel" else "New Tunnel"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    StepIndicator(0, "Basic", state.currentStep) { model.goToStep(it) }
                    StepIndicator(1, "Protocol", state.currentStep) { model.goToStep(it) }
                    StepIndicator(2, "Advanced", state.currentStep) { model.goToStep(it) }
                }
                Spacer(Modifier.height(Spacing.xl))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                ) {
                    when (state.currentStep) {
                        0 -> Step1Basic(state, model)
                        1 -> Step2Protocol(state, model)
                        2 -> Step3Advanced(state, model) { chainDialogOpen = true }
                    }

                    if (state.errorMessage != null) {
                        Spacer(Modifier.height(Spacing.md))
                        Banner(state.errorMessage!!, type = BannerType.Error)
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SaaSButton(
                        text = if (state.currentStep == 0) "Cancel" else "Back",
                        onClick = { if (state.currentStep == 0) onCancel() else model.prevStep() },
                        type = SaaSButtonType.SECONDARY,
                        icon = if (state.currentStep == 0) null else Icons.Default.ArrowBack
                    )
                    if (state.currentStep < 2) {
                        SaaSButton(
                            text = "Next",
                            onClick = { model.nextStep() },
                            type = SaaSButtonType.PRIMARY,
                            icon = Icons.Default.ArrowForward
                        )
                    } else {
                        SaaSButton(
                            text = if (state.isEditMode) "Save Changes" else "Create Tunnel",
                            onClick = { model.save(onDone) },
                            enabled = !state.isSubmitting,
                            loading = state.isSubmitting,
                            type = SaaSButtonType.ACTION,
                            icon = Icons.Default.Save
                        )
                    }
                }
            }

            // ── Right: Live Preview ──
            VerticalDivider(thickness = 1.dp, color = sc.borderSubtle)
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
                    .background(sc.surfacePanel)
                    .padding(Spacing.lg),
            ) {
                SaaSTableHeader("LIVE PREVIEW")
                Spacer(Modifier.height(8.dp))
                val preview = remember(state) { model.buildPreviewJson() }
                Text(
                    text = preview,
                    color = Cyan300,
                    fontSize = 11.sp,
                    fontFamily = MonoFontFamily,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(sc.surfaceApp)
                        .border(1.dp, sc.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                )
            }
        }

        if (chainDialogOpen) {
            ChainFormDialog(
                initialChain = null,
                onSave = { ch ->
                    model.createChainFromWizard(ch) { err ->
                        if (err == null) chainDialogOpen = false
                    }
                },
                onDismiss = { chainDialogOpen = false },
            )
        }
    }
}

@Composable
private fun StepIndicator(
    step: Int,
    label: String,
    currentStep: Int,
    onStepClick: (Int) -> Unit,
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val sc = GostSemantics.colors
    val isActive = step == currentStep
    val isDone = step < currentStep
    val color = when {
        isDone -> GreenBright
        isActive -> Cyan300
        else -> sc.textMuted
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onStepClick(step)
                focusManager.clearFocus()
            },
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isActive || isDone) SaaSSelection
                    else SaaSInputBg,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("${step + 1}", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(Spacing.sm))
        Text(label, color = color, fontSize = 12.sp, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal)
        if (step < 2) {
            Spacer(Modifier.width(8.dp))
            Text("—", color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun Step1Basic(state: ServiceFormUiState, model: ServiceFormScreenModel) {
    FormField(
        "Tunnel Name *",
        state.name,
        model::updateName,
        error = state.nameError,
        placeholder = "my-proxy",
        helperText = "Unique identifier, no spaces allowed",
    )
    Spacer(Modifier.height(16.dp))
    FormField(
        "Listen Address *",
        state.addr,
        model::updateAddr,
        error = state.addrError,
        placeholder = ":8080 or 0.0.0.0:1080",
        helperText = "Use :PORT to listen on all interfaces, or HOST:PORT for a specific bind address",
    )
}

@Composable
private fun Step2Protocol(state: ServiceFormUiState, model: ServiceFormScreenModel) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    Text(
        "Handler Type",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
    )
    Text(
        "Protocol used to handle incoming connections",
        color = MaterialTheme.colorScheme.outline,
        style = MaterialTheme.typography.labelSmall,
    )
    Spacer(Modifier.height(4.dp))
    SearchableStringDropdown(
        selected = state.handlerType,
        options = HANDLER_TYPES,
        onSelect = model::updateHandlerType,
        searchPlaceholder = "Search handler types…",
    ) { onOpen ->
        OutlinedButton(
            onClick = {
                onOpen()
                focusManager.clearFocus()
            },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(state.handlerType, fontSize = 13.sp)
        }
    }
    Spacer(Modifier.height(16.dp))
    Text(
        "Listener Type",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
    )
    Text(
        "Transport layer protocol for accepting connections",
        color = MaterialTheme.colorScheme.outline,
        style = MaterialTheme.typography.labelSmall,
    )
    Spacer(Modifier.height(4.dp))
    SearchableStringDropdown(
        selected = state.listenerType,
        options = LISTENER_TYPES,
        onSelect = model::updateListenerType,
        searchPlaceholder = "Search listener types…",
    ) { onOpen ->
        OutlinedButton(onClick = onOpen, shape = RoundedCornerShape(8.dp)) {
            Text(state.listenerType, fontSize = 13.sp)
        }
    }
    Spacer(Modifier.height(16.dp))
    Text(
        "Inline Auth (optional)",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
    )
    Text(
        "Simple username/password authentication for this tunnel",
        color = MaterialTheme.colorScheme.outline,
        style = MaterialTheme.typography.labelSmall,
    )
    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SaaSTextField(
            value = state.authUsername,
            onValueChange = model::updateAuthUsername,
            placeholder = "Username",
            modifier = Modifier.weight(1f),
        )
        SaaSTextField(
            value = state.authPassword,
            onValueChange = model::updateAuthPassword,
            placeholder = "Password",
            modifier = Modifier.weight(1f),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = {
                    passwordVisible = !passwordVisible
                    focusManager.clearFocus()
                }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    )
                }
            },
        )
    }
}

@Composable
private fun Step3Advanced(state: ServiceFormUiState, model: ServiceFormScreenModel, onCreateChain: () -> Unit) {
    Text(
        "Forwarder / remote targets (optional)",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
    )
    Spacer(Modifier.height(4.dp))
    Text(
        "When set, traffic is forwarded to these host:port targets after the handler runs (often used with a chain). Leave empty for simple proxies.",
        color = MaterialTheme.colorScheme.outline,
        style = MaterialTheme.typography.labelSmall,
        lineHeight = 14.sp,
    )
    Spacer(Modifier.height(8.dp))
    state.forwarderNodes.forEachIndexed { index, pair ->
        val (nodeName, nodeAddr) = pair
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = nodeName,
                onValueChange = { model.updateForwarderName(index, it) },
                modifier = Modifier.weight(0.38f),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                placeholder = {
                    Text("Name", color = DarkTextDim, style = MaterialTheme.typography.bodyMedium)
                },
                colors = saasTextFieldColors()
            )
            OutlinedTextField(
                value = nodeAddr,
                onValueChange = { model.updateForwarderAddr(index, it) },
                modifier = Modifier.weight(0.52f),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                placeholder = {
                    Text(
                        "192.168.1.104:9014",
                        color = DarkTextDim,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                colors = saasTextFieldColors()
            )
            TextButton(onClick = { model.removeForwarderRow(index) }) {
                Text("Remove", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(Modifier.height(Spacing.sm))
    }
    OutlinedButton(
        onClick = { model.addForwarderRow() },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text("+ Add forwarder target", fontSize = 12.sp)
    }
    Spacer(Modifier.height(24.dp))

    // ── Protocol Metadata ──
    Text(
        "Protocol Settings (Metadata)",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
    )
    Spacer(Modifier.height(4.dp))
    Text(
        "Custom flags for specific protocols (e.g., 'path' for grpc, 'method' for shadowsocks).",
        color = MaterialTheme.colorScheme.outline,
        style = MaterialTheme.typography.labelSmall,
    )
    Spacer(Modifier.height(12.dp))

    state.metadata.forEachIndexed { index, pair ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SaaSTextField(
                value = pair.first,
                onValueChange = { model.updateMetadataKey(index, it) },
                placeholder = "Key",
                modifier = Modifier.weight(0.4f),
            )
            SaaSTextField(
                value = pair.second,
                onValueChange = { model.updateMetadataValue(index, it) },
                placeholder = "Value",
                modifier = Modifier.weight(0.5f),
            )
            IconButton(
                onClick = { model.removeMetadataRow(index) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(0.4f), modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
    OutlinedButton(
        onClick = { model.addMetadataRow() },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text("+ Add protocol flag", fontSize = 12.sp)
    }
    Spacer(Modifier.height(24.dp))


    NullableDropdown("Chain", state.availableChains, state.chainRef) { model.updateChainRef(it) }
    Spacer(Modifier.height(8.dp))
    OutlinedButton(
        onClick = onCreateChain,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text("+ Create new chain…", fontSize = 12.sp)
    }
    Spacer(Modifier.height(12.dp))
    NullableDropdown("Auther", state.availableAuthers, state.autherRef) { model.updateAutherRef(it) }
    Spacer(Modifier.height(12.dp))
    NullableDropdown("Bypass", state.availableBypasses, state.bypassRef) { model.updateBypassRef(it) }
    Spacer(Modifier.height(12.dp))
    NullableDropdown("Admission", state.availableAdmissions, state.admissionRef) { model.updateAdmissionRef(it) }
    Spacer(Modifier.height(12.dp))
    NullableDropdown("Limiter", state.availableLimiters, state.limiterRef) { model.updateLimiterRef(it) }
}

@Composable
private fun NullableDropdown(label: String, options: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    Text(
        label,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
    )
    Spacer(Modifier.height(4.dp))
    val none = "(none)"
    val opts = remember(options) { listOf(none) + options }
    DropdownField(
        value = selected ?: none,
        options = opts,
        searchable = opts.size >= 10,
        onSelect = { sel -> onSelect(if (sel == none) null else sel) },
    )
}
