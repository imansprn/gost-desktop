package xyz.gobliggg.gost.screen.connection

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.gobliggg.gost.data.AppState
import xyz.gobliggg.gost.model.GostRuntimeConfig
import java.io.File

@Immutable
data class RuntimeUiState(
    val binaryPath: String = "",
    val workingDirectory: String = "",
    val autoStart: Boolean = false,
    val pathError: String? = null,
    val suggestedBinaryPath: String? = null,
)

class ConnectionScreenModel : ScreenModel {
    private val _state = MutableStateFlow(RuntimeUiState())
    val state: StateFlow<RuntimeUiState> = _state.asStateFlow()

    init {
        val runtime = AppState.settings.value.gostRuntime
        _state.value =
            RuntimeUiState(
                binaryPath = runtime.binaryPath,
                workingDirectory = runtime.workingDirectory,
                autoStart = runtime.autoStart,
            )

        screenModelScope.launch {
            val suggested = withContext(Dispatchers.IO) { detectGostBinary() }
            if (runtime.binaryPath.isBlank() && suggested != null) {
                _state.value = _state.value.copy(
                    binaryPath = suggested,
                    suggestedBinaryPath = suggested
                )
            } else {
                _state.value = _state.value.copy(suggestedBinaryPath = suggested)
            }
        }
    }

    private fun detectGostBinary(): String? {
        val commonPaths = listOf(
            "/usr/local/bin/gost",
            "/opt/homebrew/bin/gost",
            "/usr/bin/gost",
            System.getProperty("user.home") + "/bin/gost"
        )
        val found = commonPaths.firstOrNull { File(it).exists() && File(it).canExecute() }
        if (found != null) return found

        // Try 'which' as a fallback
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "gost"))
            val output = process.inputStream.bufferedReader().readText().trim()
            if (output.isNotBlank() && File(output).exists()) output else null
        } catch (e: Exception) {
            null
        }
    }

    fun updateBinaryPath(path: String) {
        val error =
            if (path.isBlank()) {
                "Path is required"
            } else {
                val file = File(path)
                if (!file.exists() || !file.canExecute()) {
                    "File does not exist or is not executable"
                } else {
                    null
                }
            }
        _state.value = _state.value.copy(binaryPath = path, pathError = error)
    }

    fun updateWorkingDirectory(dir: String) {
        _state.value = _state.value.copy(workingDirectory = dir)
    }

    fun updateAutoStart(autoStart: Boolean) {
        _state.value = _state.value.copy(autoStart = autoStart)
    }

    fun saveAndConnect(onConnected: () -> Unit) {
        val s = _state.value
        updateBinaryPath(s.binaryPath) // Validate again
        if (_state.value.pathError != null) return

        AppState.updateSettings {
            it.copy(
                gostRuntime =
                    GostRuntimeConfig(
                        binaryPath = s.binaryPath,
                        workingDirectory = s.workingDirectory,
                        autoStart = s.autoStart,
                    ),
            )
        }

        if (AppState.isRuntimeValid.value) {
            onConnected()
        }
    }
}
