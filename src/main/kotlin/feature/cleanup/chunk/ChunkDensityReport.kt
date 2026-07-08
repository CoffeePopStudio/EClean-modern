package top.e404.eclean.feature.cleanup.chunk

import top.e404.eclean.platform.execution.ChunkRef
import java.util.UUID

data class ChunkDensityDecision(
    val chunk: ChunkRef,
    val entityIdsToRemove: List<UUID>,
    val denseEntries: List<ChunkDensityEntry>,
)

data class ChunkDensityChunkReport(
    val cleaned: Int,
    val denseEntries: List<ChunkDensityEntry>,
)
