package xyz.gobliggg.gost.screen.services

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.gobliggg.gost.data.ProcessManager
import xyz.gobliggg.gost.data.ServiceEntity
import xyz.gobliggg.gost.data.ServiceRegistry
import xyz.gobliggg.gost.ui.ShellFeedback
import xyz.gobliggg.gost.data.ConfigBuilder

data class ServicesUiState(
    val services: List<ServiceEntity> = emptyList(),
    val filteredServices: List<ServiceEntity> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
)

class ServicesScreenModel : ScreenModel {
    private val _state = MutableStateFlow(ServicesUiState())
    val state: StateFlow<ServicesUiState> = _state.asStateFlow()

    init {
        screenModelScope.launch {
            ServiceRegistry.services.collect { list ->
                _state.value = _state.value.copy(
                    services = list,
                    filteredServices = filterServices(list, _state.value.searchQuery)
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
        _state.value = _state.value.copy(
            searchQuery = query,
            filteredServices = filterServices(currentServices, query)
        )
    }
    
    fun startService(id: String) {
        ProcessManager.startService(id)
    }
    
    fun stopService(id: String) {
        ProcessManager.stopService(id)
    }
    
    fun restartService(id: String) {
        ProcessManager.restartService(id)
    }

    fun deleteService(id: String) {
        stopService(id)
        ConfigBuilder.deleteServiceConfig(id)
        ServiceRegistry.removeService(id)
        screenModelScope.launch {
            ShellFeedback.showSnackbar("Tunnel deleted")
        }
    }

    private fun filterServices(services: List<ServiceEntity>, query: String): List<ServiceEntity> {
        if (query.isBlank()) return services
        val q = query.lowercase()
        return services.filter {
            it.name.lowercase().contains(q) ||
            it.id.lowercase().contains(q) ||
            it.errorMessage?.lowercase()?.contains(q) == true
        }
    }
}
