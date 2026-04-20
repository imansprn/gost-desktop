package xyz.gobliggg.gost.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.File

@Serializable
enum class ServiceStatus {
    IDLE, RUNNING, ERROR
}

@Serializable
data class ServiceEntity(
    val id: String,
    val name: String,
    val addr: String = "",
    val configPath: String,
    val port: Int? = null,
    val pid: Long? = null,
    val status: ServiceStatus = ServiceStatus.IDLE,
    val errorMessage: String? = null
)

object ServiceRegistry {
    private val dataDir = File(System.getProperty("user.home"), ".gost-manager/data")
    private val servicesFile = File(dataDir, "services.json")

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _services = MutableStateFlow<List<ServiceEntity>>(emptyList())
    val services: StateFlow<List<ServiceEntity>> = _services.asStateFlow()

    fun initialize() {
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
        load()
    }

    private fun load() {
        try {
            if (servicesFile.exists()) {
                val list = json.decodeFromString<List<ServiceEntity>>(servicesFile.readText())
                // Reset all states and errors for a clean start, and migrate addr if missing
                _services.value = list.map { svc ->
                    var currentAddr = svc.addr
                    if (currentAddr.isBlank()) {
                        try {
                            val configFile = File(svc.configPath)
                            if (configFile.exists()) {
                                val root = Json.parseToJsonElement(configFile.readText()).jsonObject
                                val svcs = root["services"]?.jsonArray
                                currentAddr = svcs?.firstOrNull()?.jsonObject?.get("addr")?.jsonPrimitive?.content ?: ""
                            }
                        } catch (e: Exception) {
                            println("migration failed for ${svc.id}: ${e.message}")
                        }
                    }
                    svc.copy(status = ServiceStatus.IDLE, pid = null, errorMessage = null, addr = currentAddr)
                }
            }
        } catch (e: Exception) {
            println("Failed to load services: ${e.message}")
        }
    }

    private fun save() {
        try {
            servicesFile.writeText(json.encodeToString(_services.value))
        } catch (e: Exception) {
            println("Failed to save services: ${e.message}")
        }
    }

    fun addOrUpdateService(service: ServiceEntity) {
        val current = _services.value.toMutableList()
        val index = current.indexOfFirst { it.id == service.id }
        if (index >= 0) {
            current[index] = service
        } else {
            current.add(service)
        }
        _services.value = current
        save()
    }

    fun updateServiceStatus(id: String, status: ServiceStatus, pid: Long? = null, errorMessage: String? = null) {
        val current = _services.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(
                status = status,
                pid = pid,
                errorMessage = errorMessage
            )
            _services.value = current
            save()
        }
    }

    fun removeService(id: String) {
        val current = _services.value.filter { it.id != id }
        _services.value = current
        save()
    }
    
    fun getService(id: String): ServiceEntity? {
        return _services.value.find { it.id == id }
    }
}
