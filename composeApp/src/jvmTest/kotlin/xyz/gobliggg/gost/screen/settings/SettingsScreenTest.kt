package xyz.gobliggg.gost.screen.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import xyz.gobliggg.gost.data.AppState
import xyz.gobliggg.gost.ui.theme.GostTheme

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        if (!AppState.isInitialized.value) {
            AppState.initialize()
        }
    }

    @Test
    fun testSettingsScreenRenders() {
        composeTestRule.setContent {
            GostTheme {
                SettingsScreen().Content()
            }
        }

        // Check header
        composeTestRule.onNodeWithText("Settings").assertExists()
        composeTestRule.onNodeWithText("PREFERENCES").assertExists()

        // Check sections (they are uppercased in SectionHeader)
        composeTestRule.onNodeWithText("APPEARANCE").assertExists()
        composeTestRule.onNodeWithText("GOST RUNTIME").assertExists()
    }

    @Test
    fun testThemeSelection() {
        composeTestRule.setContent {
            GostTheme {
                SettingsScreen().Content()
            }
        }

        // Toggle to Light theme
        composeTestRule.onNodeWithText("Light").performClick()
        
        // Verify state (AppState.isDarkTheme should be false if light selected)
        kotlin.test.assertFalse(AppState.isDarkTheme)
        
        // Toggle to Dark theme
        composeTestRule.onNodeWithText("Dark").performClick()
        kotlin.test.assertTrue(AppState.isDarkTheme)
    }
}
