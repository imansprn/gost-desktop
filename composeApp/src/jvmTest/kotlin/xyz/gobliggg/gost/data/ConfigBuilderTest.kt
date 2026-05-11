package xyz.gobliggg.gost.data

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class ConfigBuilderTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var configBuilder: ConfigBuilder
    private lateinit var baseDir: File

    @Before
    fun setup() {
        baseDir = tempFolder.newFolder("gost-manager")
        configBuilder = ConfigBuilder(baseDir)
    }

    @After
    fun cleanup() {
        // No cleanup needed — TemporaryFolder handles it
    }

    @Test
    fun `test build service config json formatting`() {
        val rawJson = """{"services":[{"name":"test"}]}"""
        val formatted = configBuilder.buildServiceConfig("test-config", rawJson)

        val file = File(formatted)
        assertTrue(file.exists())
        val content = file.readText()

        // Should be pretty printed
        assertTrue(content.contains("  \"services\""), "JSON should be pretty printed")
    }

    @Test
    fun `test template management`() {
        val type = "chains"
        val name = "test-chain"
        val content = """{"name":"test"}"""

        configBuilder.saveTemplate(type, name, content)

        val loaded = configBuilder.readTemplate(type, name)
        assertNotNull(loaded)
        assertEquals(content, loaded)

        val templates = configBuilder.listTemplates(type)
        assertTrue(templates.contains(name))
    }

    @Test
    fun `test delete template`() {
        configBuilder.saveTemplate("chains", "delete-me", """{"name":"delete-me"}""")
        assertNotNull(configBuilder.readTemplate("chains", "delete-me"))

        configBuilder.deleteTemplate("chains", "delete-me")
        assertNull(configBuilder.readTemplate("chains", "delete-me"))
    }

    @Test
    fun `test path traversal attack is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            configBuilder.saveTemplate("chains", "../../etc/passwd", "evil")
        }

        assertFailsWith<IllegalArgumentException> {
            configBuilder.buildServiceConfig("../../malicious", "{}")
        }
    }

    @Test
    fun `test valid names are accepted`() {
        configBuilder.saveTemplate("chains", "valid-name_123", """{"name":"ok"}""")
        configBuilder.saveTemplate("chains", "VALID_NAME", """{"name":"ok"}""")
        configBuilder.saveTemplate("chains", "complex-Name_99", """{"name":"ok"}""")

        val templates = configBuilder.listTemplates("chains")
        assertTrue(templates.contains("valid-name_123"))
        assertTrue(templates.contains("VALID_NAME"))
        assertTrue(templates.contains("complex-Name_99"))
    }

    @Test
    fun `test read and delete service config`() {
        val path = configBuilder.buildServiceConfig("svc-42", """{"services":[{"name":"svc-42"}]}""")
        assertTrue(File(path).exists())

        val content = configBuilder.readServiceConfig("svc-42")
        assertNotNull(content)

        configBuilder.deleteServiceConfig("svc-42")
        assertNull(configBuilder.readServiceConfig("svc-42"))
    }
}
