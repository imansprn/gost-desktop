package xyz.gobliggg.gost.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import xyz.gobliggg.gost.ui.theme.GostTheme
import kotlin.test.assertEquals

class SaaSInputsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSaaSTextFieldInput() {
        var text by mutableStateOf("")

        composeTestRule.setContent {
            GostTheme {
                SaaSTextField(
                    label = "Username",
                    value = text,
                    onValueChange = { text = it },
                    placeholder = "Enter username",
                )
            }
        }

        // Verify label and placeholder
        composeTestRule.onNodeWithText("Username").assertExists()
        composeTestRule.onNodeWithText("Enter username").assertExists()

        // Type something
        composeTestRule.onNodeWithText("Enter username").performTextInput("alice")

        // Verify state update
        assertEquals("alice", text)
    }

    @Test
    fun testHandlerAndListenerDropdownSelection() {
        var handler by mutableStateOf("http")
        var listener by mutableStateOf("tcp")

        composeTestRule.setContent {
            GostTheme {
                androidx.compose.foundation.layout.Column {
                    DropdownField(
                        value = handler,
                        options = listOf("http", "socks5", "tcp"),
                        onSelect = { handler = it },
                        searchable = true,
                        contentDescription = "Handler type",
                    )
                    DropdownField(
                        value = listener,
                        options = listOf("tcp", "tls", "grpc"),
                        onSelect = { listener = it },
                        searchable = true,
                        contentDescription = "Listener type",
                    )
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Handler type").performClick()
        composeTestRule.onNodeWithText("socks5").performClick()
        assertEquals("socks5", handler)

        composeTestRule.onNodeWithContentDescription("Listener type").performClick()
        composeTestRule.onNodeWithText("tls").performClick()
        assertEquals("tls", listener)
    }
}
