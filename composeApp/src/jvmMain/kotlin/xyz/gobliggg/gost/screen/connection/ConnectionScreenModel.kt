package xyz.gobliggg.gost.screen.connection

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.gobliggg.gost.data.AppState
import xyz.gobliggg.gost.model.GostRuntimeConfig
import java.io.File

data class RuntimeUiState(
    val binaryPath: String = "",
    val workingDirectory: String = "",
    val autoStart: Boolean = false,
    val pathError: String? = null,
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
