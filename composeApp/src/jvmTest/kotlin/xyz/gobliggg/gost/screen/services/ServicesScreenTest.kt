package xyz.gobliggg.gost.screen.services
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.cancel


import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import xyz.gobliggg.gost.ui.theme.GostTheme

class ServicesScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testServicesScreenRenders() {
        composeTestRule.setContent {
            GostTheme {
                ServicesScreen().Content()
            }
        }

        // Check header
        composeTestRule.onNodeWithText("Tunnels").assertExists()
        composeTestRule.onNodeWithText("GOST RUNTIME").assertExists()

        // Check empty state (assuming no services)
        // If there's an empty state text, I should find it.
    }
}
