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
        } catch (_: Exception) {
            null
        }
    }

    fun save(draft: ServiceWizardDraftData) {
        try {
            if (!file.parentFile.exists()) file.parentFile.mkdirs()
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
