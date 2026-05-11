package xyz.gobliggg.gost.data

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ServiceWizardDraftStoreTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var draftStore: ServiceWizardDraftStore
    private lateinit var draftFile: File

    @Before
    fun setup() {
        val dir = tempFolder.newFolder("gost-manager")
        draftFile = File(dir, "service-wizard-draft.json")
        draftStore = ServiceWizardDraftStore(draftFile.absolutePath)
    }

    @After
    fun cleanup() {
        // TemporaryFolder handles cleanup
    }

    @Test
    fun `test draft save and load`() {
        val draft =
            ServiceWizardDraftData(
                name = "Draft Tunnel",
                addr = ":8080",
                currentStep = 1,
            )

        draftStore.save(draft)

        val loaded = draftStore.load()
        assertEquals("Draft Tunnel", loaded?.name)
        assertEquals(1, loaded?.currentStep)

        draftStore.clear()
        assertNull(draftStore.load())
    }

    @Test
    fun `test empty store returns null`() {
        assertNull(draftStore.load())
    }

    @Test
    fun `test clear on empty store does not throw`() {
        draftStore.clear() // should not throw
    }

    @Test
    fun `test roundtrip preserves all fields`() {
        val draft =
            ServiceWizardDraftData(
                currentStep = 2,
                name = "My Tunnel",
                addr = "0.0.0.0:8080",
                handlerType = "socks5",
                listenerType = "tcp",
                authUsername = "admin",
                authPassword = "secret",
                chainRef = "my-chain",
                autherRef = "my-auther",
                bypassRef = "my-bypass",
                admissionRef = "my-admission",
                limiterRef = "my-limiter",
                forwarderNodes = listOf(ForwarderDraftRow("node1", "1.2.3.4:1080")),
                tlsCertFile = "/path/to/cert.pem",
                tlsKeyFile = "/path/to/key.pem",
                tlsCaFile = "/path/to/ca.pem",
                metadata = mapOf("key1" to "value1", "key2" to "value2"),
            )

        draftStore.save(draft)
        val loaded = draftStore.load()
        assertEquals(draft, loaded)
    }
}
