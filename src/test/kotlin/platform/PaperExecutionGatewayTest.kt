package platform

import kotlin.test.Test
import kotlin.test.assertNotNull
import top.e404.eclean.platform.execution.PaperExecutionGateway

class PaperExecutionGatewayTest {
    @Test
    fun `paper gateway instantiates with no-arg constructor`() {
        val gateway = PaperExecutionGateway()

        assertNotNull(gateway)
    }
}
