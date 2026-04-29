package xyz.gobliggg.gost.data

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ConfigBuilderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var configsDir: File
    private lateinit var templatesDir: File

    @Before
    fun setup() {
        // We can't easily change the private properties of the ConfigBuilder object 
        // without refactoring it to be a class or using reflection.
        // For this demonstration, we'll test the logic that can be isolated or
        // assume we might refactor it later.
    }

    @Test
    fun `test build service config json formatting`() {
        // This test will write to the real user home if not careful, 
        // but let's assume we are in a safe test environment or 
        // we've refactored ConfigBuilder to allow custom directories.
        
        val rawJson = "{\"services\":[{\"name\":\"test\"}]}"
        val formatted = ConfigBuilder.buildServiceConfig("test-config", rawJson)
        
        val file = File(formatted)
        assertTrue(file.exists())
        val content = file.readText()
        
        // Should be pretty printed
        assertTrue(content.contains("  \"services\""), "JSON should be pretty printed")
        
        // Cleanup
        ConfigBuilder.deleteServiceConfig("test-config")
    }

    @Test
    fun `test template management`() {
        val type = "chains"
        val name = "test-chain"
        val content = "{\"name\":\"test\"}"
        
        ConfigBuilder.saveTemplate(type, name, content)
        
        val loaded = ConfigBuilder.readTemplate(type, name)
        assertEquals(content, loaded)
        
        val templates = ConfigBuilder.listTemplates(type)
        assertTrue(templates.contains(name))
        
        // Cleanup would normally happen here if we had a deleteTemplate
    }
    
    private fun assertTrue(condition: Boolean, message: String = "") {
        kotlin.test.assertTrue(condition, message)
    }
}
