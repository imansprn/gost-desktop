package xyz.gobliggg.gost.util

import kotlin.test.Test
import kotlin.test.assertEquals

class FormattersTest {
    @Test
    fun testFormatBytes() {
        assertEquals("0 B", formatBytes(0))
        assertEquals("500 B", formatBytes(500))
        // Note: String.format behavior can be locale-dependent. 
        // Assuming US locale for tests (standard in CI/Gradle)
        assertEquals("1.0 KB", formatBytes(1024))
        assertEquals("1.5 KB", formatBytes(1536))
        assertEquals("1.0 MB", formatBytes(1024 * 1024))
        assertEquals("1.0 GB", formatBytes(1024L * 1024 * 1024))
        assertEquals("1.0 TB", formatBytes(1024L * 1024 * 1024 * 1024))
        assertEquals("0 B", formatBytes(-100))
    }

    @Test
    fun testFormatDuration() {
        assertEquals("0s", formatDuration(0))
        assertEquals("59s", formatDuration(59))
        assertEquals("1m 0s", formatDuration(60))
        assertEquals("1m 1s", formatDuration(61))
        assertEquals("59m 59s", formatDuration(3599))
        assertEquals("1h 0m", formatDuration(3600))
        assertEquals("2h 30m", formatDuration(9000))
    }

    @Test
    fun testTruncate() {
        assertEquals("hello", "hello".truncate(10))
        assertEquals("hello", "hello".truncate(5))
        assertEquals("hel…", "hello".truncate(4))
        assertEquals("", "".truncate(5))
    }

    @Test
    fun testExtractHost() {
        assertEquals("localhost", extractHost("http://localhost"))
        assertEquals("example.com", extractHost("https://example.com/api"))
        assertEquals("127.0.0.1:8080", extractHost("http://127.0.0.1:8080/"))
        assertEquals("myhost", extractHost("myhost"))
    }
}
