package xyz.gobliggg.gost.data

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProcessManagerTest {
    private lateinit var registry: ServiceRegistry
    private lateinit var manager: ProcessManager

    @Before
    fun setup() {
        registry = mockk(relaxed = true)
        manager = ProcessManager(registry)

        // Initialize AppState with a temporary repository to avoid UninitializedPropertyException
        val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "gost-test-pm-${java.util.UUID.randomUUID()}")
        tempDir.mkdirs()
        kotlinx.coroutines.runBlocking {
            AppState.initialize(LocalConfigRepository(tempDir))
        }
    }

    @Test
    fun `test start service is blocked while engine is stopped`() {
        AppState.updateSettings { it.copy(gostRuntime = it.gostRuntime.copy(binaryPath = "")) }
        every { registry.getService("s1") } returns ServiceEntity("s1", "S1", configPath = "/tmp/c1")

        manager.startService("s1")

        verify {
            registry.updateServiceStatus(
                "s1",
                ServiceStatus.IDLE,
                errorMessage = "Engine is stopped",
            )
        }
    }

    @Test
    fun `test engine gate prevents launching a non-existent binary`() {
        AppState.updateSettings { it.copy(gostRuntime = it.gostRuntime.copy(binaryPath = "/tmp/non-existent-binary")) }
        every { registry.getService("s1") } returns ServiceEntity("s1", "S1", configPath = "/tmp/c1")

        manager.startService("s1")

        verify {
            registry.updateServiceStatus(
                "s1",
                ServiceStatus.IDLE,
                errorMessage = "Engine is stopped",
            )
        }
    }

    @Test
    fun `test log event data class`() {
        val event = LogEvent("s1", "hello")
        assertEquals("s1", event.serviceId)
        assertEquals("hello", event.text)
        assertTrue(event.timestamp > 0)
    }

    @Test
    fun `test stop and stopAll`() {
        manager.stopAll() // should not throw
        manager.stopService("non-existent") // should not throw
    }

    @Test
    fun `test restart service respects stopped engine`() {
        every { registry.getService("s1") } returns ServiceEntity("s1", "S1", configPath = "/tmp/c1")

        manager.restartService("s1")

        verify(atLeast = 1) { registry.getService("s1") }
        verify {
            registry.updateServiceStatus(
                "s1",
                ServiceStatus.IDLE,
                errorMessage = "Engine is stopped",
            )
        }
    }
}
