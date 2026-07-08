package top.e404.eclean.feature.cleanup.chunk

import org.bukkit.Chunk
import org.bukkit.World
import top.e404.eclean.PL
import top.e404.eclean.config.Config

class ChunkDensityScanner(
    private val planner: ChunkScanPlanner = ChunkScanPlanner(),
    private val snapshotter: ChunkEntitySnapshotter = ChunkEntitySnapshotter(),
    private val policy: ChunkDensityPolicy = ChunkDensityPolicy(),
    private val cleaner: ChunkDensityCleaner = ChunkDensityCleaner(),
) {
    fun cleanAllWorlds(): ChunkDensityResult {
        val rule = ChunkDensityRule.fromConfig(Config.current.chunkDensity)
        var cleaned = 0
        val dense = mutableListOf<ChunkDensityEntry>()
        planner.planWorlds().forEach { world ->
            planner.planChunks(world).forEach { chunk ->
                val result = cleanChunk(chunk, rule)
                cleaned += result.cleaned
                dense += result.denseEntries
            }
        }
        return ChunkDensityResult(cleaned = cleaned, denseEntries = dense)
    }

    fun cleanWorld(world: World): Int {
        val rule = ChunkDensityRule.fromConfig(Config.current.chunkDensity)
        return planner.planChunks(world).sumOf { cleanChunk(it, rule).cleaned }
    }

    fun scanDenseEntries(): List<ChunkDensityEntry> {
        val rule = ChunkDensityRule.fromConfig(Config.current.chunkDensity)
        return planner.planWorlds(includeDisabled = true)
            .flatMap(planner::planChunks)
            .flatMap { chunk ->
                val snapshot = snapshotter.snapshot(chunk)
                policy.decide(snapshot, rule).denseEntries
            }
            .sortedByDescending { it.amount }
    }

    private fun cleanChunk(chunk: Chunk, rule: ChunkDensityRule): ChunkDensityChunkReport {
        val snapshot = snapshotter.snapshot(chunk)
        if (snapshot.entities.isEmpty()) {
            return ChunkDensityChunkReport(
                cleaned = 0,
                denseEntries = emptyList(),
            )
        }
        val decision = policy.decide(snapshot, rule)
        val report = cleaner.clean(chunk, decision)
        PL.debug { "区块${chunk.x},${chunk.z}密集清理完成(${report.cleaned})" }
        return report
    }
}
