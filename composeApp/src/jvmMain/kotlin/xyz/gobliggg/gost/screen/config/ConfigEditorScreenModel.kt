package xyz.gobliggg.gost.screen.config

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.gobliggg.gost.data.ConfigBuilder
import xyz.gobliggg.gost.data.ServiceRegistry
import xyz.gobliggg.gost.ui.ShellFeedback
import java.io.File

data class ConfigEditorUiState(
    val content: String = "",
    val format: String = "json",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isDirty: Boolean = false,
    val selectedServiceId: String? = null,
    val serviceIds: List<String> = emptyList(),
)

class ConfigEditorScreenModel(
    private val serviceRegistry: ServiceRegistry = ServiceRegistry.default(),
    private val configBuilder: ConfigBuilder = ConfigBuilder.default(),
) : ScreenModel {
    private val _state = MutableStateFlow(ConfigEditorUiState())
    val state: StateFlow<ConfigEditorUiState> = _state.asStateFlow()

    init {
        loadServiceList()
    }

    private fun loadServiceList() {
        val services = serviceRegistry.services.value
        val ids = services.map { it.id }
        _state.value =
            _state.value.copy(
                serviceIds = ids,
                isLoading = false,
            )
        if (ids.isNotEmpty() && _state.value.selectedServiceId == null) {
            selectService(ids.first())
        } else {
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun selectService(serviceId: String) {
        val content = configBuilder.readServiceConfig(serviceId) ?: ""
        _state.value =
            _state.value.copy(
                selectedServiceId = serviceId,
                content = content,
                isDirty = false,
                errorMessage = null,
                successMessage = null,
                isLoading = false,
            )
    }

    fun setFormat(format: String) {
        if (_state.value.format == format) return
        _state.value = _state.value.copy(format = format)
    }

    fun updateContent(newContent: String) {
        _state.value = _state.value.copy(content = newContent, isDirty = true, successMessage = null)
    }

    fun save() {
        val s = _state.value
        val serviceId = s.selectedServiceId ?: return
        _state.value = s.copy(isSaving = true, errorMessage = null, successMessage = null)

        try {
            configBuilder.buildServiceConfig(serviceId, s.content)
            _state.value =
                _state.value.copy(
                    isSaving = false,
                    isDirty = false,
                    successMessage = "Configuration saved to disk.",
                )
            ShellFeedback.showSnackbar("Configuration saved")
        } catch (e: Exception) {
            _state.value =
                _state.value.copy(
                    isSaving = false,
                    errorMessage = "Save failed: ${e.message}",
                )
        }
    }

    /** Reload current config from disk (discards local edits). */
    fun reloadFromDisk() {
        val serviceId = _state.value.selectedServiceId ?: return
        selectService(serviceId)
        ShellFeedback.showSnackbar("Reloaded from disk")
    }

    fun exportToLocalFile(path: String) {
        try {
            File(path).writeText(_state.value.content)
            _state.value =
                _state.value.copy(
                    successMessage = "Exported to: $path",
                    errorMessage = null,
                )
            ShellFeedback.showSnackbar("Exported configuration to file")
        } catch (e: Exception) {
            _state.value = _state.value.copy(errorMessage = e.message ?: "Export failed")
        }
    }
}
