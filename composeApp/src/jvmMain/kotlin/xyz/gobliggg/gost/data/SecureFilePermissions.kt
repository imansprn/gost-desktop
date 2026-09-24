package xyz.gobliggg.gost.data

import java.io.File

/**
 * Best-effort hardening for local files that may contain runtime configuration or credentials.
 * On platforms that support owner-only permissions, remove group/other access.
 */
internal fun File.restrictToOwner() {
    runCatching {
        setReadable(false, false)
        setWritable(false, false)
        setExecutable(false, false)
        setReadable(true, true)
        setWritable(true, true)
        if (isDirectory) {
            setExecutable(true, true)
        }
    }
}

internal fun File.restrictTreeToOwner() {
    if (!exists()) return
    walkBottomUp().forEach { it.restrictToOwner() }
}
