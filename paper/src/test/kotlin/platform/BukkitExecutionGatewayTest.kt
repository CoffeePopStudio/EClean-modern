package platform

import kotlin.test.Test
import kotlin.test.assertNotNull
import top.e404.eclean.platform.execution.BukkitExecutionGateway

class BukkitExecutionGatewayTest {
    @Test
    fun `gateway instantiates with no-arg constructor`() {
        val gateway = BukkitExecutionGateway()

        assertNotNull(gateway)
    }
}
