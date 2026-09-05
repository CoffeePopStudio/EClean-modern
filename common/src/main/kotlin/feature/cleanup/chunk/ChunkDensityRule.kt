package top.e404.eclean.feature.cleanup.chunk

import top.e404.eclean.config.model.ChunkDensityConfig

data class ChunkDensityRule(
    val cleanNamed: Boolean,
    val cleanLeashed: Boolean,
    val cleanMounted: Boolean,
    val alertThreshold: Int,
    val entityLimits: Map<Regex, Int>,
) {
    companion object {
        fun fromConfig(config: ChunkDensityConfig) = ChunkDensityRule(
            cleanNamed = config.settings.cleanNamed,
            cleanLeashed = config.settings.cleanLeashed,
            cleanMounted = config.settings.cleanMounted,
            alertThreshold = config.alertThreshold,
            entityLimits = config.entityLimits,
        )
    }
}
