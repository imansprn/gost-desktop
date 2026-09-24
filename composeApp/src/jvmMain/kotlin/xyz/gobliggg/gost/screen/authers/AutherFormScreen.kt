package xyz.gobliggg.gost.screen.authers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import kotlinx.serialization.json.Json
import xyz.gobliggg.gost.api.dto.AutherDto
import xyz.gobliggg.gost.data.ConfigBuilder
import xyz.gobliggg.gost.data.TemplateRuntimeSynchronizer
import xyz.gobliggg.gost.data.TemplateTypes
import xyz.gobliggg.gost.ui.ShellFeedback
import xyz.gobliggg.gost.ui.UnsavedChangesGuard
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
        val json =
            remember {
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }
            }
        val initialAuther =
            remember(editName) {
                if (editName.isNullOrBlank()) return@remember null
                val content = ConfigBuilder.default().readTemplate(TemplateTypes.AUTHERS, editName)
                if (content.isNullOrBlank()) return@remember AutherDto(name = editName)
                try {
                    json.decodeFromString(AutherDto.serializer(), content)
                } catch (_: Exception) {
                    AutherDto(name = editName)
                }
            }

        DisposableEffect(Unit) {
            onDispose { UnsavedChangesGuard.clear() }
        }

        AutherFormEditor(
            initialAuther = initialAuther,
            title = if (editName == null) "New Auth Rule" else "Edit Auth Rule",
            variant = AutherFormVariant.FullScreen,
            onSave = { dto ->
                val name = dto.name
                if (!name.isNullOrBlank()) {
                    val builder = ConfigBuilder.default()
                    try {
                        if (!builder.isValidName(name)) {
                            throw IllegalArgumentException(
                                "Use only letters, numbers, underscore, and hyphen.",
                            )
                        }
                        if (builder.templateExists(TemplateTypes.AUTHERS, name) && name != editName) {
                            throw IllegalArgumentException("An auth rule named '$name' already exists.")
                        }
                        if (editName != null && editName != name) {
                            val dependents =
                                builder.findDependentServices(TemplateTypes.AUTHERS, editName)
                            if (dependents.isNotEmpty()) {
                                throw IllegalStateException(
                                    "Cannot rename while used by: " + dependents.joinToString(),
                                )
                            }
                        }

                        builder.saveTemplate(
                            TemplateTypes.AUTHERS,
                            name,
                            json.encodeToString(AutherDto.serializer(), dto),
                        )
                        if (editName != null && editName != name) {
                            builder.deleteTemplate(TemplateTypes.AUTHERS, editName)
                        }
                        TemplateRuntimeSynchronizer
                            .synchronize(TemplateTypes.AUTHERS, name)
                            .getOrThrow()
                        ShellFeedback.showSnackbar("Auth rule saved")
                        onDone()
                    } catch (e: Exception) {
                        ShellFeedback.showSnackbar(e.message ?: "Failed to save auth rule")
                    }
                }
            },
            onCancel = onCancel,
            onDirtyChange = UnsavedChangesGuard::setDirty,
        )
    }
}
