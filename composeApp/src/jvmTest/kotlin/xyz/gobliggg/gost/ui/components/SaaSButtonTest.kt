package xyz.gobliggg.gost.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import xyz.gobliggg.gost.ui.theme.GostTheme
import kotlin.test.assertTrue

class SaaSButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSaaSButtonShowsTextAndClicks() {
        var clicked = false
        
        composeTestRule.setContent {
            GostTheme {
                SaaSButton(
                    text = "Click Me",
                    onClick = { clicked = true }
                )
            }
        }

        // Verify text is displayed
        composeTestRule.onNodeWithText("Click Me").assertExists()

        // Perform click
        composeTestRule.onNodeWithText("Click Me").performClick()

        // Verify click handler was called
        assertTrue(clicked, "Button should have been clicked")
    }
}
