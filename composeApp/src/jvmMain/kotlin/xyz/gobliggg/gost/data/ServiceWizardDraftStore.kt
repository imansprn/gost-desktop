package xyz.gobliggg.gost.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Serializable
data class ForwarderDraftRow(
    val name: String,
    val addr: String,
)

@Serializable
data class ServiceWizardDraftData(
    val currentStep: Int = 0,
    val name: String = "",
    val addr: String = "",
    val handlerType: String = "http",
    val listenerType: String = "tcp",
    val authUsername: String = "",
    val authPassword: String = "",
    val chainRef: String? = null,
    val autherRef: String? = null,
    val bypassRef: String? = null,
    val admissionRef: String? = null,
    val limiterRef: String? = null,
    val forwarderNodes: List<ForwarderDraftRow> = emptyList(),
    val tlsCertFile: String = "",
    val tlsKeyFile: String = "",
    val tlsCaFile: String = "",
    val metadata: Map<String, String> = emptyMap(),
)

class ServiceWizardDraftStore(
    filePath: String = "${System.getProperty("user.home")}/.gost-manager/service-wizard-draft.json",
) {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    private val file = File(filePath)

    companion object {
        private val defaultInstance by lazy { ServiceWizardDraftStore() }

        /** Convenience accessor for non-DI callers. */
        fun default(): ServiceWizardDraftStore = defaultInstance
    }

    fun load(): ServiceWizardDraftData? {
        return try {
            if (!file.exists()) return null
            json.decodeFromString<ServiceWizardDraftData>(file.readText())
        } catch (e: Exception) {
            val backup =
                File(
                    file.parentFile,
                    "service-wizard-draft.corrupt-${System.currentTimeMillis()}.json",
                )
            val backedUp = runCatching { file.renameTo(backup) }.getOrDefault(false)
            AppState.reportPersistenceIssue(
                if (backedUp) {
                    "An unfinished tunnel draft was corrupt and moved to ${backup.name}."
                } else {
                    "The unfinished tunnel draft is corrupt and could not be backed up: ${e.message}"
                },
            )
            null
        }
    }

    fun save(draft: ServiceWizardDraftData) {
        try {
            val parent = file.parentFile
            if (!parent.exists() && !parent.mkdirs()) {
                throw IllegalStateException("Failed to create draft directory: ${parent.absolutePath}")
            }

            val temp = File.createTempFile("service-wizard-draft", ".json.tmp", parent)
            try {
                temp.writeText(json.encodeToString(draft))
                temp.restrictToOwner()
                try {
                    Files.move(
                        temp.toPath(),
                        file.toPath(),
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING,
                    )
                } catch (_: Exception) {
                    Files.move(
                        temp.toPath(),
                        file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                    )
                }
                file.restrictToOwner()
            } finally {
                if (temp.exists()) temp.delete()
            }
        } catch (e: Exception) {
            AppState.reportPersistenceIssue(
                "Failed to save unfinished tunnel draft: ${e.message ?: "unknown filesystem error"}",
            )
        }
    }

    fun clear() {
        try {
            if (file.exists() && !file.delete()) {
                throw IllegalStateException("Failed to delete draft file")
            }
        } catch (e: Exception) {
            AppState.reportPersistenceIssue(
                "Failed to clear unfinished tunnel draft: ${e.message ?: "unknown filesystem error"}",
            )
        }
    }
}
