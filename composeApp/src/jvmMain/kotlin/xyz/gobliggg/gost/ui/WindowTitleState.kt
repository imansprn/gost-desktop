package xyz.gobliggg.gost.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Reactive window title for [xyz.gobliggg.gost.main]. */
object WindowTitleState {
    private val _title = MutableStateFlow("GOST Manager")
    val title: StateFlow<String> = _title.asStateFlow()

    fun update(connected: Boolean, profileName: String?) {
        _title.value = when {
            !connected -> "GOST Manager"
            !profileName.isNullOrBlank() -> "GOST Manager — $profileName"
            else -> "GOST Manager"
        }
    }
}
