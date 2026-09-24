package xyz.gobliggg.gost.screen.serviceform
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import xyz.gobliggg.gost.ui.GlobalWindowShortcuts
import xyz.gobliggg.gost.ui.UnsavedChangesGuard
import xyz.gobliggg.gost.ui.components.*
import xyz.gobliggg.gost.ui.components.ChainFormDialog
import xyz.gobliggg.gost.ui.components.SearchableStringDropdown
import xyz.gobliggg.gost.ui.theme.*
import xyz.gobliggg.gost.ui.theme.Spacing

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

        if (!state.isEditMode && state.draftRecovered) {
            SaaSDialog(
                title = "Resume unfinished tunnel?",
                onDismissRequest = { model.resumeRecoveredDraft() },
                size = SaaSDialogSize.Sm,
            ) {
                Text(
                    "An unfinished tunnel draft was found. Resume it or start with an empty form.",
                    color = sc.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(Spacing.xl))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    SaaSButton(
                        text = "Start Fresh",
                        onClick = { model.startFreshDraft() },
                        type = SaaSButtonType.SECONDARY,
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    SaaSButton(
                        text = "Resume Draft",
                        onClick = { model.resumeRecoveredDraft() },
                        type = SaaSButtonType.PRIMARY,
                    )
                }
            }
        }

        LaunchedEffect(state.isDirty) {
            UnsavedChangesGuard.setDirty(state.isDirty)
        }
        DisposableEffect(Unit) {
            onDispose { UnsavedChangesGuard.clear() }
        }

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
                    title = if (state.isEditMode) "Edit Tunnel" else "New Tunnel",
                )

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    StepIndicator(0, "Basic", state.currentStep) { model.goToStep(it) }
                    StepIndicator(1, "Protocol", state.currentStep) { model.goToStep(it) }
                    StepIndicator(2, "Advanced", state.currentStep) { model.goToStep(it) }
                }
                Spacer(Modifier.height(Spacing.xl))

                Column(
                    modifier =
                        Modifier
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
                        Spacer(Modifier.height(Spacing.sm))
                        Banner(state.errorMessage!!, type = BannerType.Error)
                    }
                    Spacer(Modifier.height(Spacing.xl))
                }

                Spacer(Modifier.height(Spacing.xl))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SaaSButton(
                        text = if (state.currentStep == 0) "Cancel" else "Back",
                        onClick = { if (state.currentStep == 0) onCancel() else model.prevStep() },
                        type = SaaSButtonType.SECONDARY,
                        icon = if (state.currentStep == 0) null else Icons.Default.ArrowBack,
                    )
                    if (state.currentStep < 2) {
                        SaaSButton(
                            text = "Next",
                            onClick = { model.nextStep() },
                            type = SaaSButtonType.PRIMARY,
                            icon = Icons.Default.ArrowForward,
                        )
                    } else {
                        SaaSButton(
                            text = if (state.isEditMode) "Save Changes" else "Create Tunnel",
                            onClick = { model.save(onDone) },
                            enabled = !state.isSubmitting,
                            loading = state.isSubmitting,
                            type = SaaSButtonType.ACTION,
                            icon = Icons.Default.Save,
                        )
                    }
                }
            }

            // ── Right: Live Preview ──
            VerticalDivider(thickness = GostControlSize.borderWidth, color = sc.borderSubtle)
            Column(
                modifier =
                    Modifier
                        .width(GostLayoutSize.editorSidebar)
                        .fillMaxHeight()
                        .background(sc.surfacePanel)
                        .padding(Spacing.lg),
            ) {
                SaaSTableHeader("LIVE PREVIEW")
                Spacer(Modifier.height(Spacing.sm))
                val preview = remember(state) { model.buildPreviewJson() }
                Text(
                    text = preview,
                    color = Cyan300,
                    style = GostTextStyles.logLine,
                    fontFamily = MonoFontFamily,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(GostRadius.md))
                            .background(sc.surfaceApp)
                            .border(GostControlSize.borderWidth, sc.borderSubtle, RoundedCornerShape(GostRadius.md))
                            .padding(Spacing.lg)
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
    val color =
        when {
            isDone -> sc.statusSuccess
            isActive -> sc.focusRing
            else -> sc.textMuted
        }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .clip(RoundedCornerShape(GostRadius.sm))
                .clickable {
                    onStepClick(step)
                    focusManager.clearFocus()
                },
    ) {
        Box(
            Modifier
                .size(GostControlSize.stepIndicator)
                .clip(RoundedCornerShape(GostRadius.md))
                .background(
                    if (isActive || isDone) {
                        sc.stateSelected
                    } else {
                        sc.surfaceInput
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "${step + 1}",
                color = color,
                style = GostTextStyles.pillLabel.copy(fontWeight = FontWeight.Bold),
            )
        }
        Spacer(Modifier.width(Spacing.sm))
        Text(
            label,
            color = color,
            style =
                GostTextStyles.bodyCompact.copy(
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                ),
        )
        if (step < 2) {
            Spacer(Modifier.width(Spacing.sm))
            Text("—", color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.width(Spacing.sm))
        }
    }
}

@Composable
private fun Step1Basic(
    state: ServiceFormUiState,
    model: ServiceFormScreenModel,
) {
    SaaSTextField(
        value = state.name,
        onValueChange = model::updateName,
        label = "Tunnel Name *",
        placeholder = "my-proxy",
        isError = state.nameError != null,
        helperText = state.nameError ?: "Unique identifier, no spaces allowed",
    )
    Spacer(Modifier.height(Spacing.xl))
    SaaSTextField(
        value = state.addr,
        onValueChange = model::updateAddr,
        label = "Listen Address *",
        placeholder = ":8080 or 0.0.0.0:1080",
        isError = state.addrError != null,
        helperText = state.addrError ?: "Use :PORT to listen on all interfaces, or HOST:PORT for a specific bind address",
    )
}

