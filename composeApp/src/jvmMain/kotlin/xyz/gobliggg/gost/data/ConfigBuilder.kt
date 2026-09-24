package xyz.gobliggg.gost.data

import kotlinx.serialization.json.*
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object TemplateTypes {
    const val CHAINS = "chains"
    const val AUTHERS = "authers"
    const val BYPASSES = "bypasses"
    const val ADMISSIONS = "admissions"
    const val RESOLVERS = "resolvers"
    const val HOSTS = "hosts"
    const val LIMITERS = "limiters"
}

class ConfigBuilder(
    baseDir: File = File(System.getProperty("user.home"), ".gost-manager"),
) {
    private val configsDir = File(baseDir, "configs")
    private val templatesDir = File(baseDir, "templates")

    init {
        if (!configsDir.exists()) configsDir.mkdirs()
        if (!templatesDir.exists()) templatesDir.mkdirs()
        configsDir.restrictTreeToOwner()
        templatesDir.restrictTreeToOwner()
    }

    companion object {
        private val defaultInstance by lazy { ConfigBuilder() }

        /** Convenience accessor for non-DI callers (Compose screens). */
        fun default(): ConfigBuilder = defaultInstance
    }

    /**
     * Validates a name used for file operations.
     * Only allows alphanumeric, underscore, and hyphen characters.
     * This prevents path traversal attacks (e.g., "../../etc/passwd").
     */
    private fun validateFileName(name: String) {
        if (!name.matches(Regex("^[a-zA-Z0-9_-]+$"))) {
            throw IllegalArgumentException("Invalid name: '$name'. Only alphanumeric, underscore, and hyphen are allowed.")
        }
    }

    private fun canonicalTemplateType(type: String): String =
        when (type) {
            "bypass" -> TemplateTypes.BYPASSES
            "admission" -> TemplateTypes.ADMISSIONS
            else -> type
        }

    private fun templateTypeCandidates(type: String): List<String> =
        when (canonicalTemplateType(type)) {
            TemplateTypes.BYPASSES -> listOf(TemplateTypes.BYPASSES, "bypass")
            TemplateTypes.ADMISSIONS -> listOf(TemplateTypes.ADMISSIONS, "admission")
            else -> listOf(canonicalTemplateType(type))
        }

    private fun atomicWrite(file: File, content: String) {
        file.parentFile?.mkdirs()
        val temp = File.createTempFile(file.name, ".tmp", file.parentFile)
        try {
            temp.writeText(content)
            temp.restrictToOwner()
            try {
                Files.move(
                    temp.toPath(),
                    file.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: Exception) {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            file.restrictToOwner()
        } finally {
            if (temp.exists()) temp.delete()
        }
    }

    fun buildServiceConfig(
        serviceId: String,
        jsonContent: String,
    ): String {
        validateFileName(serviceId)
        val element = Json.parseToJsonElement(jsonContent)
        val format = Json { prettyPrint = true }
        val file = File(configsDir, "$serviceId.json")
        atomicWrite(file, format.encodeToString(element))
        return file.absolutePath
    }

    fun validateRawServiceConfig(
        jsonContent: String,
        expectedServiceId: String? = null,
    ): JsonObject {
        val root =
            Json.parseToJsonElement(jsonContent) as? JsonObject
                ?: throw IllegalArgumentException("Service config must be a JSON object")

        val services =
            root["services"] as? JsonArray
                ?: throw IllegalArgumentException("Service config must contain a 'services' array")
        if (services.size != 1) {
            throw IllegalArgumentException("A tunnel config must contain exactly one service")
        }

        services.forEachIndexed { index, element ->
            val service =
                element as? JsonObject
                    ?: throw IllegalArgumentException("Service at index $index must be a JSON object")
            fun requiredString(key: String): String =
                service[key]
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?.takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException("Service at index $index requires non-empty '$key'")

            val serviceName = requiredString("name")
            if (expectedServiceId != null && serviceName != expectedServiceId) {
                throw IllegalArgumentException(
                    "Service name '$serviceName' must match tunnel '$expectedServiceId'. Rename tunnels from the Tunnels editor.",
                )
            }

            val handler =
                service["handler"] as? JsonObject
                    ?: throw IllegalArgumentException("Service at index $index requires a 'handler' object")
            val handlerType = handler["type"]?.jsonPrimitive?.contentOrNull
            if (handlerType.isNullOrBlank()) {
                throw IllegalArgumentException("Service at index $index requires non-empty 'handler.type'")
            }

            val listener =
                service["listener"] as? JsonObject
                    ?: throw IllegalArgumentException("Service at index $index requires a 'listener' object")
            val listenerType = listener["type"]?.jsonPrimitive?.contentOrNull
            if (listenerType.isNullOrBlank()) {
                throw IllegalArgumentException("Service at index $index requires non-empty 'listener.type'")
            }
        }

        return root
    }

    fun deleteServiceConfig(serviceId: String) {
        validateFileName(serviceId)
        val file = File(configsDir, "$serviceId.json")
        if (file.exists() && !file.delete()) {
            throw IllegalStateException("Failed to delete service config '$serviceId'")
        }
    }

    fun readServiceConfig(serviceId: String): String? {
        validateFileName(serviceId)
        val file = File(configsDir, "$serviceId.json")
        return if (file.exists()) file.readText() else null
    }

    fun listTemplates(type: String): List<String> =
        templateTypeCandidates(type)
            .flatMap { candidate ->
                val dir = File(templatesDir, candidate)
                dir.listFiles { f -> f.isFile && f.name.endsWith(".json") }
                    ?.map { it.nameWithoutExtension }
                    ?: emptyList()
            }
            .distinct()
            .sortedBy { it.lowercase() }

    fun readTemplate(
        type: String,
        name: String,
    ): String? {
        validateFileName(name)
        templateTypeCandidates(type).forEach { candidate ->
            val file = File(templatesDir, "$candidate/$name.json")
            if (file.exists()) return file.readText()
        }
        return null
    }

    fun saveTemplate(
        type: String,
        name: String,
        content: String,
    ) {
        validateFileName(name)
        val parsed = Json.parseToJsonElement(content)
        val pretty = Json { prettyPrint = true }.encodeToString(parsed)
        val canonical = canonicalTemplateType(type)
        val file = File(templatesDir, "$canonical/$name.json")
        atomicWrite(file, pretty)

        templateTypeCandidates(type)
            .filter { it != canonical }
            .forEach { legacy ->
                File(templatesDir, "$legacy/$name.json").takeIf { it.exists() }?.delete()
            }
    }

    fun deleteTemplate(
        type: String,
        name: String,
    ) {
        validateFileName(name)
        templateTypeCandidates(type).forEach { candidate ->
            val file = File(templatesDir, "$candidate/$name.json")
            if (file.exists() && !file.delete()) {
                throw IllegalStateException("Failed to delete template '$name'")
            }
        }
    }

    fun isValidName(name: String): Boolean =
        name.matches(Regex("^[a-zA-Z0-9_-]+$"))

    fun templateExists(type: String, name: String): Boolean =
        readTemplate(type, name) != null

    fun findDependentServices(type: String, name: String): List<String> {
        val canonical = canonicalTemplateType(type)
        if (!configsDir.exists()) return emptyList()

        return configsDir
            .listFiles { file -> file.isFile && file.extension.equals("json", ignoreCase = true) }
            ?.mapNotNull { file ->
                runCatching {
                    val root = Json.parseToJsonElement(file.readText()).jsonObject
                    val service =
                        root["services"]
                            ?.jsonArray
                            ?.firstOrNull()
                            ?.jsonObject
                            ?: return@runCatching null
                    val handler = service["handler"]?.jsonObject
                    val matches =
                        when (canonical) {
                            TemplateTypes.CHAINS ->
                                handler?.get("chain")?.jsonPrimitive?.contentOrNull == name
                            TemplateTypes.AUTHERS ->
                                handler?.get("auther")?.jsonPrimitive?.contentOrNull == name
                            TemplateTypes.BYPASSES ->
                                service["bypass"]?.jsonPrimitive?.contentOrNull == name
                            TemplateTypes.ADMISSIONS ->
                                service["admission"]?.jsonPrimitive?.contentOrNull == name
                            TemplateTypes.LIMITERS ->
                                service["limiter"]?.jsonPrimitive?.contentOrNull == name
                            else -> false
                        }
                    if (matches) {
                        service["name"]?.jsonPrimitive?.contentOrNull ?: file.nameWithoutExtension
                    } else {
                        null
                    }
                }.getOrNull()
            }
            ?.distinct()
            ?.sorted()
            ?: emptyList()
    }

    fun refreshTemplateInDependentServiceConfigs(type: String, name: String): List<String> {
        val canonical = canonicalTemplateType(type)
        val arrayName =
            when (canonical) {
                TemplateTypes.CHAINS -> "chains"
                TemplateTypes.AUTHERS -> "authers"
                TemplateTypes.BYPASSES -> "bypasses"
                TemplateTypes.ADMISSIONS -> "admissions"
                TemplateTypes.LIMITERS -> "limiters"
                else -> return emptyList()
            }

        val templateContent = readTemplate(canonical, name) ?: return emptyList()
        val templateElement = Json.parseToJsonElement(templateContent)
        val replacements =
            if (templateElement is JsonArray) templateElement.toList() else listOf(templateElement)

        val dependents = findDependentServices(canonical, name)
        dependents.forEach { serviceId ->
            val file = File(configsDir, "$serviceId.json")
            if (!file.exists()) return@forEach

            val root = Json.parseToJsonElement(file.readText()).jsonObject
            val existing = root[arrayName]?.jsonArray ?: JsonArray(emptyList())
            val filtered =
                existing.filterNot { element ->
                    element.jsonObject["name"]?.jsonPrimitive?.contentOrNull == name
                }
            val updatedRoot =
                buildJsonObject {
                    root.forEach { (key, value) ->
                        if (key != arrayName) put(key, value)
                    }
                    put(arrayName, JsonArray(filtered + replacements))
                }
            atomicWrite(file, Json { prettyPrint = true }.encodeToString(updatedRoot))
        }
        return dependents
    }
}
