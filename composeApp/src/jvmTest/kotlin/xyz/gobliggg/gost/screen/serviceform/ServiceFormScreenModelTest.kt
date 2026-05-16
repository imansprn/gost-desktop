package xyz.gobliggg.gost.screen.serviceform



import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import xyz.gobliggg.gost.api.dto.ChainDto
import xyz.gobliggg.gost.data.ConfigBuilder
import xyz.gobliggg.gost.data.ProcessManager
import xyz.gobliggg.gost.data.ServiceRegistry
import xyz.gobliggg.gost.data.ServiceWizardDraftData
import xyz.gobliggg.gost.data.ServiceWizardDraftStore
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceFormScreenModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var draftStore: ServiceWizardDraftStore
    private lateinit var registry: ServiceRegistry
    private lateinit var configBuilder: ConfigBuilder
    private lateinit var processManager: ProcessManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        draftStore = mockk(relaxed = true)
        registry = mockk(relaxed = true)
        configBuilder = mockk(relaxed = true)
        processManager = mockk(relaxed = true)
        
        every { draftStore.load() } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test basic state updates`() = runTest(testDispatcher) {
        val model = ServiceFormScreenModel(null, draftStore, configBuilder, registry, processManager)
        testScheduler.advanceUntilIdle()

        assertFalse(model.state.value.isDirty)
        
        model.updateName("TestSvc")
        assertEquals("TestSvc", model.state.value.name)
        assertTrue(model.state.value.isDirty)

        model.updateAddr(":8080")
        assertEquals(":8080", model.state.value.addr)

        model.updateHandlerType("socks5")
        assertEquals("socks5", model.state.value.handlerType)

        model.updateListenerType("tls")
        assertEquals("tls", model.state.value.listenerType)

        model.updateAuthUsername("admin")
        assertEquals("admin", model.state.value.authUsername)

        model.updateAuthPassword("pass")
        assertEquals("pass", model.state.value.authPassword)

        model.onDispose()
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun `test forwarder rows`() = runTest(testDispatcher) {
        val model = ServiceFormScreenModel(null, draftStore, configBuilder, registry, processManager)
        testScheduler.advanceUntilIdle()

        model.addForwarderRow()
        assertEquals(1, model.state.value.forwarderNodes.size)

        model.updateForwarderName(0, "fwd1")
        assertEquals("fwd1", model.state.value.forwarderNodes[0].first)

        model.updateForwarderAddr(0, "1.1.1.1:80")
        assertEquals("1.1.1.1:80", model.state.value.forwarderNodes[0].second)

        model.removeForwarderRow(0)
        assertEquals(0, model.state.value.forwarderNodes.size)

        model.onDispose()
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun `test metadata rows`() = runTest(testDispatcher) {
        val model = ServiceFormScreenModel(null, draftStore, configBuilder, registry, processManager)
        testScheduler.advanceUntilIdle()

        model.addMetadataRow()
        assertEquals(1, model.state.value.metadata.size)

        model.updateMetadataKey(0, "key1")
        assertEquals("key1", model.state.value.metadata[0].first)

        model.updateMetadataValue(0, "val1")
        assertEquals("val1", model.state.value.metadata[0].second)

        model.removeMetadataRow(0)
        assertEquals(0, model.state.value.metadata.size)

        model.onDispose()
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun `test next and prev steps`() = runTest(testDispatcher) {
        val model = ServiceFormScreenModel(null, draftStore, configBuilder, registry, processManager)
        testScheduler.advanceUntilIdle()

        // Empty state, next step should fail
        assertFalse(model.nextStep())
        assertEquals(0, model.state.value.currentStep)
        assertEquals("Required, no spaces", model.state.value.nameError)

        model.updateName("Svc")
        model.updateAddr(":8080")
        assertTrue(model.nextStep())
        assertEquals(1, model.state.value.currentStep)

        model.prevStep()
        assertEquals(0, model.state.value.currentStep)

        model.onDispose()
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun `test buildPreviewJson and save`() = runTest(testDispatcher) {
        val model = ServiceFormScreenModel(null, draftStore, configBuilder, registry, processManager)
        testScheduler.advanceUntilIdle()

        model.updateName("MyTunnel")
        model.updateAddr(":8080")
        model.updateHandlerType("http")
        model.updateListenerType("tcp")
        model.addForwarderRow()
        model.updateForwarderName(0, "node1")
        model.updateForwarderAddr(0, "1.2.3.4:80")

        val json = model.buildPreviewJson()
        assertTrue(json.contains("MyTunnel"))
        assertTrue(json.contains(":8080"))
        assertTrue(json.contains("http"))
        assertTrue(json.contains("tcp"))
        assertTrue(json.contains("node1"))
        assertTrue(json.contains("1.2.3.4:80"))

        var saved = false
        model.save { saved = true }
        assertTrue(saved)
        assertFalse(model.state.value.isSubmitting)

        model.onDispose()
        testScheduler.advanceUntilIdle()
    }



    @Test
    fun `test createChainFromWizard`() = runTest(testDispatcher) {
        val model = ServiceFormScreenModel(null, draftStore, configBuilder, registry, processManager)
        testScheduler.advanceUntilIdle()

        var doneMsg: String? = "not_called"
        model.createChainFromWizard(ChainDto(name = "NewChain")) {
            doneMsg = it
        }
        
        io.mockk.verify { configBuilder.saveTemplate("chains", "NewChain", any()) }
        assertEquals(null, doneMsg)
        assertEquals("NewChain", model.state.value.chainRef)

        model.onDispose()
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun `test loadService on edit mode`() = runTest(testDispatcher) {
        val mockConfigJson = """
            {
              "services": [
                {
                  "name": "EditSvc",
                  "addr": ":8888",
                  "handler": { "type": "http" },
                  "listener": { "type": "tcp" }
                }
              ]
            }
        """.trimIndent()
        every { configBuilder.readServiceConfig("EditSvc") } returns mockConfigJson

        val model = ServiceFormScreenModel("EditSvc", draftStore, configBuilder, registry, processManager)
        testScheduler.advanceUntilIdle()

        assertEquals("EditSvc", model.state.value.name)
        assertEquals(":8888", model.state.value.addr)
        assertEquals("http", model.state.value.handlerType)
        assertTrue(model.state.value.isEditMode)

        // Save in edit mode
        var saved = false
        model.save { saved = true }
        assertTrue(saved)
        io.mockk.verify { processManager.stopService("EditSvc") } // stops old process

        model.onDispose()
        testScheduler.advanceUntilIdle()
    }
}
