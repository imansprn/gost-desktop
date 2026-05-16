package xyz.gobliggg.gost.util

/**
 * Format bytes to human-readable string (KB, MB, GB, TB).
 */
fun formatBytes(bytes: Long): String {
    if (bytes < 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return if (unitIndex == 0) {
        "$bytes B"
    } else {
        java.util.Locale.US.let { locale ->
            String.format(locale, "%.1f %s", value, units[unitIndex])
        }
    }
}

/**
 * Format a duration in seconds to a readable string.
 */
fun formatDuration(seconds: Long): String {
    if (seconds < 60) return "${seconds}s"
    if (seconds < 3600) return "${seconds / 60}m ${seconds % 60}s"
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return "${h}h ${m}m"
}

/**
 * Truncate a string with ellipsis if it exceeds maxLength.
 */
fun String.truncate(maxLength: Int): String =
    if (length <= maxLength) {
        this
    } else {
        take(maxLength - 1) + "…"
    }

/**
 * Extract the host portion from a URL string.
 */
fun extractHost(url: String): String =
    url
        .removePrefix("http://")
        .removePrefix("https://")
        .split("/")
        .firstOrNull() ?: url
