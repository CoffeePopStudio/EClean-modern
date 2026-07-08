package top.e404.eclean.config

import top.e404.eclean.app.RuntimeServices

fun interface RuntimeConfigApplier {
    fun applyRuntimeConfig()
}

object ConfigRuntimeApplier {
    internal var runtimeConfigApplier: RuntimeConfigApplier = RuntimeConfigApplier {
        RuntimeServices.applyRuntimeConfig()
    }

    fun apply(bundle: ConfigBundle) {
        runtimeConfigApplier.applyRuntimeConfig()
    }
}
