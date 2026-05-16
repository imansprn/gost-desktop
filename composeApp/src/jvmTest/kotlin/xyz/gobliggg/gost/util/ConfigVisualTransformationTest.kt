package xyz.gobliggg.gost.util

import androidx.compose.ui.text.AnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfigVisualTransformationTest {
    @Test
    fun testJsonHighlighting() {
        val transformer = ConfigVisualTransformation("json")
        val text = """{"key": "value", "count": 123, "active": true}"""
        val input = AnnotatedString(text)
        val result = transformer.filter(input)
        
        assertEquals(text, result.text.text)
        
        // Assert that some styles were added
        val styles = result.text.spanStyles
        assertTrue(styles.isNotEmpty(), "Highlighting styles should be applied to JSON")
    }

    @Test
    fun testYamlHighlighting() {
        val transformer = ConfigVisualTransformation("yaml")
        val text = "key: value\ncount: 123\nactive: true"
        val input = AnnotatedString(text)
        val result = transformer.filter(input)
        
        assertEquals(text, result.text.text)
        
        // Assert that some styles were added
        val styles = result.text.spanStyles
        assertTrue(styles.isNotEmpty(), "Highlighting styles should be applied to YAML")
    }
}
