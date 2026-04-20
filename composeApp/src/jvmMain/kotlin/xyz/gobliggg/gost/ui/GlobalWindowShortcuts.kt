package xyz.gobliggg.gost.ui

/**
 * Window-level shortcuts ([App]) invoke these when the corresponding screen is active.
 * Screens register handlers with [DisposableEffect] and clear them on dispose.
 */
object GlobalWindowShortcuts {
    var saveHandler: (() -> Unit)? = null
    var refreshHandler: (() -> Unit)? = null
}
