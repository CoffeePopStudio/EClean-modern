package top.e404.eclean.feature.cleanup.chunk

import java.util.UUID

class ChunkDensityCleaner {
    fun clean(
        decision: ChunkDensityDecision,
        remover: (List<UUID>) -> Int,
    ): ChunkDensityChunkReport {
        val cleaned = if (decision.entityIdsToRemove.isNotEmpty()) {
            remover(decision.entityIdsToRemove)
        } else {
            0
        }
        return ChunkDensityChunkReport(
            cleaned = cleaned,
            denseEntries = decision.denseEntries,
        )
    }
}