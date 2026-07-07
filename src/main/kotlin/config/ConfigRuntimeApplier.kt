package top.e404.eclean.config

import top.e404.eclean.clean.Clean
import top.e404.eclean.clean.Trashcan

object ConfigRuntimeApplier {
    fun apply(bundle: ConfigBundle) {
        Clean.schedule()
        Trashcan.schedule()
    }
}
