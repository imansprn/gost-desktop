package xyz.gobliggg.gost.screen.logs

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import xyz.gobliggg.gost.ui.theme.GostTheme

class LogsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLogsScreenRenders() {
        composeTestRule.setContent {
            GostTheme {
                LogsScreen().Content()
            }
        }

        // Check header
        composeTestRule.onNodeWithText("Console Logs").assertExists()
        composeTestRule.onNodeWithText("REAL-TIME").assertExists()
        
        // Check controls
        composeTestRule.onNodeWithText("Clear").assertExists()
        composeTestRule.onNodeWithText("Pause").assertExists()
    }
}
