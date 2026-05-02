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
        val initial = AppState.settings.value.sidebarCollapsed
        AppState.updateSettings { it.copy(sidebarCollapsed = !initial) }
        assertEquals(!initial, AppState.settings.value.sidebarCollapsed)
        assertEquals(ThemeMode.DARK, AppState.settings.value.theme)
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
    fun `test theme is always dark`() {
        assertTrue(AppState.isDarkTheme)
        AppState.updateSettings { it.copy(theme = ThemeMode.LIGHT) }
        assertTrue(AppState.isDarkTheme)
        assertEquals(ThemeMode.DARK, AppState.settings.value.theme)
    }
}
