package config

import kotlin.test.Test
import kotlin.test.assertEquals
import top.e404.eclean.config.ConfigBundle
import top.e404.eclean.config.ConfigRuntimeApplier
import top.e404.eclean.config.RuntimeConfigApplier

class ConfigRuntimeApplierTest {
    @Test
    fun `runtime applier delegates to current runtime lifecycle hook`() {
        val previous = ConfigRuntimeApplier.runtimeConfigApplier
        var applyCount = 0

        try {
            ConfigRuntimeApplier.runtimeConfigApplier = RuntimeConfigApplier {
                applyCount++
            }

            ConfigRuntimeApplier.apply(ConfigBundle())

            assertEquals(1, applyCount)
        } finally {
            ConfigRuntimeApplier.runtimeConfigApplier = previous
        }
    }
}
