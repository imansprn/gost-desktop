package xyz.gobliggg.gost.screen.authers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import kotlinx.serialization.json.Json
import xyz.gobliggg.gost.api.dto.AutherDto
import xyz.gobliggg.gost.data.ConfigBuilder
import xyz.gobliggg.gost.ui.components.AutherFormEditor
import xyz.gobliggg.gost.ui.components.AutherFormVariant

class AutherFormScreen(
    private val routeId: String,
    private val editName: String? = null,
    private val onDone: () -> Unit = {},
    private val onCancel: () -> Unit = {},
) : Screen {

    override val key: ScreenKey = routeId

    @Composable
    override fun Content() {
        val json = remember { Json { ignoreUnknownKeys = true; prettyPrint = true } }
        val initialAuther = remember(editName) {
            if (editName.isNullOrBlank()) return@remember null
            val content = ConfigBuilder.readTemplate("authers", editName)
            if (content.isNullOrBlank()) return@remember AutherDto(name = editName)
            try {
                json.decodeFromString(AutherDto.serializer(), content)
            } catch (_: Exception) {
                AutherDto(name = editName)
            }
        }

        AutherFormEditor(
            initialAuther = initialAuther,
            title = if (editName == null) "New Auth Rule" else "Edit Auth Rule",
            variant = AutherFormVariant.FullScreen,
            onSave = { dto ->
                val name = dto.name
                if (!name.isNullOrBlank()) {
                    ConfigBuilder.saveTemplate("authers", name, json.encodeToString(AutherDto.serializer(), dto))
                    onDone()
                }
            },
            onCancel = onCancel,
        )
    }
}

