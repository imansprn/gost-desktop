package xyz.gobliggg.gost.screen.services

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.gobliggg.gost.data.ConfigBuilder
import xyz.gobliggg.gost.data.ProcessManager
import xyz.gobliggg.gost.data.ServiceEntity
import xyz.gobliggg.gost.data.ServiceRegistry
import xyz.gobliggg.gost.ui.ShellFeedback

data class ServicesUiState(
    val services: List<ServiceEntity> = emptyList(),
    val filteredServices: List<ServiceEntity> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
)

class ServicesScreenModel(
    private val serviceRegistry: ServiceRegistry = ServiceRegistry.default(),
    private val configBuilder: ConfigBuilder = ConfigBuilder.default(),
    private val processManager: ProcessManager = ProcessManager.default(),
) : ScreenModel {
    private val _state = MutableStateFlow(ServicesUiState())
    val state: StateFlow<ServicesUiState> = _state.asStateFlow()

    init {
        screenModelScope.launch {
            serviceRegistry.services.collect { list ->
                _state.value =
                    _state.value.copy(
                        services = list,
                        filteredServices = filterServices(list, _state.value.searchQuery),
                    )
            }
        }
    }

    fun refresh() {
        // Flow updates automatically, but we can do a dummy refresh
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun search(query: String) {
        val currentServices = _state.value.services
        _state.value =
            _state.value.copy(
                searchQuery = query,
                filteredServices = filterServices(currentServices, query),
            )
    }

    fun startService(id: String) {
        processManager.startService(id)
    }

    fun stopService(id: String) {
        processManager.stopService(id)
    }

    fun restartService(id: String) {
        processManager.restartService(id)
    }

    fun deleteService(id: String) {
        stopService(id)
        configBuilder.deleteServiceConfig(id)
        serviceRegistry.removeService(id)
        screenModelScope.launch {
            ShellFeedback.showSnackbar("Tunnel deleted")
        }
    }

    private fun filterServices(
        services: List<ServiceEntity>,
        query: String,
    ): List<ServiceEntity> {
        if (query.isBlank()) return services
        val q = query.lowercase()
        return services.filter {
            it.name.lowercase().contains(q) ||
                it.id.lowercase().contains(q) ||
                it.errorMessage?.lowercase()?.contains(q) == true
        }
    }
}
