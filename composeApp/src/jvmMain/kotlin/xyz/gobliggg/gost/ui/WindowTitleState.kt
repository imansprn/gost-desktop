package xyz.gobliggg.gost.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Reactive window title for [xyz.gobliggg.gost.main]. */
object WindowTitleState {
    private val _title = MutableStateFlow("GOST Desktop")
    val title: StateFlow<String> = _title.asStateFlow()

    fun update(connected: Boolean, profileName: String?) {
        _title.value = when {
            !connected -> "GOST Desktop"
            !profileName.isNullOrBlank() -> "GOST Desktop — $profileName"
            else -> "GOST Desktop"
        }
    }
}
