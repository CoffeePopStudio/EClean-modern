package top.e404.eclean.config
import top.e404.eclean.PL


object ConfigRuntimeApplier {
    fun apply(bundle: ConfigBundle, changes: Set<ConfigSection> = ConfigSection.entries.toSet()) {
        if (changes.isEmpty()) return
        if (ConfigSection.CLEANUP in changes || ConfigSection.PER_WORLD in changes) {
            PL.services.cleanupTickService.stop()
            PL.services.cleanupTickService.start()
        }
        if (ConfigSection.TRASHCAN in changes) {
            PL.services.trashcanTicker.restart()
        }
    }
}
