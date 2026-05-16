package xyz.gobliggg.gost.screen.connection
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.cancel


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import xyz.gobliggg.gost.data.AppState
import xyz.gobliggg.gost.data.LocalConfigRepository
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionScreenModelTest {
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "gost-test-${java.util.UUID.randomUUID()}")
        tempDir.mkdirs()
        kotlinx.coroutines.runBlocking {
            AppState.initialize(LocalConfigRepository(tempDir))
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test state updates`() = runTest(testDispatcher) {
        val model = ConnectionScreenModel()
        
        // Initial state depends on AppState, wait for detection
        testScheduler.advanceUntilIdle()
        
        model.updateWorkingDirectory("/tmp/work")
        assertEquals("/tmp/work", model.state.value.workingDirectory)
        
        model.updateAutoStart(true)
        assertTrue(model.state.value.autoStart)
        
        model.updateBinaryPath("")
        assertEquals("Path is required", model.state.value.pathError)
        
        model.updateBinaryPath("/non-existent-path-12345")
        assertEquals("File does not exist or is not executable", model.state.value.pathError)
        
        // Use an executable that probably exists
        val lsPath = if (java.io.File("/bin/ls").exists()) "/bin/ls" else "C:\\Windows\\System32\\cmd.exe"
        model.updateBinaryPath(lsPath)
        assertNull(model.state.value.pathError)
    }

    @Test
    fun `test save and connect`() = runTest(testDispatcher) {
        val model = ConnectionScreenModel()
        testScheduler.advanceUntilIdle()
        
        var connected = false
        val lsPath = if (java.io.File("/bin/ls").exists()) "/bin/ls" else "C:\\Windows\\System32\\cmd.exe"
        model.updateBinaryPath(lsPath)
        
        model.saveAndConnect { connected = true }
        assertTrue(connected)
        assertEquals(lsPath, AppState.settings.value.gostRuntime.binaryPath)
    }
    
    @Test
    fun `test save and connect fails on invalid path`() = runTest(testDispatcher) {
        val model = ConnectionScreenModel()
        testScheduler.advanceUntilIdle()
        
        var connected = false
        model.updateBinaryPath("/non-existent-path-12345")
        
        model.saveAndConnect { connected = true }
        assertFalse(connected)
    }
}
