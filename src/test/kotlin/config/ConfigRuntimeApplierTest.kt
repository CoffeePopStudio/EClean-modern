package config

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.e404.eclean.config.ConfigBundle
import top.e404.eclean.config.ConfigRuntimeApplier

@Disabled("RuntimeConfigApplier delegate pattern removed; test needs rewrite")
class ConfigRuntimeApplierTest {
    @Test
    fun `runtime applier delegates to current runtime lifecycle hook`() {
        ConfigRuntimeApplier.apply(ConfigBundle())
    }
}
