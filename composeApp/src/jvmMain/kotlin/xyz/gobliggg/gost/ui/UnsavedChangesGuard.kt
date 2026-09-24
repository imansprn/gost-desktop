package xyz.gobliggg.gost.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object UnsavedChangesGuard {
    private val _isDirty = MutableStateFlow(false)
    val isDirty = _isDirty.asStateFlow()

    fun setDirty(value: Boolean) {
        _isDirty.value = value
    }

    fun clear() {
        _isDirty.value = false
    }
}
