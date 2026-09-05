package top.e404.eclean.feature.cleanup.chunk

data class ChunkDensityChunkReport(
    val cleaned: Int,
    val denseEntries: List<ChunkDensityEntry>,
)
