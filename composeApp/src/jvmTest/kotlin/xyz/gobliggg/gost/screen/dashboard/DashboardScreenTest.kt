package xyz.gobliggg.gost.screen.dashboard

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import xyz.gobliggg.gost.ui.theme.GostTheme

class DashboardScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testDashboardScreenRenders() {
        composeTestRule.setContent {
            GostTheme {
                DashboardScreen().Content()
            }
        }

        // Check header
        composeTestRule.onNodeWithText("Dashboard").assertExists()
        composeTestRule.onNodeWithText("OVERVIEW").assertExists()

        // Check welcome message
        composeTestRule.onNodeWithText("GOST tunnel wrapper is online.").assertExists()
    }
}
