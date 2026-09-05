package top.e404.eclean.feature.stats

import org.bukkit.Chunk

class WorldStatsCollector {
    fun collectFromChunk(chunk: Chunk): ChunkSnapshot = ChunkSnapshot(
        entityCounts = chunk.entities
            .groupingBy { it.type.name }
            .eachCount(),
    )

    fun countEntityTypeInChunk(chunk: Chunk, type: String): Int =
        chunk.entities.count { it.type.name == type }
}
