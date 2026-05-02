package xyz.gobliggg.gost.screen.services

import org.junit.Test
import kotlin.test.assertNotNull

class ServicesScreenModelTest {
    @Test
    fun `test state initialization`() {
        val model = ServicesScreenModel()
        assertNotNull(model.state.value.services)
    }
}
