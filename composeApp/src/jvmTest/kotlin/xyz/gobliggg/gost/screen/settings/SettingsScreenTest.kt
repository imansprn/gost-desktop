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
            GostTheme(darkTheme = true) {
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
    fun testSidebarDefaultToggle() {
        composeTestRule.setContent {
            GostTheme(darkTheme = true) {
                SettingsScreen().Content()
            }
        }

        val initial = AppState.settings.value.sidebarCollapsed
        composeTestRule.onNodeWithText("Collapse sidebar by default").performClick()
        kotlin.test.assertEquals(!initial, AppState.settings.value.sidebarCollapsed)
        kotlin.test.assertTrue(AppState.isDarkTheme)
    }
}
