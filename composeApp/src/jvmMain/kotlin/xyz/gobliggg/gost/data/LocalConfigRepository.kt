package xyz.gobliggg.gost.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.gobliggg.gost.model.AppSettings
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Serializable
data class LocalConfig(
    val settings: AppSettings = AppSettings(),
)

class LocalConfigRepository(
    private val baseDir: File = File(System.getProperty("user.home"), ".gost-manager"),
) {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    private val configDir = baseDir
    private val configFile = File(configDir, "config.json")

    var lastLoadWarning: String? = null
        private set

    init {
        if (!configDir.exists()) {
            configDir.mkdirs()
        }
        configDir.restrictToOwner()
        if (configFile.exists()) configFile.restrictToOwner()
    }

    fun load(): LocalConfig =
        try {
            lastLoadWarning = null
            if (configFile.exists()) {
                json.decodeFromString<LocalConfig>(configFile.readText())
            } else {
                LocalConfig()
            }
        } catch (_: Exception) {
            val backup =
                File(
                    configDir,
                    "config.corrupt-${System.currentTimeMillis()}.json",
                )
            val backedUp = runCatching { configFile.renameTo(backup) }.getOrDefault(false)
            lastLoadWarning =
                if (backedUp) {
                    "Settings file was corrupt and moved to ${backup.name}. Defaults were loaded."
                } else {
                    "Settings file is corrupt and could not be backed up. Defaults were loaded."
                }
            LocalConfig()
        }

    fun save(config: LocalConfig) {
        if (!configDir.exists() && !configDir.mkdirs()) {
            throw IllegalStateException("Failed to create settings directory: ${configDir.absolutePath}")
        }

        val text = json.encodeToString(config)
        val temp = File.createTempFile("config", ".json.tmp", configDir)
        try {
            temp.writeText(text)
            temp.restrictToOwner()
            try {
                Files.move(
                    temp.toPath(),
                    configFile.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: Exception) {
                Files.move(
                    temp.toPath(),
                    configFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                )
            }
            configFile.restrictToOwner()
        } finally {
            if (temp.exists()) temp.delete()
        }
    }
}
