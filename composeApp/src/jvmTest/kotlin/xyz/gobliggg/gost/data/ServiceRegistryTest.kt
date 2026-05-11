package xyz.gobliggg.gost.data

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ServiceRegistryTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var serviceRegistry: ServiceRegistry
    private lateinit var testDataDir: File

    @Before
    fun setup() {
        testDataDir = tempFolder.newFolder("gost-manager", "data")
        serviceRegistry = ServiceRegistry(testDataDir)
        serviceRegistry.initialize()
    }

    @After
    fun cleanup() {
        // TemporaryFolder handles cleanup
    }

    @Test
    fun `test add and retrieve service`() {
        val entity =
            ServiceEntity(
                id = "test-id",
                name = "Test Service",
                configPath = "/path/to/config.json",
                status = ServiceStatus.RUNNING,
            )

        assertEquals("test-id", entity.id)
        assertEquals(ServiceStatus.RUNNING, entity.status)

        serviceRegistry.addOrUpdateService(entity)

        val loaded = serviceRegistry.getService("test-id")
        assertNotNull(loaded)
        assertEquals("Test Service", loaded.name)
    }

    @Test
    fun `test update service status`() {
        serviceRegistry.addOrUpdateService(
            ServiceEntity(
                id = "svc-1",
                name = "Svc 1",
                configPath = "/tmp/test.json",
                status = ServiceStatus.IDLE,
            ),
        )

        serviceRegistry.updateServiceStatus("svc-1", ServiceStatus.RUNNING, pid = 12345L)

        val updated = serviceRegistry.getService("svc-1")
        assertNotNull(updated)
        assertEquals(ServiceStatus.RUNNING, updated.status)
        assertEquals(12345L, updated.pid)
    }

    @Test
    fun `test remove service`() {
        serviceRegistry.addOrUpdateService(
            ServiceEntity(
                id = "to-remove",
                name = "Remove Me",
                configPath = "/tmp/test.json",
            ),
        )

        assertNotNull(serviceRegistry.getService("to-remove"))
        serviceRegistry.removeService("to-remove")
        assertNull(serviceRegistry.getService("to-remove"))
    }

    @Test
    fun `test services flow reflects added entities`() {
        serviceRegistry.addOrUpdateService(
            ServiceEntity(id = "a", name = "A", configPath = "/tmp/a.json"),
        )
        serviceRegistry.addOrUpdateService(
            ServiceEntity(id = "b", name = "B", configPath = "/tmp/b.json"),
        )

        val services = serviceRegistry.services.value
        assertEquals(2, services.size)
        assertTrue(services.any { it.id == "a" })
        assertTrue(services.any { it.id == "b" })
    }

    @Test
    fun `test services are persisted to disk`() {
        serviceRegistry.addOrUpdateService(
            ServiceEntity(
                id = "persist-id",
                name = "Persist Test",
                configPath = "/path/to/config.json",
                status = ServiceStatus.IDLE,
            ),
        )

        // Verify the services file was created
        val servicesFile = File(testDataDir, "services.json")
        assertTrue(servicesFile.exists())

        // Create a new registry reading from the same dir and check the data loads
        val secondRegistry = ServiceRegistry(testDataDir)
        secondRegistry.initialize()

        val loaded = secondRegistry.getService("persist-id")
        assertNotNull(loaded)
        assertEquals("Persist Test", loaded.name)
    }
}
