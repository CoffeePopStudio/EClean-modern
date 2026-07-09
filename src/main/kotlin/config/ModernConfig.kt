package top.e404.eclean.config

object ModernConfig {
    val global get() = Config.current.global
    val cleanup get() = Config.current.cleanup
    val drop get() = Config.current.drop
    val living get() = Config.current.living
    val chunkDensity get() = Config.current.chunkDensity
    val trashcan get() = Config.current.trashcan
    val perWorld get() = Config.current.perWorld

    val cleanupDuration get() = cleanup.intervalSeconds
}
