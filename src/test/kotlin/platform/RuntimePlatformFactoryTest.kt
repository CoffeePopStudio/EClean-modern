package platform

import kotlin.test.Test
import kotlin.test.assertEquals
import top.e404.eclean.platform.runtime.RuntimePlatformFactory

class RuntimePlatformFactoryTest {
    @Test
    fun `factory returns folia or paper platform by flag`() {
        assertEquals("folia", RuntimePlatformFactory.create(true).id)
        assertEquals("paper", RuntimePlatformFactory.create(false).id)
    }
}
