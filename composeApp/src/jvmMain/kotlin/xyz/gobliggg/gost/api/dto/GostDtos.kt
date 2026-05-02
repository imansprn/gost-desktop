package xyz.gobliggg.gost.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthDto(
    val username: String? = null,
    val password: String? = null,
)

@Serializable
data class ConnectorDto(
    val type: String? = null,
    val auth: AuthDto? = null,
    val metadata: Map<String, String>? = null,
)

@Serializable
data class DialerDto(
    val type: String? = null,
    val auth: AuthDto? = null,
    val metadata: Map<String, String>? = null,
)

@Serializable
data class NodeDto(
    val name: String? = null,
    val addr: String? = null,
    val connector: ConnectorDto? = null,
    val dialer: DialerDto? = null,
    val auth: AuthDto? = null,
)

@Serializable
data class HopDto(
    val name: String? = null,
    val nodes: List<NodeDto>? = null,
)

@Serializable
data class ChainDto(
    val name: String? = null,
    val hops: List<HopDto>? = null,
)

@Serializable
data class AutherDto(
    val name: String? = null,
    val auths: List<AuthDto>? = null,
    val plugin: PluginDto? = null,
)

@Serializable
data class PluginDto(
    val type: String? = null,
    val addr: String? = null,
    val token: String? = null,
)

@Serializable
data class BypassDto(
    val name: String? = null,
    val reverse: Boolean? = false,
    val matchers: List<String>? = null,
)

@Serializable
data class AdmissionDto(
    val name: String? = null,
    val reverse: Boolean? = false,
    val matchers: List<String>? = null,
)

@Serializable
data class ResolverDto(
    val name: String? = null,
    val nameservers: List<NameserverDto>? = null,
    val ttl: String? = null,
    val prefer: String? = null,
)

@Serializable
data class NameserverDto(
    val addr: String? = null,
    val chain: String? = null,
    val prefer: String? = null,
)

@Serializable
data class HostsDto(
    val name: String? = null,
    val mappings: List<HostMappingDto>? = null,
)

@Serializable
data class HostMappingDto(
    val ip: String? = null,
    val hostnames: List<String>? = null,
)
