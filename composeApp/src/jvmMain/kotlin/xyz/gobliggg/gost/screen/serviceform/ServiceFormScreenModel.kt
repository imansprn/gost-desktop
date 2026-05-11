package xyz.gobliggg.gost.screen.serviceform

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.*
import xyz.gobliggg.gost.api.dto.ChainDto
import xyz.gobliggg.gost.data.*
import xyz.gobliggg.gost.ui.ShellFeedback

val HANDLER_TYPES =
    listOf(
        "http",
        "http2",
        "socks4",
        "socks5",
        "ss",
        "ssu",
        "relay",
        "sni",
        "sshd",
        "dns",
        "red",
        "redu",
        "rtcp",
        "rudp",
        "tun",
        "tap",
        "auto",
        "tcp",
        "udp",
    )
val LISTENER_TYPES =
    listOf(
        "tcp",
        "udp",
        "tls",
        "mtls",
        "ws",
        "mws",
        "h2",
        "h2c",
        "http2",
        "grpc",
        "quic",
        "kcp",
        "ssh",
        "sshd",
        "icmp",
        "ohttp",
        "otls",
        "ftcp",
    )

data class ServiceFormUiState(
    val isEditMode: Boolean = false,
    val currentStep: Int = 0, // 0=Basic, 1=Protocol, 2=Advanced
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
    val forwarderNodes: List<Pair<String, String>> = emptyList(), // name, addr
    val metadata: List<Pair<String, String>> = emptyList(), // key, value
    val tlsCertFile: String = "",
    val tlsKeyFile: String = "",
    val tlsCaFile: String = "",
    // Dropdowns data
    val availableChains: List<String> = emptyList(),
    val availableAuthers: List<String> = emptyList(),
    val availableBypasses: List<String> = emptyList(),
    val availableAdmissions: List<String> = emptyList(),
    val availableLimiters: List<String> = emptyList(),
    // State
    val nameError: String? = null,
    val addrError: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isDirty: Boolean = false,
)

