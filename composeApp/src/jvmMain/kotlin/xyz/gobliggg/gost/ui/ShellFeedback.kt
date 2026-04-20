package xyz.gobliggg.gost.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * App-wide snackbar messages (success / info) from screen models and screens.
 */
object ShellFeedback {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _snackbars = MutableSharedFlow<String>(extraBufferCapacity = 32)
    val snackbars = _snackbars.asSharedFlow()

    fun showSnackbar(message: String) {
        scope.launch { _snackbars.emit(message) }
    }
}
