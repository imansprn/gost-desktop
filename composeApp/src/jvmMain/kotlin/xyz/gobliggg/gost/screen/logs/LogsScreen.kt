package xyz.gobliggg.gost.screen.logs
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.gobliggg.gost.data.ProcessManager
import xyz.gobliggg.gost.data.ServiceRegistry
import xyz.gobliggg.gost.ui.components.*
import xyz.gobliggg.gost.ui.theme.*
import xyz.gobliggg.gost.ui.theme.Spacing

private data class LogTerminalPalette(
    val background: Color,
    val onSurface: Color,
    val onSurfaceMuted: Color,
    val onSurfaceDim: Color,
)

@Composable
private fun LogLevelFilterChip(
    levelLabel: String,
    isActive: Boolean,
    onToggle: () -> Unit,
) {
    val sc = GostSemantics.colors
    val (activeBg, activeFg) =
        when (levelLabel) {
            "DEBUG" -> sc.surfaceCard to Color.White.copy(alpha = 0.7f)
            "INFO" -> sc.stateSelected to sc.statusSuccess
            "WARN" -> Color(0xFF4B2E0E) to sc.statusWarning
            "ERROR" -> Color(0xFF451313) to sc.statusError
            else -> sc.surfaceCard to Color.White
        }
    val inactiveBg = sc.surfaceInput
    val inactiveFg = Color.White.copy(alpha = 0.4f)
    Box(
        Modifier
            .clip(RoundedCornerShape(GostRadius.sm))
            .background(if (isActive) activeBg else inactiveBg)
            .border(1.dp, if (isActive) activeFg.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(GostRadius.sm))
            .clickable { onToggle() }
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
    ) {
        Text(
            levelLabel,
            color = if (isActive) activeFg else inactiveFg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

data class LogEntry(
    val timestamp: String,
    val level: String,
    val service: String? = null,
    val message: String,
    val raw: String,
)

data class LogsUiState(
    val entries: List<LogEntry> = emptyList(),
    val filteredEntries: List<LogEntry> = emptyList(),
    val levelFilter: Set<String> = setOf("debug", "info", "warn", "error"),
    val serviceFilter: String? = null,
    val searchQuery: String = "",
    val autoScroll: Boolean = true,
    val sourceAvailable: Boolean = false,
    val availableServices: List<String> = emptyList(),
)

/**
 * Maps GOST / common log level strings to canonical `debug` | `info` | `warn` | `error`.
 * Returns null if the bracket content is not a known level (e.g. `[gateway]`).
 */
private fun normalizeToCanonicalLevel(raw: String): String? {
    val s = raw.trim().lowercase()
    return when (s) {
        "err", "fatal", "panic" -> "error"
        "warning" -> "warn"
        "trace", "verbose" -> "debug"
        "inf", "information" -> "info"
        "debug", "info", "warn", "error" -> s
        else -> null
    }
}

private val bracketLevelPattern = Regex("""\[([^\]]+)\]""")

/**
 * Finds the first `[token]` where [normalizeToCanonicalLevel] succeeds; returns level + line with that bracket removed.
 */
private fun findBracketLevelInLine(line: String): Pair<String, String>? {
    for (match in bracketLevelPattern.findAll(line)) {
        val raw = match.groupValues[1]
        val canon = normalizeToCanonicalLevel(raw) ?: continue
        val remainder =
            buildString {
                append(line.substring(0, match.range.first))
                append(line.substring(match.range.last + 1))
            }.trim()
        return Pair(canon, if (remainder.isNotEmpty()) remainder else line)
    }
    return null
}

class LogsScreenModel(
    private val serviceRegistry: ServiceRegistry = ServiceRegistry.default(),
    private val processManager: ProcessManager = ProcessManager.default(),
) : ScreenModel {
    private val _state = MutableStateFlow(LogsUiState(sourceAvailable = true))
    val state: StateFlow<LogsUiState> = _state.asStateFlow()

    // Limits
    private val maxLines = 1000

    init {
        screenModelScope.launch {
            // Load available services for filter dropdown
            serviceRegistry.services.collect { services ->
                _state.value =
                    _state.value.copy(
                        availableServices = services.map { it.id },
                    )
            }
        }
        screenModelScope.launch {
            processManager.logs.collect { event ->
                val line = parseGostLog(event.text, event.serviceId, event.timestamp)
                val s = _state.value
                val newEntries = (s.entries + line).takeLast(maxLines)
                _state.value =
                    s.copy(
                        entries = newEntries,
                        filteredEntries = applyFilterSync(newEntries, s.levelFilter, s.serviceFilter, s.searchQuery),
                    )
            }
        }
    }

    private fun parseGostLog(
        text: String,
        serviceId: String,
        systemTime: Long,
    ): LogEntry {
        var timestamp = ""
        var level = "info"
        var msg = text

        val parts = text.split(" ", limit = 4)
        val structured =
            parts.size >= 3 && parts[2].startsWith("[") && parts[2].endsWith("]")

        if (structured) {
            timestamp = "${parts[0]} ${parts[1]}"
            val rawLevel = parts[2].removeSurrounding("[", "]")
            level = normalizeToCanonicalLevel(rawLevel) ?: "info"
            msg = if (parts.size > 3) parts[3] else ""
        } else {
            val fallback = findBracketLevelInLine(text)
            if (fallback != null) {
                level = fallback.first
                msg = fallback.second
            }
        }

        if (timestamp.isBlank()) {
            val sdf = java.text.SimpleDateFormat("HH:mm:ss.SSS")
            timestamp = sdf.format(java.util.Date(systemTime))
        }

        return LogEntry(
            timestamp = timestamp,
            level = level,
            service = serviceId,
            message = msg,
            raw = text,
        )
    }

    fun toggleLevel(level: String) {
        val current = _state.value.levelFilter.toMutableSet()
        if (current.contains(level)) current.remove(level) else current.add(level)
        _state.value = _state.value.copy(levelFilter = current)
        applyFilters()
    }

    fun setServiceFilter(service: String?) {
        _state.value = _state.value.copy(serviceFilter = service)
        applyFilters()
    }

    fun setSearch(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }

    fun toggleAutoScroll() {
        _state.value = _state.value.copy(autoScroll = !_state.value.autoScroll)
    }

    fun clear() {
        _state.value = _state.value.copy(entries = emptyList(), filteredEntries = emptyList())
    }

    private fun applyFilters() {
        val s = _state.value
        _state.value = s.copy(filteredEntries = applyFilterSync(s.entries, s.levelFilter, s.serviceFilter, s.searchQuery))
    }

    private fun applyFilterSync(
        entries: List<LogEntry>,
        levelFilter: Set<String>,
        serviceFilter: String?,
        searchQuery: String,
    ): List<LogEntry> =
        entries.filter { entry ->
            levelFilter.contains(entry.level.lowercase()) &&
                (serviceFilter == null || entry.service == serviceFilter) &&
                (searchQuery.isBlank() || entry.raw.contains(searchQuery, ignoreCase = true))
        }
}

class LogsScreen : Screen {
    @Composable
    override fun Content() {
        val model = rememberScreenModel { LogsScreenModel() }
        val state by model.state.collectAsState()
        val listState = rememberLazyListState()
        val sc = GostSemantics.colors

        // ── Auto-scroll effect ──
        LaunchedEffect(state.filteredEntries.size, state.autoScroll) {
            if (state.autoScroll && state.filteredEntries.isNotEmpty()) {
                listState.animateScrollToItem(state.filteredEntries.lastIndex)
            }
        }

        val terminalPalette =
            remember(sc) {
                LogTerminalPalette(
                    background = sc.surfaceApp,
                    onSurface = sc.textPrimary,
                    onSurfaceMuted = sc.textPrimary.copy(alpha = 0.85f),
                    onSurfaceDim = sc.textSecondary,
                )
            }

        ScreenScaffold(
            header = {
                SaaSScreenHeader(
                    superTitle = "OBSERVABILITY",
                    title = "Logs",
                )
            },
        ) {
            // Toolbar
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Level filters
                listOf("DEBUG", "INFO", "WARN", "ERROR").forEach { label ->
                    LogLevelFilterChip(
                        levelLabel = label,
                        isActive = state.levelFilter.contains(label.lowercase()),
                        onToggle = { model.toggleLevel(label.lowercase()) },
                    )
                }

                // Service filter dropdown
                if (state.availableServices.isNotEmpty()) {
                    val allLabel = "All tunnels"
                    val options = remember(state.availableServices) { listOf(allLabel) + state.availableServices }
                    DropdownField(
                        value = state.serviceFilter ?: allLabel,
                        options = options,
                        searchable = options.size >= 10,
                        onSelect = { sel -> model.setServiceFilter(if (sel == allLabel) null else sel) },
                        modifier = Modifier.widthIn(min = 180.dp, max = 260.dp),
                        contentDescription = "Filter by tunnel",
                    )
                }

                // Search
                SaaSSearchBar(
                    query = state.searchQuery,
                    onQueryChange = model::setSearch,
                    placeholder = "Search logs...",
                    modifier = Modifier.weight(1f).widthIn(min = 160.dp, max = 480.dp),
                )

                // Auto-scroll toggle
                SegmentedControl(
                    options = listOf(true, false),
                    selected = state.autoScroll,
                    onSelect = { desired ->
                        if (desired != state.autoScroll) model.toggleAutoScroll()
                    },
                    label = { if (it) "Auto" else "Free" },
                )

                SaaSButton(
                    text = "Clear",
                    onClick = model::clear,
                    type = SaaSButtonType.SECONDARY,
                )
            }
            Spacer(Modifier.height(Spacing.md))

            // Log viewer
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(GostRadius.lg))
                    .background(terminalPalette.background)
                    .border(1.dp, sc.borderSubtle, RoundedCornerShape(GostRadius.lg))
                    .padding(Spacing.sm),
            ) {
                if (!state.sourceAvailable && state.entries.isEmpty()) {
                    Column(
                        Modifier.fillMaxSize().padding(Spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.Default.Terminal,
                            contentDescription = "Logs unavailable",
                            tint = terminalPalette.onSurfaceMuted,
                            modifier = Modifier.size(36.dp),
                        )
                        Spacer(Modifier.height(Spacing.md))
                        Text("No logs yet", color = terminalPalette.onSurfaceMuted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(Spacing.xs))
                        Text(
                            "Logs will appear here when local GOST tunnels emit output.",
                            color = terminalPalette.onSurfaceDim,
                            fontSize = 12.sp,
                        )
                    }
                } else if (state.filteredEntries.isEmpty() && state.entries.isNotEmpty()) {
                    Column(
                        Modifier.fillMaxSize().padding(Spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            "No logs match current filters",
                            color = terminalPalette.onSurfaceMuted,
                            fontSize = 13.sp,
                        )
                    }
                } else {
                    LazyColumn(state = listState) {
                        items(state.filteredEntries) { entry ->
                            LogLine(entry, terminalPalette)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogLine(
    entry: LogEntry,
    palette: LogTerminalPalette,
) {
    val sc = GostSemantics.colors
    val levelColor =
        when (entry.level.lowercase()) {
            "error" -> sc.statusError
            "warn" -> sc.statusWarning
            "info" -> sc.statusSuccess
            "debug" -> palette.onSurfaceDim
            else -> palette.onSurfaceMuted
        }
    val bgColor =
        when (entry.level.lowercase()) {
            "error" -> sc.statusError.copy(alpha = 0.08f)
            "warn" -> sc.statusWarning.copy(alpha = 0.08f)
            else -> Color.Transparent
        }

    Row(
        Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            entry.timestamp,
            color = palette.onSurfaceDim,
            style = GostTextStyles.logLine,
            fontFamily = MonoFontFamily,
            modifier = Modifier.width(90.dp),
        )
        Spacer(Modifier.width(Spacing.xs))
        Box(Modifier.width(44.dp)) {
            Text(
                entry.level.uppercase(),
                color = levelColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MonoFontFamily,
            )
        }
        if (entry.service != null) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(Spacing.xs))
                    .background(Teal400.copy(alpha = 0.18f))
                    .padding(horizontal = Spacing.xs),
            ) {
                Text(entry.service, color = Teal300, fontSize = 10.sp, fontFamily = MonoFontFamily)
            }
            Spacer(Modifier.width(Spacing.xs))
        }
        Text(entry.message, color = palette.onSurface, style = GostTextStyles.logLine, fontFamily = MonoFontFamily)
    }
}
