package xyz.gobliggg.gost.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import xyz.gobliggg.gost.model.AppSettings
import xyz.gobliggg.gost.model.ThemeMode
import xyz.gobliggg.gost.model.GostRuntimeConfig
import java.io.File

/**
 * Global application state singleton.
 * Holds the local config and settings.
 */
object AppState {
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isRuntimeValid = MutableStateFlow(false)
    val isRuntimeValid: StateFlow<Boolean> = _isRuntimeValid.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()


    private var pendingShellRoute: String? = null

    private lateinit var configRepo: LocalConfigRepository
    private var localConfig = LocalConfig()

    fun initialize(repo: LocalConfigRepository = LocalConfigRepository()) {
        configRepo = repo
        localConfig = configRepo.load()
        
        _settings.value = localConfig.settings
        
        checkRuntimeValid(localConfig.settings.gostRuntime)
        
        // Ensure other systems boot up
        ServiceRegistry.initialize()
        ProcessManager.initialize()

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
        configRepo.save(localConfig)
        
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

    // Keeping these to not break App.kt aggressively before we remove them from App.kt
    fun disconnect() {
        updateSettings {
            it.copy(gostRuntime = it.gostRuntime.copy(binaryPath = ""))
        }
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

    val isDarkTheme: Boolean
        get() = when (_settings.value.theme) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> true
        }
}
