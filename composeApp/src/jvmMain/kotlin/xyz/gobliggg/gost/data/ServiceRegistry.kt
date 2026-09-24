package xyz.gobliggg.gost.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Serializable
enum class ServiceStatus {
    IDLE,
    STARTING,
    RUNNING,
    STOPPING,
    ERROR,
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
    val errorMessage: String? = null,
    val desiredRunning: Boolean = false,
)

class ServiceRegistry(
    dataDir: File = File(System.getProperty("user.home"), ".gost-manager/data"),
) {
    private val servicesFile = File(dataDir, "services.json")

    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    private val _services = MutableStateFlow<List<ServiceEntity>>(emptyList())
    val services: StateFlow<List<ServiceEntity>> = _services.asStateFlow()

    companion object {
        private val defaultInstance by lazy { ServiceRegistry() }

        /** Convenience accessor for non-DI callers where injection isn't practical. */
        fun default(): ServiceRegistry = defaultInstance
    }

    fun initialize() {
        if (!servicesFile.parentFile.exists()) {
            servicesFile.parentFile.mkdirs()
        }
        servicesFile.parentFile.restrictToOwner()
        if (servicesFile.exists()) servicesFile.restrictToOwner()
        load()
    }

    private fun load() {
        try {
            if (servicesFile.exists()) {
                val list = json.decodeFromString<List<ServiceEntity>>(servicesFile.readText())
                // Reset all states and errors for a clean start, and migrate addr if missing
                _services.value =
                    list.map { svc ->
                        var currentAddr = svc.addr
                        if (currentAddr.isBlank()) {
                            try {
                                val configFile = File(svc.configPath)
                                if (configFile.exists()) {
                                    val root = Json.parseToJsonElement(configFile.readText()).jsonObject
                                    val svcs = root["services"]?.jsonArray
                                    currentAddr = svcs
                                        ?.firstOrNull()
                                        ?.jsonObject
                                        ?.get("addr")
                                        ?.jsonPrimitive
                                        ?.content ?: ""
                                }
                            } catch (e: Exception) {
                                println("migration failed for ${svc.id}: ${e.message}")
                            }
                        }
                        svc.copy(status = ServiceStatus.IDLE, pid = null, errorMessage = null, addr = currentAddr)
                    }
            }
        } catch (e: Exception) {
            val backup =
                File(
                    servicesFile.parentFile,
                    "services.corrupt-${System.currentTimeMillis()}.json",
                )
            val backedUp = runCatching { servicesFile.renameTo(backup) }.getOrDefault(false)
            _services.value = emptyList()
            AppState.reportPersistenceIssue(
                if (backedUp) {
                    "Tunnel registry was corrupt and moved to ${backup.name}. Existing config files were left untouched."
                } else {
                    "Tunnel registry is corrupt and could not be backed up: ${e.message}"
                },
            )
        }
    }

    private fun save(next: List<ServiceEntity>): Boolean {
        if (!servicesFile.parentFile.exists() && !servicesFile.parentFile.mkdirs()) {
            AppState.reportPersistenceIssue(
                "Failed to create tunnel registry directory: ${servicesFile.parentFile.absolutePath}",
            )
            return false
        }

        var temp: File? = null
        return try {
            temp = File.createTempFile("services", ".json.tmp", servicesFile.parentFile)
            temp.writeText(json.encodeToString(next))
            temp.restrictToOwner()
            try {
                Files.move(
                    temp.toPath(),
                    servicesFile.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: Exception) {
                Files.move(
                    temp.toPath(),
                    servicesFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                )
            }
            servicesFile.restrictToOwner()
            true
        } catch (e: Exception) {
            AppState.reportPersistenceIssue(
                "Failed to save tunnel registry: ${e.message ?: "unknown filesystem error"}",
            )
            false
        } finally {
            temp?.takeIf { it.exists() }?.delete()
        }
    }

    fun addOrUpdateService(service: ServiceEntity): Boolean {
        val current = _services.value.toMutableList()
        val index = current.indexOfFirst { it.id == service.id }
        if (index >= 0) {
            current[index] = service
        } else {
            current.add(service)
        }
        if (!save(current)) return false
        _services.value = current
        return true
    }

    fun replaceService(
        oldId: String?,
        service: ServiceEntity,
    ): Boolean {
        val current =
            _services.value
                .filterNot { existing ->
                    existing.id == service.id ||
                        (oldId != null && existing.id == oldId)
                }
                .toMutableList()
        current.add(service)
        if (!save(current)) return false
        _services.value = current
        return true
    }

    fun updateServiceStatus(
        id: String,
        status: ServiceStatus,
        pid: Long? = null,
        errorMessage: String? = null,
    ) {
        val current = _services.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            val existing = current[index]
            current[index] =
                existing.copy(
                    status = status,
                    pid = pid,
                    errorMessage = errorMessage,
                )
            if (save(current)) {
                _services.value = current
            }
        }
    }

    fun removeService(id: String): Boolean {
        val current = _services.value.filter { it.id != id }
        if (!save(current)) return false
        _services.value = current
        return true
    }

    fun updateDesiredRunning(id: String, desiredRunning: Boolean) {
        val current = _services.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            current[index] = current[index].copy(desiredRunning = desiredRunning)
            if (save(current)) {
                _services.value = current
            }
        }
    }

    fun getService(id: String): ServiceEntity? = _services.value.find { it.id == id }
}
