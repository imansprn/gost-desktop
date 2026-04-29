package xyz.gobliggg.gost.data

import org.junit.Before
import org.junit.Test
import xyz.gobliggg.gost.model.ThemeMode
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppStateTest {

    @Before
    fun setup() {
        if (!AppState.isInitialized.value) {
            AppState.initialize()
        }
    }

    @Test
    fun `test settings update`() {
        // Since AppState is a singleton object, we test the public interface.
        // We might need to handle the fact that it's already initialized in some environments.
        
        val initialTheme = AppState.settings.value.theme
        val newTheme = if (initialTheme == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
        
        AppState.updateSettings { it.copy(theme = newTheme) }
        
        assertEquals(newTheme, AppState.settings.value.theme)
    }

    @Test
    fun `test pending shell route logic`() {
        AppState.setPendingShellRoute("dashboard")
        assertEquals("dashboard", AppState.takePendingShellRoute())
        assertNull(AppState.takePendingShellRoute(), "Route should be cleared after take")
    }

    @Test
    fun `test api error route normalization`() {
        AppState.setPendingShellRoute("api-error/404")
        assertNull(AppState.takePendingShellRoute(), "api-error routes should be normalized to null")
    }

    @Test
    fun `test isDarkTheme logic`() {
        AppState.updateSettings { it.copy(theme = ThemeMode.DARK) }
        assertTrue(AppState.isDarkTheme)
        
        AppState.updateSettings { it.copy(theme = ThemeMode.LIGHT) }
        kotlin.test.assertFalse(AppState.isDarkTheme)
    }
}
