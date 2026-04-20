package xyz.gobliggg.gost.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import xyz.gobliggg.gost.ui.theme.AmberStatus
import xyz.gobliggg.gost.ui.theme.Cyan300
import xyz.gobliggg.gost.ui.theme.GreenStatus

class ConfigVisualTransformation(private val format: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val coloredString = buildAnnotatedString {
            append(text.text)

            // Basic JSON/YAML Highlighting

            // Strings: ".*"
            val stringRegex = "\".*?\"".toRegex()
            stringRegex.findAll(text.text).forEach { result ->
                addStyle(SpanStyle(color = Cyan300), result.range.first, result.range.last + 1)
            }

            // Keys in JSON: `"key":`
            val keyRegexJson = "\"([^\"]+)\"\\s*:".toRegex()
            keyRegexJson.findAll(text.text).forEach { result ->
                addStyle(SpanStyle(color = Color(0xFF9CDCFE)), result.groups[1]!!.range.first, result.groups[1]!!.range.last + 1)
            }
            
            // Keys in YAML: `key:` at the start of a line or after spaces
            if (format == "yaml") {
                val keyRegexYaml = "^\\s*([a-zA-Z0-9_-]+):".toRegex(RegexOption.MULTILINE)
                keyRegexYaml.findAll(text.text).forEach { result ->
                    addStyle(SpanStyle(color = Color(0xFF9CDCFE)), result.groups[1]!!.range.first, result.groups[1]!!.range.last + 1)
                }
            }

            // Numbers: \d+
            val numberRegex = "(?<![a-zA-Z0-9_-])(?<!\")\\b\\d+(\\.\\d+)?\\b(?!\")".toRegex()
            numberRegex.findAll(text.text).forEach { result ->
                addStyle(SpanStyle(color = AmberStatus), result.range.first, result.range.last + 1)
            }

            // Booleans
            val boolRegex = "(?<![a-zA-Z0-9_-])(?<!\")\\b(true|false)\\b(?!\")".toRegex()
            boolRegex.findAll(text.text).forEach { result ->
                addStyle(SpanStyle(color = GreenStatus), result.range.first, result.range.last + 1)
            }
        }
        return TransformedText(coloredString, OffsetMapping.Identity)
    }
}
