package xyz.gobliggg.gost.util

/**
 * Validate a URL string.
 */
fun isValidUrl(url: String): Boolean {
    val pattern = Regex("^https?://[\\w.-]+(:\\d{1,5})?(/.*)?$")
    return pattern.matches(url)
}

/**
 * Validate an address string (e.g. ":8080" or "0.0.0.0:1080").
 */
fun isValidAddr(addr: String): Boolean {
    if (addr.isBlank()) return false
    // Formats: ":port", "host:port", "0.0.0.0:port"
    val pattern = Regex("^([\\w.*-]*):([0-9]{1,5})$")
    return pattern.matches(addr)
}

/**
 * Validate CIDR notation (e.g. "192.168.0.0/24").
 */
fun isValidCidr(cidr: String): Boolean {
    val pattern = Regex("^(\\d{1,3}\\.){3}\\d{1,3}/\\d{1,2}$")
    if (!pattern.matches(cidr)) return false
    val parts = cidr.split("/")
    val prefix = parts[1].toIntOrNull() ?: return false
    return prefix in 0..32
}

/**
 * Validate an IP address (IPv4).
 */
fun isValidIpv4(ip: String): Boolean {
    val pattern = Regex("^(\\d{1,3}\\.){3}\\d{1,3}$")
    if (!pattern.matches(ip)) return false
    return ip.split(".").all {
        val v = it.toIntOrNull() ?: return false
        v in 0..255
    }
}

/**
 * Validate a glob/domain pattern (e.g. "*.example.com").
 */
fun isValidPattern(pattern: String): Boolean {
    return pattern.isNotBlank() && !pattern.contains(" ")
}

/**
 * Validate a service name (no spaces, not empty).
 */
fun isValidServiceName(name: String): Boolean {
    return name.isNotBlank() && !name.contains("\\s".toRegex())
}
