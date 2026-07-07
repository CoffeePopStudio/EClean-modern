package top.e404.eclean.feature.cleanup.chunk

import org.bukkit.Chunk
import org.bukkit.entity.EntityType

data class ChunkDensityEntry(
    val chunk: Chunk,
    val entityType: EntityType,
    val amount: Int,
)

data class ChunkDensityResult(
    val cleaned: Int,
    val denseEntries: List<ChunkDensityEntry>,
)
