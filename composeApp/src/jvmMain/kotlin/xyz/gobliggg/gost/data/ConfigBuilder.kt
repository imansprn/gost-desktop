package xyz.gobliggg.gost.data

import java.io.File
import kotlinx.serialization.json.*

object ConfigBuilder {
    private val configsDir = File(System.getProperty("user.home"), ".gost-manager/configs")
    private val templatesDir = File(System.getProperty("user.home"), ".gost-manager/templates")
    
    init {
        if (!configsDir.exists()) configsDir.mkdirs()
        if (!templatesDir.exists()) templatesDir.mkdirs()
    }
    
    // In advanced usage, this would compose multiple selected template files.
    // For now we assume the wizard just generates the raw JSON and we save it here.
    fun buildServiceConfig(serviceId: String, jsonContent: String): String {
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
        val file = File(configsDir, "$serviceId.json")
        if (file.exists()) {
            file.delete()
        }
    }
    
    fun readServiceConfig(serviceId: String): String? {
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
    fun readTemplate(type: String, name: String): String? {
        val file = File(templatesDir, "$type/$name.json")
        if (file.exists()) return file.readText()
        return null
    }

    // Write template content
    fun saveTemplate(type: String, name: String, content: String) {
        val dir = File(templatesDir, type)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "$name.json")
        file.writeText(content)
    }
}
