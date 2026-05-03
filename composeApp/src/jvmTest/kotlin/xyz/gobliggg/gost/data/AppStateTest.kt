package xyz.gobliggg.gost.data

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class AppStateTest {
    private lateinit var tempDir: java.io.File

    @Before
    fun setup() {
        tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "gost-test-${java.util.UUID.randomUUID()}")
        tempDir.mkdirs()
        val testRepo = LocalConfigRepository(tempDir)
        runBlocking { AppState.initialize(testRepo) }
    }

    @Test
    fun `test settings update`() {
        val initial = AppState.settings.value.sidebarCollapsed
        AppState.updateSettings { it.copy(sidebarCollapsed = !initial) }
        assertEquals(!initial, AppState.settings.value.sidebarCollapsed)
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

}
