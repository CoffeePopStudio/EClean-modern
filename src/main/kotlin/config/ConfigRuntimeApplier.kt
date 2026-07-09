package top.e404.eclean.config

import top.e404.eclean.app.RuntimeServices

object ConfigRuntimeApplier {
    fun apply(bundle: ConfigBundle, changes: Set<ConfigSection> = ConfigSection.entries.toSet()) {
        if (changes.isEmpty()) return
        if (ConfigSection.CLEANUP in changes || ConfigSection.PER_WORLD in changes) {
            RuntimeServices.cleanupTickService.stop()
            RuntimeServices.cleanupTickService.start()
        }
        if (ConfigSection.TRASHCAN in changes) {
            RuntimeServices.trashcanTicker.restart()
        }
    }
}
