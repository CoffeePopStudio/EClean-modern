package top.e404.eclean.config

object ModernConfig {
    val cleanupDuration get() = Config.config.duration
    val drop get() = Config.config.drop
    val living get() = Config.config.living
    val chunkDensity get() = Config.config.chunk
    val trashcan get() = Config.config.trashcan
}
