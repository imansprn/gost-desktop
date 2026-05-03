package xyz.gobliggg.gost.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class GostRuntimeConfig(
    val binaryPath: String = "",
    val workingDirectory: String = "",
    val autoStart: Boolean = false,
)

@Serializable
@Immutable
data class AppSettings(
    val accentColor: AccentColor = AccentColor.CYAN,
    val gostRuntime: GostRuntimeConfig = GostRuntimeConfig(),
    val sidebarCollapsed: Boolean = false,
    val confirmDeletes: Boolean = true,
    val defaultPollIntervalSeconds: Int = 10,
    val logBufferSize: Int = 1000,
)

@Serializable
enum class AccentColor {
    CYAN,
    EMERALD,
    INDIGO,
    AMBER,
}
