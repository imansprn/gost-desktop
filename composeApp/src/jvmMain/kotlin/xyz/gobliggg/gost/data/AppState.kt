package xyz.gobliggg.gost.data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import xyz.gobliggg.gost.model.AppSettings
import xyz.gobliggg.gost.model.GostRuntimeConfig
import java.io.File

/**
 * Global application state singleton.
 * Holds the local config and settings.
 */
enum class EngineTransition {
    STARTING,
    STOPPING,
}

object AppState {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isRuntimeValid = MutableStateFlow(false)
    val isRuntimeValid: StateFlow<Boolean> = _isRuntimeValid.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _isEngineRunning = MutableStateFlow(false)
    val isEngineRunning: StateFlow<Boolean> = _isEngineRunning.asStateFlow()

    private val _engineTransition = MutableStateFlow<EngineTransition?>(null)
    val engineTransition: StateFlow<EngineTransition?> = _engineTransition.asStateFlow()

    private val _isRuntimeReconfiguring = MutableStateFlow(false)
    val isRuntimeReconfiguring: StateFlow<Boolean> = _isRuntimeReconfiguring.asStateFlow()

    private val _persistenceIssue = MutableStateFlow<String?>(null)
    val persistenceIssue: StateFlow<String?> = _persistenceIssue.asStateFlow()

    private var pendingShellRoute: String? = null
    private var engineWasRunningBeforeReconfigure = false

    private lateinit var configRepo: LocalConfigRepository
    private var localConfig = LocalConfig()

    suspend fun initialize(repo: LocalConfigRepository = LocalConfigRepository()) = withContext(Dispatchers.IO) {
        configRepo = repo
        localConfig = configRepo.load()
        _persistenceIssue.value = configRepo.lastLoadWarning
        val loaded = localConfig.settings
        _settings.value = loaded

        checkRuntimeValid(loaded.gostRuntime)

        // Ensure other systems boot up
        ServiceRegistry.default().initialize()
        ProcessManager.default().initialize()

        _isEngineRunning.value = _isRuntimeValid.value && loaded.gostRuntime.autoStart
        if (_isEngineRunning.value) {
            ServiceRegistry.default().services.value
                .filter { it.desiredRunning }
                .forEach { ProcessManager.default().startService(it.id, recordIntent = false) }
        }

        _isInitialized.value = true
    }

    private fun checkRuntimeValid(runtime: GostRuntimeConfig) {
        val file = File(runtime.binaryPath)
        _isRuntimeValid.value = file.exists() && file.isFile && file.canExecute()
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val updated = transform(_settings.value)
        val configToSave = localConfig.copy(settings = updated)
        _settings.value = updated
        localConfig = configToSave

        scope.launch {
            try {
                configRepo.save(configToSave)
            } catch (e: Exception) {
                reportPersistenceIssue(
                    "Failed to save settings: ${e.message ?: "unknown filesystem error"}",
                )
            }
        }

        checkRuntimeValid(updated.gostRuntime)
    }

    fun reportPersistenceIssue(message: String?) {
        _persistenceIssue.value = message
    }

    fun setPendingShellRoute(route: String?) {
        val finalRoute = if (route?.startsWith("api-error") == true) null else route
        pendingShellRoute = finalRoute
    }

    fun takePendingShellRoute(): String? {
        val r = pendingShellRoute
        pendingShellRoute = null
        return r
    }

    fun startEngine() {
        if (!_isRuntimeValid.value || _isEngineRunning.value || _engineTransition.value != null) return
        _engineTransition.value = EngineTransition.STARTING
        _isEngineRunning.value = true
        scope.launch {
            try {
                ServiceRegistry.default().services.value
                    .filter { it.desiredRunning }
                    .forEach { ProcessManager.default().startService(it.id, recordIntent = false) }
            } finally {
                _engineTransition.value = null
            }
        }
    }

    fun stopEngine() {
        if (!_isEngineRunning.value || _engineTransition.value != null) return
        _engineTransition.value = EngineTransition.STOPPING
        // Gate new starts immediately while existing/starting processes are being drained.
        _isEngineRunning.value = false
        scope.launch {
            try {
                ProcessManager.default().stopAll(preserveIntent = true)
            } finally {
                _engineTransition.value = null
            }
        }
    }

    fun toggleEngine() {
        if (_engineTransition.value != null) return
        if (_isEngineRunning.value) stopEngine() else startEngine()
    }

    private fun runAfterEngineSettles(block: () -> Unit) {
        scope.launch {
            while (_engineTransition.value != null) {
                delay(25)
            }
            block()
        }
    }

    fun beginRuntimeReconfiguration() {
        if (_engineTransition.value != null) return
        engineWasRunningBeforeReconfigure = _isEngineRunning.value
        stopEngine()
        _isRuntimeReconfiguring.value = true
        _isRuntimeValid.value = false
    }

    fun cancelRuntimeReconfiguration() {
        if (!_isRuntimeReconfiguring.value) return
        _isRuntimeReconfiguring.value = false
        checkRuntimeValid(_settings.value.gostRuntime)
        val shouldRestart = engineWasRunningBeforeReconfigure
        engineWasRunningBeforeReconfigure = false
        if (shouldRestart && _isRuntimeValid.value) {
            runAfterEngineSettles { startEngine() }
        }
    }

    fun finishRuntimeReconfiguration() {
        _isRuntimeReconfiguring.value = false
        checkRuntimeValid(_settings.value.gostRuntime)
        engineWasRunningBeforeReconfigure = false
        if (_isRuntimeValid.value && _settings.value.gostRuntime.autoStart) {
            runAfterEngineSettles { startEngine() }
        }
    }

    // Legacy alias used by the shell.
    fun disconnect() {
        stopEngine()
    }

    // ── Legacy profile stubs (local-only mode has no connection profiles) ──

    @Serializable
    data class ConnectionProfile(
        val id: String,
        val name: String,
        val baseUrl: String = "",
    )

    fun getProfiles(): List<ConnectionProfile> = emptyList()

    fun deleteProfile(id: String) { /* no-op in local mode */ }

    val isDarkTheme: Boolean = true
}
