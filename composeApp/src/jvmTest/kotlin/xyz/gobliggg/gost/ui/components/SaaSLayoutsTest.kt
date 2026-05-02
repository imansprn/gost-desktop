package xyz.gobliggg.gost.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import xyz.gobliggg.gost.ui.theme.GostTheme

class SaaSLayoutsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSaaSScreenHeaderDisplay() {
        composeTestRule.setContent {
            GostTheme {
                SaaSScreenHeader(
                    title = "Main Title",
                    superTitle = "SUPER TITLE",
                    subtitle = "This is a subtitle",
                )
            }
        }

        composeTestRule.onNodeWithText("Main Title").assertExists()
        composeTestRule.onNodeWithText("SUPER TITLE").assertExists()
        composeTestRule.onNodeWithText("This is a subtitle").assertExists()
    }

    @Test
    fun testSaaSAppBackground() {
        composeTestRule.setContent {
            GostTheme {
                SaaSAppBackground {
                    Text("Background Content")
                }
            }
        }
        composeTestRule.onNodeWithText("Background Content").assertExists()
    }

    @Test
    fun testSaaSDialog() {
        var dismissed = false
        composeTestRule.setContent {
            GostTheme {
                SaaSDialog(
                    title = "Test Dialog",
                    onDismissRequest = { dismissed = true },
                ) {
                    Text("Dialog Body")
                }
            }
        }

        composeTestRule.onNodeWithText("Test Dialog").assertExists()
        composeTestRule.onNodeWithText("Dialog Body").assertExists()

        // Find and click close button (Icon button with "Close" content description)
        composeTestRule.onNodeWithContentDescription("Close").performClick()
        kotlin.test.assertTrue(dismissed)
    }

    @Test
    fun testSaaSToggleGroupSelection() {
        var selected by androidx.compose.runtime.mutableStateOf("A")
        val options = listOf("A", "B", "C")

        composeTestRule.setContent {
            GostTheme {
                SaaSToggleGroup(
                    options = options,
                    selectedOption = selected,
                    onOptionSelected = { selected = it },
                )
            }
        }

        // Verify "A" is present
        composeTestRule.onNodeWithText("A").assertExists()

        // Click "B"
        composeTestRule.onNodeWithText("B").performClick()

        // Verify state update
        kotlin.test.assertEquals("B", selected)
    }
}