class ServiceFormScreenModel(
    private val editName: String? = null,
    private val wizardDraftStore: ServiceWizardDraftStore = ServiceWizardDraftStore.default(),
    private val configBuilder: ConfigBuilder = ConfigBuilder.default(),
    private val serviceRegistry: ServiceRegistry = ServiceRegistry.default(),
    private val processManager: ProcessManager = ProcessManager.default(),
) : ScreenModel {
    private val json = Json { prettyPrint = true }

    private val _state = MutableStateFlow(ServiceFormUiState(isEditMode = editName != null))
    val state: StateFlow<ServiceFormUiState> = _state.asStateFlow()

    init {
        if (editName == null) {
            wizardDraftStore.load()?.let { draft ->
                applyLoadedDraft(draft)
                _state.value = _state.value.copy(currentStep = 0)
            }
        }
        loadDropdowns()
        if (editName != null) loadService(editName)
    }

    private fun applyLoadedDraft(d: ServiceWizardDraftData) {
        _state.value =
            _state.value.copy(
                currentStep = d.currentStep.coerceIn(0, 2),
                name = d.name,
                addr = d.addr,
                handlerType = d.handlerType,
                listenerType = d.listenerType,
                authUsername = d.authUsername,
                authPassword = d.authPassword,
                chainRef = d.chainRef,
                autherRef = d.autherRef,
                bypassRef = d.bypassRef,
                admissionRef = d.admissionRef,
                limiterRef = d.limiterRef,
                forwarderNodes = d.forwarderNodes.map { it.name to it.addr },
                metadata = d.metadata.map { it.key to it.value },
                tlsCertFile = d.tlsCertFile,
                tlsKeyFile = d.tlsKeyFile,
                tlsCaFile = d.tlsCaFile,
                isDirty = true,
            )
    }

    fun persistDraftToDiskIfNeeded() {
        if (editName != null) return
        val s = _state.value
        if (!s.isDirty) return
        wizardDraftStore.save(
            ServiceWizardDraftData(
                currentStep = s.currentStep,
                name = s.name,
                addr = s.addr,
                handlerType = s.handlerType,
                listenerType = s.listenerType,
                authUsername = s.authUsername,
                authPassword = s.authPassword,
                chainRef = s.chainRef,
                autherRef = s.autherRef,
                bypassRef = s.bypassRef,
                admissionRef = s.admissionRef,
                limiterRef = s.limiterRef,
                forwarderNodes = s.forwarderNodes.map { ForwarderDraftRow(it.first, it.second) },
                metadata = s.metadata.associate { it.first to it.second },
                tlsCertFile = s.tlsCertFile,
                tlsKeyFile = s.tlsKeyFile,
                tlsCaFile = s.tlsCaFile,
            ),
        )
    }

    private fun loadDropdowns() {
        val chains = configBuilder.listTemplates("chains")
        val authers = configBuilder.listTemplates("authers")
        val bypasses = configBuilder.listTemplates("bypass")
        val admissions = configBuilder.listTemplates("admission")
        val limiters = configBuilder.listTemplates("limiters")

        _state.value =
            _state.value.copy(
                availableChains = chains,
                availableAuthers = authers,
                availableBypasses = bypasses,
                availableAdmissions = admissions,
                availableLimiters = limiters,
            )
    }

    private fun loadService(id: String) {
        val jsonConfig = configBuilder.readServiceConfig(id)
        if (jsonConfig == null) {
            _state.value = _state.value.copy(errorMessage = "Config not found")
            return
        }
        try {
            val root = json.parseToJsonElement(jsonConfig).jsonObject
            val svcs = root["services"]?.jsonArray
            val svc = svcs?.firstOrNull()?.jsonObject ?: return

            // Re-hydrate state from local JSON
            val handler = svc["handler"]?.jsonObject
            val listener = svc["listener"]?.jsonObject
            val auth = handler?.get("auth")?.jsonObject
            val tls = listener?.get("tls")?.jsonObject

            _state.value =
                _state.value.copy(
                    name = svc["name"]?.jsonPrimitive?.content ?: "",
                    addr = svc["addr"]?.jsonPrimitive?.content ?: "",
                    handlerType = handler?.get("type")?.jsonPrimitive?.content ?: "http",
                    listenerType = listener?.get("type")?.jsonPrimitive?.content ?: "tcp",
                    authUsername = auth?.get("username")?.jsonPrimitive?.content ?: "",
                    authPassword = auth?.get("password")?.jsonPrimitive?.content ?: "",
                    chainRef = handler?.get("chain")?.jsonPrimitive?.content,
                    autherRef = handler?.get("auther")?.jsonPrimitive?.content,
                    bypassRef = svc["bypass"]?.jsonPrimitive?.content,
                    admissionRef = svc["admission"]?.jsonPrimitive?.content,
                    limiterRef = svc["limiter"]?.jsonPrimitive?.content,
                    forwarderNodes =
                        svc["forwarder"]?.jsonObject?.get("nodes")?.jsonArray?.map {
                            val obj = it.jsonObject
                            val n = obj["name"]?.jsonPrimitive?.content ?: ""
                            val a = obj["addr"]?.jsonPrimitive?.content ?: ""
                            n to a
                        } ?: emptyList(),
                    metadata =
                        run {
                            val mMap = mutableMapOf<String, String>()
                            handler?.get("metadata")?.jsonObject?.forEach { k, v -> mMap[k] = v.jsonPrimitive.content }
                            listener?.get("metadata")?.jsonObject?.forEach { k, v -> mMap[k] = v.jsonPrimitive.content }
                            mMap.toList()
                        },
                    tlsCertFile = tls?.get("certFile")?.jsonPrimitive?.content ?: "",
                    tlsKeyFile = tls?.get("keyFile")?.jsonPrimitive?.content ?: "",
                    tlsCaFile = tls?.get("caFile")?.jsonPrimitive?.content ?: "",
                )
        } catch (e: Exception) {
            _state.value = _state.value.copy(errorMessage = "Failed to parse config: ${e.message}")
        }
    }

    fun updateName(v: String) {
        _state.value = _state.value.copy(name = v, nameError = null, isDirty = true)
    }

    fun updateAddr(v: String) {
        _state.value = _state.value.copy(addr = v, addrError = null, isDirty = true)
    }

    fun updateHandlerType(v: String) {
        _state.value = _state.value.copy(handlerType = v, isDirty = true)
    }

    fun updateListenerType(v: String) {
        _state.value = _state.value.copy(listenerType = v, isDirty = true)
    }

    fun updateAuthUsername(v: String) {
        _state.value = _state.value.copy(authUsername = v, isDirty = true)
    }

    fun updateAuthPassword(v: String) {
        _state.value = _state.value.copy(authPassword = v, isDirty = true)
    }

    fun updateChainRef(v: String?) {
        _state.value = _state.value.copy(chainRef = v, isDirty = true)
    }

    fun updateAutherRef(v: String?) {
        _state.value = _state.value.copy(autherRef = v, isDirty = true)
    }

    fun updateBypassRef(v: String?) {
        _state.value = _state.value.copy(bypassRef = v, isDirty = true)
    }

    fun updateAdmissionRef(v: String?) {
        _state.value = _state.value.copy(admissionRef = v, isDirty = true)
    }

    fun updateLimiterRef(v: String?) {
        _state.value = _state.value.copy(limiterRef = v, isDirty = true)
    }

    fun addForwarderRow() {
        val s = _state.value
        _state.value = s.copy(forwarderNodes = s.forwarderNodes + ("" to ""), isDirty = true)
    }

    fun removeForwarderRow(index: Int) {
        val s = _state.value
        _state.value = s.copy(forwarderNodes = s.forwarderNodes.filterIndexed { i, _ -> i != index }, isDirty = true)
    }

    fun updateForwarderName(
        index: Int,
        v: String,
    ) {
        val s = _state.value
        if (index !in s.forwarderNodes.indices) return
        val list = s.forwarderNodes.toMutableList()
        list[index] = v to list[index].second
        _state.value = s.copy(forwarderNodes = list, isDirty = true)
    }

    fun updateForwarderAddr(
        index: Int,
        v: String,
    ) {
        val s = _state.value
        if (index !in s.forwarderNodes.indices) return
        val list = s.forwarderNodes.toMutableList()
        list[index] = list[index].first to v
        _state.value = s.copy(forwarderNodes = list, isDirty = true)
    }

    fun addMetadataRow() {
        val s = _state.value
        _state.value = s.copy(metadata = s.metadata + ("" to ""), isDirty = true)
    }

    fun removeMetadataRow(index: Int) {
        val s = _state.value
        if (index !in s.metadata.indices) return
        _state.value = s.copy(metadata = s.metadata.filterIndexed { i, _ -> i != index }, isDirty = true)
    }

    fun updateMetadataKey(
        index: Int,
        v: String,
    ) {
        val s = _state.value
        if (index !in s.metadata.indices) return
        val list = s.metadata.toMutableList()
        list[index] = v to list[index].second
        _state.value = s.copy(metadata = list, isDirty = true)
    }

    fun updateMetadataValue(
        index: Int,
        v: String,
    ) {
        val s = _state.value
        if (index !in s.metadata.indices) return
        val list = s.metadata.toMutableList()
        list[index] = list[index].first to v
        _state.value = s.copy(metadata = list, isDirty = true)
    }

    fun nextStep(): Boolean {
        val s = _state.value
        when (s.currentStep) {
            0 -> {
                var valid = true
                var ne: String? = null
                var ae: String? = null
                if (s.name.isBlank() || s.name.contains("\\s".toRegex())) {
                    ne = "Required, no spaces"
                    valid = false
                }
                if (s.addr.isBlank()) {
                    ae = "Required"
                    valid = false
                }
                if (!valid) {
                    _state.value = s.copy(nameError = ne, addrError = ae)
                    return false
                }
            }
        }
        _state.value = _state.value.copy(currentStep = (s.currentStep + 1).coerceAtMost(2))
        return true
    }

    fun prevStep() {
        _state.value = _state.value.copy(currentStep = (_state.value.currentStep - 1).coerceAtLeast(0))
    }

    fun goToStep(target: Int) {
        val t = target.coerceIn(0, 2)
        if (t == _state.value.currentStep) return
        if (t < _state.value.currentStep) {
            _state.value = _state.value.copy(currentStep = t)
            return
        }
        while (_state.value.currentStep < t) {
            if (!nextStep()) return
        }
    }

    fun createChainFromWizard(
        ch: ChainDto,
        onDone: (String?) -> Unit,
    ) {
        val name = ch.name
        if (name.isNullOrBlank()) {
            onDone("Chain name is required")
            return
        }
        try {
            val content = json.encodeToString(ch)
            configBuilder.saveTemplate("chains", name, content)
            loadDropdowns()
            _state.value = _state.value.copy(chainRef = name, isDirty = true)
            onDone(null)
        } catch (e: Exception) {
            onDone(e.message ?: "Failed to save chain")
        }
    }

    fun buildPreviewJson(): String = json.encodeToString(buildJsonObjectConfig())

    private fun buildJsonObjectConfig(): JsonObject {
        val s = _state.value

        // Build Handler
        val handler =
            buildJsonObject {
                put("type", s.handlerType)
                if (s.authUsername.isNotBlank()) {
                    put(
                        "auth",
                        buildJsonObject {
                            put("username", s.authUsername)
                            put("password", s.authPassword)
                        },
                    )
                }
                s.chainRef?.let { put("chain", it) }
                s.autherRef?.let { put("auther", it) }

                val validMetadata = s.metadata.filter { it.first.isNotBlank() }
                if (validMetadata.isNotEmpty()) {
                    put(
                        "metadata",
                        buildJsonObject {
                            validMetadata.forEach { (k, v) -> put(k, v) }
                        },
                    )
                }
            }

        // Build Listener
        val listener =
            buildJsonObject {
                put("type", s.listenerType)
                if (s.tlsCertFile.isNotBlank()) {
                    put(
                        "tls",
                        buildJsonObject {
                            put("certFile", s.tlsCertFile)
                            put("keyFile", s.tlsKeyFile)
                            put("caFile", s.tlsCaFile)
                        },
                    )
                }
                val validMetadata = s.metadata.filter { it.first.isNotBlank() }
                if (validMetadata.isNotEmpty()) {
                    put(
                        "metadata",
                        buildJsonObject {
                            validMetadata.forEach { (k, v) -> put(k, v) }
                        },
                    )
                }
            }

        // Build Service
        val service =
            buildJsonObject {
                put("name", s.name)
                if (s.addr.isNotBlank()) put("addr", s.addr)
                put("handler", handler)
                put("listener", listener)

                s.bypassRef?.let { put("bypass", it) }
                s.admissionRef?.let { put("admission", it) }
                s.limiterRef?.let { put("limiter", it) }

                val validNodes = s.forwarderNodes.filter { it.second.isNotBlank() }
                if (validNodes.isNotEmpty()) {
                    put(
                        "forwarder",
                        buildJsonObject {
                            put(
                                "nodes",
                                buildJsonArray {
                                    validNodes.forEach { (n, a) ->
                                        add(
                                            buildJsonObject {
                                                if (n.isNotBlank()) put("name", n)
                                                put("addr", a)
                                            },
                                        )
                                    }
                                },
                            )
                        },
                    )
                }
            }

        val arrays = mutableMapOf<String, JsonArray>()
        // Note: Actual embedded embedding happens here if necessary.
        // E.g. we might want to read `chains/$chainRef.json` and put it inside `chains: []` block of the overall file!
        // The GOST v3 config supports a global `chains: [{name: "xyz", ...}]`.

        fun attachTemplate(
            type: String,
            ref: String?,
            rootArrayName: String,
        ) {
            if (ref != null) {
                val tp = configBuilder.readTemplate(type, ref)
                if (tp != null) {
                    val arr = arrays.getOrPut(rootArrayName) { buildJsonArray {} }
                    val newArr =
                        buildJsonArray {
                            arr.forEach { add(it) }
                            try {
                                // If template is already an array, add its elements, otherwise add the object
                                val el = json.parseToJsonElement(tp)
                                if (el is JsonArray) el.forEach { add(it) } else add(el)
                            } catch (e: Exception) {
                                println("failed to attach template $type/$ref: ${e.message}")
                            }
                        }
                    arrays[rootArrayName] = newArr
                }
            }
        }

        attachTemplate("chains", s.chainRef, "chains")
        attachTemplate("authers", s.autherRef, "authers")
        attachTemplate("bypass", s.bypassRef, "bypasses")
        attachTemplate("admission", s.admissionRef, "admissions")
        attachTemplate("limiters", s.limiterRef, "limiters")

        return buildJsonObject {
            put("services", buildJsonArray { add(service) })
            arrays.forEach { (name, arr) ->
                put(name, arr)
            }
        }
    }

    fun save(onSuccess: () -> Unit) {
        _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
        try {
            val configContent = buildPreviewJson()
            val id = _state.value.name // id is just the name chosen by user

            val path = configBuilder.buildServiceConfig(id, configContent)

            // If we are editing, stop the old process and handle renames
            if (editName != null) {
                processManager.stopService(editName)
                if (editName != id) {
                    serviceRegistry.removeService(editName)
                    configBuilder.deleteServiceConfig(editName)
                }
            }

            serviceRegistry.addOrUpdateService(
                ServiceEntity(
                    id = id,
                    name = id,
                    addr = _state.value.addr,
                    configPath = path,
                    status = ServiceStatus.IDLE, // starts idle, must be explicitly started
                ),
            )

            if (editName == null) wizardDraftStore.clear()
            _state.value = _state.value.copy(isSubmitting = false)
            ShellFeedback.showSnackbar(if (editName != null) "Tunnel updated" else "Tunnel created")
            onSuccess()
        } catch (e: Exception) {
            _state.value = _state.value.copy(isSubmitting = false, errorMessage = e.message)
        }
    }
}
