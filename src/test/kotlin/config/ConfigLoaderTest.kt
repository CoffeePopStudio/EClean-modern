package top.e404.eclean.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfigLoaderTest {
    @Test
    fun `bundle loads all config files`() {
        val loader = ConfigLoader()
        val bundle = loader.loadFromText(
            globalText = "debug: true\nupdateCheck: false",
            cleanupText = "intervalSeconds: 120",
            dropText = "enabled: true",
            livingText = "enabled: true",
            chunkDensityText = "enabled: true",
            trashcanText = "enabled: true"
        )

        assertTrue(bundle.global.debug)
        assertEquals(120, bundle.cleanup.intervalSeconds)
        assertTrue(bundle.drop.enabled)
    }
}