@Composable
private fun Step2Protocol(
    state: ServiceFormUiState,
    model: ServiceFormScreenModel,
) {
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
    Spacer(Modifier.height(Spacing.xs))
    DropdownField(
        value = state.handlerType,
        options = HANDLER_TYPES,
        onSelect = model::updateHandlerType,
        searchable = true,
        contentDescription = "Handler type",
    )
    Spacer(Modifier.height(Spacing.xl))
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
    Spacer(Modifier.height(Spacing.xs))
    DropdownField(
        value = state.listenerType,
        options = LISTENER_TYPES,
        onSelect = model::updateListenerType,
        searchable = true,
        contentDescription = "Listener type",
    )
    Spacer(Modifier.height(Spacing.xl))
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
    Spacer(Modifier.height(Spacing.xs))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
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
                SaaSIconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                        focusManager.clearFocus()
                    },
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        modifier = Modifier.size(GostControlSize.icon),
                    )
                }
            },
        )
    }
}

@Composable
private fun Step3Advanced(
    state: ServiceFormUiState,
    model: ServiceFormScreenModel,
    onCreateChain: () -> Unit,
) {
    val sc = GostSemantics.colors
    Text(
        "Forwarder / remote targets (optional)",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
    )
    Spacer(Modifier.height(Spacing.xs))
    Text(
        "When set, traffic is forwarded to these host:port targets after the handler runs (often used with a chain). Leave empty for simple proxies.",
        color = MaterialTheme.colorScheme.outline,
        style = MaterialTheme.typography.labelSmall,
    )
    Spacer(Modifier.height(Spacing.sm))
    state.forwarderNodes.forEachIndexed { index, pair ->
        val (nodeName, nodeAddr) = pair
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SaaSTextField(
                value = nodeName,
                onValueChange = { model.updateForwarderName(index, it) },
                modifier = Modifier.weight(0.38f),
                placeholder = "Name",
            )
            SaaSTextField(
                value = nodeAddr,
                onValueChange = { model.updateForwarderAddr(index, it) },
                modifier = Modifier.weight(0.52f),
                placeholder = "192.168.1.104:9014",
            )
            IconTooltipButton(
                tooltip = "Remove forwarder",
                onClick = { model.removeForwarderRow(index) },
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove forwarder",
                    tint = sc.statusError,
                    modifier = Modifier.size(GostControlSize.icon),
                )
            }
        }
        Spacer(Modifier.height(Spacing.sm))
    }
    SaaSButton(
        text = "Add forwarder target",
        onClick = { model.addForwarderRow() },
        modifier = Modifier.fillMaxWidth(),
        type = SaaSButtonType.SECONDARY,
        icon = Icons.Default.Add,
    )
    Spacer(Modifier.height(Spacing.xxl))

    // ── Protocol Metadata ──
    Text(
        "Protocol Settings (Metadata)",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
    )
    Spacer(Modifier.height(Spacing.xs))
    Text(
        "Custom flags for specific protocols (e.g., 'path' for grpc, 'method' for shadowsocks).",
        color = MaterialTheme.colorScheme.outline,
        style = MaterialTheme.typography.labelSmall,
    )
    Spacer(Modifier.height(Spacing.lg))

    state.metadata.forEachIndexed { index, pair ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
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
            IconTooltipButton(
                tooltip = "Remove protocol flag",
                onClick = { model.removeMetadataRow(index) },
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove protocol flag",
                    tint = sc.textMuted,
                    modifier = Modifier.size(GostControlSize.icon),
                )
            }
        }
        Spacer(Modifier.height(Spacing.sm))
    }
    SaaSButton(
        text = "Add protocol flag",
        onClick = { model.addMetadataRow() },
        modifier = Modifier.fillMaxWidth(),
        type = SaaSButtonType.SECONDARY,
        icon = Icons.Default.Add,
    )
    Spacer(Modifier.height(Spacing.xxl))

    NullableDropdown("Chain", state.availableChains, state.chainRef) { model.updateChainRef(it) }
    Spacer(Modifier.height(Spacing.sm))
    SaaSButton(
        text = "Create new chain…",
        onClick = onCreateChain,
        modifier = Modifier.fillMaxWidth(),
        type = SaaSButtonType.SECONDARY,
        icon = Icons.Default.Add,
    )
    Spacer(Modifier.height(Spacing.lg))
    NullableDropdown("Auther", state.availableAuthers, state.autherRef) { model.updateAutherRef(it) }
    Spacer(Modifier.height(Spacing.lg))
    NullableDropdown("Bypass", state.availableBypasses, state.bypassRef) { model.updateBypassRef(it) }
    Spacer(Modifier.height(Spacing.lg))
    NullableDropdown("Admission", state.availableAdmissions, state.admissionRef) { model.updateAdmissionRef(it) }
    Spacer(Modifier.height(Spacing.lg))
    NullableDropdown("Limiter", state.availableLimiters, state.limiterRef) { model.updateLimiterRef(it) }
}

@Composable
private fun NullableDropdown(
    label: String,
    options: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    val none = "(none)"
    val opts = remember(options) { listOf(none) + options }
    DropdownField(
        label = label,
        value = selected ?: none,
        options = opts,
        searchable = opts.size >= 10,
        onSelect = { sel -> onSelect(if (sel == none) null else sel) },
    )
}
