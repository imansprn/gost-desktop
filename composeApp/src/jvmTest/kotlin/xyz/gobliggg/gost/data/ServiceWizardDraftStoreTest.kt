package xyz.gobliggg.gost.data

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ServiceWizardDraftStoreTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `test draft save and load`() {
        val draft =
            ServiceWizardDraftData(
                name = "Draft Tunnel",
                addr = ":8080",
                currentStep = 1,
            )

        ServiceWizardDraftStore.save(draft)

        val loaded = ServiceWizardDraftStore.load()
        assertEquals("Draft Tunnel", loaded?.name)
        assertEquals(1, loaded?.currentStep)

        ServiceWizardDraftStore.clear()
        assertNull(ServiceWizardDraftStore.load())
    }
}
