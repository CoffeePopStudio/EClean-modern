package top.e404.eclean.feature.cleanup.chunk

data class ChunkDensityRule(
    val cleanNamed: Boolean,
    val cleanLeashed: Boolean,
    val cleanMounted: Boolean,
    val threshold: Int,
)
