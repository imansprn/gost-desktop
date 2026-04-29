package xyz.gobliggg.gost.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.gobliggg.gost.model.AppSettings
import java.io.File

@Serializable
data class LocalConfig(
    val settings: AppSettings = AppSettings()
)

class LocalConfigRepository(
    private val baseDir: File = File(System.getProperty("user.home"), ".gost-manager")
) {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    private val configDir = baseDir
    private val configFile = File(configDir, "config.json")
    
    init {
        if (!configDir.exists()) {
            configDir.mkdirs()
        }
    }
    
    fun load(): LocalConfig {
        return try {
            if (configFile.exists()) {
                json.decodeFromString<LocalConfig>(configFile.readText())
            } else {
                LocalConfig()
            }
        } catch (e: Exception) {
            println("Failed to load config: ${e.message}")
            LocalConfig()
        }
    }
    
    fun save(config: LocalConfig) {
        try {
            val text = json.encodeToString(config)
            configFile.writeText(text)
        } catch (e: Exception) {
            println("Failed to save config: ${e.message}")
        }
    }
}
