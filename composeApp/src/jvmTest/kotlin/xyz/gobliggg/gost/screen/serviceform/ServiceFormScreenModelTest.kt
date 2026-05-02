package xyz.gobliggg.gost.screen.serviceform

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ServiceFormScreenModelTest {
    @Test
    fun `test state initialization in create mode`() {
        val model = ServiceFormScreenModel(editName = null)
        val state = model.state.value

        assertFalse(state.isEditMode)
        assertEquals(0, state.currentStep)
        assertEquals("http", state.handlerType)
    }

    @Test
    fun `test state initialization in edit mode`() {
        // This might fail if ConfigBuilder isn't mocked,
        // but it shows how to test the ScreenModel state
        val model = ServiceFormScreenModel(editName = "my-service")
        val state = model.state.value

        assertTrue(state.isEditMode)
    }
}
