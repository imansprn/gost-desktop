package xyz.gobliggg.gost.data

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import xyz.gobliggg.gost.model.AppSettings
import xyz.gobliggg.gost.model.GostRuntimeConfig
import java.io.File
import kotlin.test.assertEquals

class LocalConfigRepositoryTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `test load non-existent config returns default`() {
        val repo = LocalConfigRepository(tempFolder.newFolder())
        val config = repo.load()
        assertEquals(LocalConfig(), config)
    }

    @Test
    fun `test save and load config`() {
        val baseDir = tempFolder.newFolder()
        val repo = LocalConfigRepository(baseDir)

        val customSettings =
            AppSettings(
                gostRuntime = GostRuntimeConfig(binaryPath = "/custom/path"),
            )
        val config = LocalConfig(settings = customSettings)

        repo.save(config)

        val loaded = repo.load()
        assertEquals("/custom/path", loaded.settings.gostRuntime.binaryPath)
    }

    @Test
    fun `test load corrupted config returns default`() {
        val baseDir = tempFolder.newFolder()
        val configFile = File(baseDir, "config.json")
        configFile.writeText("{ corrupted: true")

        val repo = LocalConfigRepository(baseDir)
        val config = repo.load()

        assertEquals(LocalConfig(), config)
    }
}
