package xyz.gobliggg.gost.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

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

object ServiceWizardDraftStore {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    private val dir = File(System.getProperty("user.home"), ".gost-manager")
    private val file = File(dir, "service-wizard-draft.json")

    fun load(): ServiceWizardDraftData? {
        return try {
            if (!file.exists()) return null
            json.decodeFromString<ServiceWizardDraftData>(file.readText())
        } catch (_: Exception) {
            null
        }
    }

    fun save(draft: ServiceWizardDraftData) {
        try {
            if (!dir.exists()) dir.mkdirs()
            file.writeText(json.encodeToString(draft))
        } catch (_: Exception) {
        }
    }

    fun clear() {
        try {
            if (file.exists()) file.delete()
        } catch (_: Exception) {
        }
    }
}
