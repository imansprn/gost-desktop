package xyz.gobliggg.gost.api.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class GostDtosTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test chain serialization`() {
        val chain =
            ChainDto(
                name = "test-chain",
                hops =
                    listOf(
                        HopDto(
                            name = "hop-1",
                            nodes =
                                listOf(
                                    NodeDto(name = "node-1", addr = ":8080"),
                                ),
                        ),
                    ),
            )

        val serialized = Json.encodeToString(chain)
        assertContains(serialized, "test-chain")
        assertContains(serialized, ":8080")

        val deserialized = Json.decodeFromString<ChainDto>(serialized)
        assertEquals("test-chain", deserialized.name)
        assertEquals(1, deserialized.hops?.size)
    }

    @Test
    fun `test bypass serialization`() {
        val bypass =
            BypassDto(
                name = "block-local",
                reverse = true,
                matchers = listOf("127.0.0.1", "localhost"),
            )
        val serialized = Json.encodeToString(bypass)
        val deserialized = Json.decodeFromString<BypassDto>(serialized)
        assertEquals(true, deserialized.reverse)
        assertEquals(2, deserialized.matchers?.size)
    }

    @Test
    fun `test host mapping serialization`() {
        val hosts =
            HostsDto(
                name = "custom-dns",
                mappings =
                    listOf(
                        HostMappingDto(ip = "1.1.1.1", hostnames = listOf("cloudflare.com")),
                    ),
            )
        val serialized = Json.encodeToString(hosts)
        assertContains(serialized, "1.1.1.1")
    }
}
