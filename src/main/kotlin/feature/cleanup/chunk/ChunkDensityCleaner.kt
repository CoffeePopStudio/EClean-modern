package top.e404.eclean.feature.cleanup.chunk

import org.bukkit.Chunk

class ChunkDensityCleaner {
    fun clean(
        chunk: Chunk,
        decision: ChunkDensityDecision,
    ): ChunkDensityChunkReport {
        if (decision.entityIdsToRemove.isEmpty()) {
            return ChunkDensityChunkReport(
                cleaned = 0,
                denseEntries = decision.denseEntries,
            )
        }

        val ids = decision.entityIdsToRemove.toHashSet()
        var cleaned = 0
        chunk.entities.forEach { entity ->
            if (entity.uniqueId !in ids) return@forEach
            entity.remove()
            cleaned++
        }
        return ChunkDensityChunkReport(
            cleaned = cleaned,
            denseEntries = decision.denseEntries,
        )
    }
}
