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

    private var pendingShellRoute: String? = null

    private lateinit var configRepo: LocalConfigRepository
    private var localConfig = LocalConfig()

    suspend fun initialize(repo: LocalConfigRepository = LocalConfigRepository()) = withContext(Dispatchers.IO) {
        configRepo = repo
        localConfig = configRepo.load()
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
        _settings.value = updated
        localConfig = localConfig.copy(settings = updated)
        
        scope.launch {
            configRepo.save(localConfig)
        }

        checkRuntimeValid(updated.gostRuntime)
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
        if (!_isRuntimeValid.value || _isEngineRunning.value) return
        _isEngineRunning.value = true
        ServiceRegistry.default().services.value
            .filter { it.desiredRunning }
            .forEach { ProcessManager.default().startService(it.id, recordIntent = false) }
    }

    fun stopEngine() {
        if (!_isEngineRunning.value) return
        ProcessManager.default().stopAll(preserveIntent = true)
        _isEngineRunning.value = false
    }

    fun toggleEngine() {
        if (_isEngineRunning.value) stopEngine() else startEngine()
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
