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

    @Test
    fun `test disconnect`() {
        AppState.updateSettings { it.copy(gostRuntime = it.gostRuntime.copy(binaryPath = "/invalid/path")) }
        AppState.disconnect()
        assertEquals("", AppState.settings.value.gostRuntime.binaryPath)
    }

    @Test
    fun `test profiles stubs`() {
        assertTrue(AppState.getProfiles().isEmpty())
        AppState.deleteProfile("any") // should not throw
    }

    @Test
    fun `test theme stub`() {
        assertTrue(AppState.isDarkTheme)
    }

    @Test
    fun `test connection profile data class`() {
        val profile = AppState.ConnectionProfile("1", "test", "http://host")
        assertEquals("1", profile.id)
        assertEquals("test", profile.name)
        assertEquals("http://host", profile.baseUrl)
        
        val copy = profile.copy(id = "2")
        assertEquals("2", copy.id)
        assertEquals(profile, profile)
        assertTrue(profile.hashCode() != 0)
        assertTrue(profile.toString().contains("ConnectionProfile"))
    }
}
