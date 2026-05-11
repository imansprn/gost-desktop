package xyz.gobliggg.gost.data

import kotlinx.serialization.json.*
import java.io.File

class ConfigBuilder(
    baseDir: File = File(System.getProperty("user.home"), ".gost-manager"),
) {
    private val configsDir = File(baseDir, "configs")
    private val templatesDir = File(baseDir, "templates")

    init {
        if (!configsDir.exists()) configsDir.mkdirs()
        if (!templatesDir.exists()) templatesDir.mkdirs()
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

    // In advanced usage, this would compose multiple selected template files.
    // For now we assume the wizard just generates the raw JSON and we save it here.
    fun buildServiceConfig(
        serviceId: String,
        jsonContent: String,
    ): String {
        validateFileName(serviceId)
        val file = File(configsDir, "$serviceId.json")

        // Pretty print formatting or validation could go here
        try {
            val element = Json.parseToJsonElement(jsonContent)
            val format = Json { prettyPrint = true }
            file.writeText(format.encodeToString(element))
        } catch (e: Exception) {
            // Fallback to writing as-is
            file.writeText(jsonContent)
        }

        return file.absolutePath
    }

    fun deleteServiceConfig(serviceId: String) {
        validateFileName(serviceId)
        val file = File(configsDir, "$serviceId.json")
        if (file.exists()) {
            file.delete()
        }
    }

    fun readServiceConfig(serviceId: String): String? {
        validateFileName(serviceId)
        val file = File(configsDir, "$serviceId.json")
        return if (file.exists()) file.readText() else null
    }

    // List template files (e.g., chains/my-chain.json)
    fun listTemplates(type: String): List<String> {
        val dir = File(templatesDir, type)
        if (!dir.exists()) return emptyList()
        return dir.listFiles { f -> f.isFile && f.name.endsWith(".json") }?.map { it.nameWithoutExtension } ?: emptyList()
    }

    // Read template content
    fun readTemplate(
        type: String,
        name: String,
    ): String? {
        validateFileName(name)
        val file = File(templatesDir, "$type/$name.json")
        if (file.exists()) return file.readText()
        return null
    }

    // Write template content
    fun saveTemplate(
        type: String,
        name: String,
        content: String,
    ) {
        validateFileName(name)
        val dir = File(templatesDir, type)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "$name.json")
        file.writeText(content)
    }

    fun deleteTemplate(
        type: String,
        name: String,
    ) {
        validateFileName(name)
        val file = File(templatesDir, "$type/$name.json")
        if (file.exists()) file.delete()
    }
}
