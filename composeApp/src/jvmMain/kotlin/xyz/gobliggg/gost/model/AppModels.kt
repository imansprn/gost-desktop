package xyz.gobliggg.gost.model

import kotlinx.serialization.Serializable

@Serializable
data class GostRuntimeConfig(
    val binaryPath: String = "",
    val workingDirectory: String = "",
    val autoStart: Boolean = false
)

@Serializable
data class AppSettings(
    val theme: ThemeMode = ThemeMode.DARK,
    val gostRuntime: GostRuntimeConfig = GostRuntimeConfig(),
    val sidebarCollapsed: Boolean = false,
    val confirmDeletes: Boolean = true,
    val defaultPollIntervalSeconds: Int = 10,
    val logBufferSize: Int = 1000,
)

@Serializable
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}
