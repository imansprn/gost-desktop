package xyz.gobliggg.gost.screen.logs
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.cancel


import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import xyz.gobliggg.gost.data.LogEvent
import xyz.gobliggg.gost.data.ProcessManager
import xyz.gobliggg.gost.data.ServiceEntity
import xyz.gobliggg.gost.data.ServiceRegistry
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LogsScreenModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var registry: ServiceRegistry
    private lateinit var processManager: ProcessManager
    private lateinit var mockLogs: MutableSharedFlow<LogEvent>
    private lateinit var mockServices: MutableStateFlow<List<ServiceEntity>>

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        registry = mockk(relaxed = true)
        processManager = mockk(relaxed = true)
        
        mockLogs = MutableSharedFlow()
        mockServices = MutableStateFlow(emptyList())
        
        every { processManager.logs } returns mockLogs
        every { registry.services } returns mockServices
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state and services load`() = runTest(testDispatcher) {
        val model = LogsScreenModel(registry, processManager)
        
        mockServices.value = listOf(ServiceEntity("svc1", "Service 1", configPath = ""))
        testScheduler.advanceUntilIdle()
        
        assertEquals(listOf("svc1"), model.state.value.availableServices)
        model.onDispose()
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun `test log parsing and filtering`() = runTest(testDispatcher) {
        val model = LogsScreenModel(registry, processManager)
        testScheduler.advanceUntilIdle()

        // Emit some logs
        // Structured GOST log
        mockLogs.emit(LogEvent("svc1", "2023-10-01 12:00:00.000 [info] Server started"))
        // Unstructured with brackets
        mockLogs.emit(LogEvent("svc2", "Some random text [ERROR] something failed"))
        // Completely unstructured
        mockLogs.emit(LogEvent("svc1", "Just a normal line without level"))
        // Edge cases
        mockLogs.emit(LogEvent("svc1", "2023-10-01 12:00:00.000 [trace] Detailed trace"))

        testScheduler.advanceUntilIdle()

        val entries = model.state.value.entries
        assertEquals(4, entries.size)

        // Check structured log
        assertEquals("2023-10-01 12:00:00.000", entries[0].timestamp)
        assertEquals("info", entries[0].level)
        assertEquals("Server started", entries[0].message)

        // Check unstructured with brackets
        assertEquals("error", entries[1].level)
        assertEquals("Some random text  something failed", entries[1].message)

        // Check unstructured
        assertEquals("info", entries[2].level) // fallback to info
        assertEquals("Just a normal line without level", entries[2].message)

        // Check trace mapping to debug
        assertEquals("debug", entries[3].level)

        // Test filtering
        model.toggleLevel("info") // turn off info
        testScheduler.advanceUntilIdle()
        assertEquals(2, model.state.value.filteredEntries.size) // Only error and debug remain

        model.toggleLevel("info") // turn back on
        model.setServiceFilter("svc1")
        testScheduler.advanceUntilIdle()
        assertEquals(3, model.state.value.filteredEntries.size) // 3 logs from svc1

        model.setSearch("failed")
        testScheduler.advanceUntilIdle()
        assertEquals(0, model.state.value.filteredEntries.size) // svc1 doesn't have "failed"
        
        model.setServiceFilter(null)
        testScheduler.advanceUntilIdle()
        assertEquals(1, model.state.value.filteredEntries.size) // svc2 has "failed"
        
        model.onDispose()
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun `test auto scroll and clear`() = runTest(testDispatcher) {
        val model = LogsScreenModel(registry, processManager)
        testScheduler.advanceUntilIdle()
        
        assertTrue(model.state.value.autoScroll)
        model.toggleAutoScroll()
        assertFalse(model.state.value.autoScroll)
        
        mockLogs.emit(LogEvent("s1", "log"))
        testScheduler.advanceUntilIdle()
        assertEquals(1, model.state.value.entries.size)
        
        model.clear()
        assertEquals(0, model.state.value.entries.size)
        
        model.onDispose()
        testScheduler.advanceUntilIdle()
    }
}
