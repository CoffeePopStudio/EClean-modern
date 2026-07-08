package top.e404.eclean.feature.stats

import org.bukkit.Chunk
import org.bukkit.entity.EntityType

class WorldStatsCollector {
    fun collectFromChunk(chunk: Chunk): ChunkSnapshot = ChunkSnapshot(
        entityCounts = chunk.entities
            .groupingBy { it.type }
            .eachCount()
            .mapKeys { it.key },
        forceLoaded = chunk.isForceLoaded,
    )

    fun countEntityTypeInChunk(chunk: Chunk, type: EntityType): Int =
        chunk.entities.count { it.type == type }
}

data class ChunkSnapshot(
    val entityCounts: Map<EntityType, Int>,
    val forceLoaded: Boolean,
) {
    companion object {
        fun merge(partials: List<ChunkSnapshot>): WorldStatsResult {
            val merged = mutableMapOf<EntityType, Int>()
            var forceLoaded = 0
            partials.forEach { snap ->
                snap.entityCounts.forEach { (type, count) ->
                    merged[type] = (merged[type] ?: 0) + count
                }
                if (snap.forceLoaded) forceLoaded++
            }
            return WorldStatsResult(
                entityCounts = merged,
                loadedChunks = partials.size,
                forceLoadedChunks = forceLoaded,
            )
        }
    }
}
