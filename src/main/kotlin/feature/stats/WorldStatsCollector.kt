package top.e404.eclean.feature.stats

import org.bukkit.Chunk
import org.bukkit.entity.EntityType

class WorldStatsCollector {
    fun collectFromChunk(chunk: Chunk): ChunkSnapshot = ChunkSnapshot(
        entityCounts = chunk.entities
            .groupingBy { it.type }
            .eachCount(),
    )

    fun countEntityTypeInChunk(chunk: Chunk, type: EntityType): Int =
        chunk.entities.count { it.type == type }
}

data class ChunkSnapshot(
    val entityCounts: Map<EntityType, Int>,
) {
    companion object {
        fun merge(
            partials: List<ChunkSnapshot>,
            forceLoadedCount: Int,
        ): WorldStatsResult {
            val merged = mutableMapOf<EntityType, Int>()
            partials.forEach { snap ->
                snap.entityCounts.forEach { (type, count) ->
                    merged[type] = (merged[type] ?: 0) + count
                }
            }
            return WorldStatsResult(
                entityCounts = merged,
                loadedChunks = partials.size,
                forceLoadedChunks = forceLoadedCount,
            )
        }
    }
}
