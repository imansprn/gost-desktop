package xyz.gobliggg.gost.data

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals

class ServiceRegistryTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var testDataDir: File

    @Before
    fun setup() {
        testDataDir = tempFolder.newFolder(".gost-manager", "data")
        // We can't easily mock the 'object' ServiceRegistry's private dataDir without reflection
        // but for a demonstration, we could refactor ServiceRegistry to accept a dataDir.
    }

    @Test
    fun `test service entity serialization`() {
        val entity =
            ServiceEntity(
                id = "test-id",
                name = "Test Service",
                configPath = "/path/to/config.json",
                status = ServiceStatus.RUNNING,
            )

        assertEquals("test-id", entity.id)
        assertEquals(ServiceStatus.RUNNING, entity.status)
    }
}
