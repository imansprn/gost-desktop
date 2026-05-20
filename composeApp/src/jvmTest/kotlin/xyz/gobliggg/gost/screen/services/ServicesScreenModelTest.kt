package xyz.gobliggg.gost.screen.services



import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import xyz.gobliggg.gost.data.ConfigBuilder
import xyz.gobliggg.gost.data.ProcessManager
import xyz.gobliggg.gost.data.ServiceEntity
import xyz.gobliggg.gost.data.ServiceRegistry
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ServicesScreenModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var registry: ServiceRegistry
    private lateinit var configBuilder: ConfigBuilder
    private lateinit var processManager: ProcessManager
    private lateinit var mockServices: MutableStateFlow<List<ServiceEntity>>

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        registry = mockk(relaxed = true)
        configBuilder = mockk(relaxed = true)
        processManager = mockk(relaxed = true)
        
        mockServices = MutableStateFlow(emptyList())
        every { registry.services } returns mockServices
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }



    @Test
    fun `test process actions`() = runTest(testDispatcher) {
        val model = ServicesScreenModel(registry, configBuilder, processManager)
        
        model.startService("s1")
        verify { processManager.startService("s1") }
        
        model.stopService("s1")
        verify { processManager.stopService("s1") }
        
        model.restartService("s1")
        verify { processManager.restartService("s1") }
        
        model.onDispose()
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun `test delete service`() = runTest(testDispatcher) {
        val model = ServicesScreenModel(registry, configBuilder, processManager)
        
        model.deleteService("s1")
        
        verify { processManager.stopService("s1") }
        verify { configBuilder.deleteServiceConfig("s1") }
        verify { registry.removeService("s1") }
        
        model.onDispose()
        testScheduler.advanceUntilIdle()
    }
}
