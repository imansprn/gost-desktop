package xyz.gobliggg.gost.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidatorsTest {
    @Test
    fun testIsValidUrl() {
        assertTrue(isValidUrl("http://localhost"))
        assertTrue(isValidUrl("https://example.com"))
        assertTrue(isValidUrl("http://127.0.0.1:8080"))
        assertTrue(isValidUrl("https://my-server.io/api/v1"))
        assertFalse(isValidUrl("ftp://wrong.com"))
        assertFalse(isValidUrl("just-a-string"))
        assertFalse(isValidUrl("http://"))
    }

    @Test
    fun testIsValidAddr() {
        assertTrue(isValidAddr(":8080"))
        assertTrue(isValidAddr("localhost:1080"))
        assertTrue(isValidAddr("0.0.0.0:443"))
        assertTrue(isValidAddr("my.host-name:12345"))
        assertFalse(isValidAddr(""))
        assertFalse(isValidAddr("8080"))
        assertFalse(isValidAddr("host:"))
        assertFalse(isValidAddr("host:port"))
    }

    @Test
    fun testIsValidCidr() {
        assertTrue(isValidCidr("192.168.0.0/24"))
        assertTrue(isValidCidr("0.0.0.0/0"))
        assertTrue(isValidCidr("255.255.255.255/32"))
        assertFalse(isValidCidr("192.168.0.0"))
        assertFalse(isValidCidr("192.168.0.0/33"))
        assertFalse(isValidCidr("a.b.c.d/24"))
        assertFalse(isValidCidr("192.168.0.0/prefix"))
    }

    @Test
    fun testIsValidIpv4() {
        assertTrue(isValidIpv4("127.0.0.1"))
        assertTrue(isValidIpv4("0.0.0.0"))
        assertTrue(isValidIpv4("255.255.255.255"))
        assertFalse(isValidIpv4("256.256.256.256"))
        assertFalse(isValidIpv4("1.2.3"))
        assertFalse(isValidIpv4("1.2.3.4.5"))
        assertFalse(isValidIpv4("a.b.c.d"))
    }

    @Test
    fun testIsValidPattern() {
        assertTrue(isValidPattern("*.example.com"))
        assertTrue(isValidPattern("direct"))
        assertFalse(isValidPattern(""))
        assertFalse(isValidPattern("with space"))
    }

    @Test
    fun testIsValidServiceName() {
        assertTrue(isValidServiceName("my-service"))
        assertTrue(isValidServiceName("service123"))
        assertFalse(isValidServiceName(""))
        assertFalse(isValidServiceName("service name"))
    }
}
