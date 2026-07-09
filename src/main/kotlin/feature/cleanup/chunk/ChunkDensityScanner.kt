package top.e404.eclean.feature.cleanup.chunk

import org.bukkit.Bukkit
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.Config
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.platform.dispatch.ChunkTaskCoordinator
import java.util.concurrent.atomic.AtomicInteger

class ChunkDensityScanner(
    private val planner: ChunkScanPlanner = ChunkScanPlanner(),
    private val snapshotter: ChunkEntitySnapshotter = ChunkEntitySnapshotter(),
    private val policy: ChunkDensityPolicy = ChunkDensityPolicy(),
    private val cleaner: ChunkDensityCleaner = ChunkDensityCleaner(),
    private val coordinator: ChunkTaskCoordinator = ChunkTaskCoordinator(),
) {
    fun cleanAllWorlds(dryRun: Boolean = false, onComplete: (ChunkDensityResult) -> Unit) {
        val worldNames = planner.planWorldNames()
        val rule = ChunkDensityRule.fromConfig(Config.current.chunkDensity)
        if (worldNames.isEmpty()) {
            onComplete(ChunkDensityResult(0, emptyList()))
            return
        }
        val cleaned = AtomicInteger(0)
        val dense = mutableListOf<ChunkDensityEntry>()
        val pending = AtomicInteger(worldNames.size)
        worldNames.forEach { name ->
            cleanWorld(name, rule, dryRun = dryRun, onWorldComplete = { result ->
                cleaned.addAndGet(result.cleaned)
                synchronized(dense) { dense += result.denseEntries }
                if (pending.decrementAndGet() == 0) {
                    onComplete(ChunkDensityResult(cleaned.get(), dense.toList()))
                }
            })
        }
    }

    fun cleanWorld(
        worldName: String,
        rule: ChunkDensityRule = ChunkDensityRule.fromConfig(Config.current.chunkDensity),
        dryRun: Boolean = false,
        onWorldComplete: (ChunkDensityResult) -> Unit = {},
    ) {
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            onWorldComplete(ChunkDensityResult(0, emptyList()))
            return
        }
        val chunkRefs = planner.planChunks(world)
        if (chunkRefs.isEmpty()) {
            onWorldComplete(ChunkDensityResult(0, emptyList()))
            return
        }
        val cleaned = AtomicInteger(0)
        val dense = mutableListOf<ChunkDensityEntry>()
        coordinator.dispatchToChunks(
            chunkRefs = chunkRefs,
            resolveWorld = { Bukkit.getWorld(it) },
            perChunk = { w, ref ->
                val chunk = w.getChunkAt(ref.x, ref.z)
                val report = cleanChunk(chunk, rule, dryRun)
                cleaned.addAndGet(report.cleaned)
                synchronized(dense) { dense += report.denseEntries }
            },
            onComplete = {
                RuntimeServices.messages.debug { "Dense entity cleanup complete in ${worldName} (${cleaned.get()} removed)" }
                onWorldComplete(ChunkDensityResult(cleaned.get(), dense.toList()))
            },
        )
    }

    fun scanDenseEntries(onComplete: (List<ChunkDensityEntry>) -> Unit) {
        val worldNames = planner.planWorldNames(includeDisabled = true)
        val rule = ChunkDensityRule.fromConfig(Config.current.chunkDensity)
        if (worldNames.isEmpty()) {
            onComplete(emptyList())
            return
        }
        val dense = mutableListOf<ChunkDensityEntry>()
        val pending = AtomicInteger(worldNames.size)
        worldNames.forEach { name ->
            val world = Bukkit.getWorld(name)
            if (world == null) {
                if (pending.decrementAndGet() == 0) onComplete(dense.sortedByDescending { it.amount })
                return@forEach
            }
            val chunkRefs = planner.planChunks(world)
            coordinator.dispatchToChunks(
                chunkRefs = chunkRefs,
                resolveWorld = { Bukkit.getWorld(it) },
                perChunk = { w, ref ->
                    val chunk = w.getChunkAt(ref.x, ref.z)
                    val snapshot = snapshotter.snapshot(chunk)
                    if (snapshot.entities.isEmpty()) return@dispatchToChunks
                    val decision = policy.decide(snapshot, rule)
                    synchronized(dense) { dense += decision.denseEntries }
                },
                onComplete = {
                    if (pending.decrementAndGet() == 0) onComplete(dense.sortedByDescending { it.amount })
                },
            )
        }
    }

    private fun cleanChunk(chunk: org.bukkit.Chunk, rule: ChunkDensityRule, dryRun: Boolean = false): ChunkDensityChunkReport {
        val snapshot = snapshotter.snapshot(chunk)
        if (snapshot.entities.isEmpty()) {
            return ChunkDensityChunkReport(cleaned = 0, denseEntries = emptyList())
        }
        val decision = policy.decide(snapshot, rule)
        if (!dryRun) {
            val report = cleaner.clean(chunk, decision)
            RuntimeServices.messages.debug { "Dense cleanup complete in chunk ${chunk.x},${chunk.z} (${report.cleaned} removed)" }
            return report
        } else {
            val wouldClean = decision.denseEntries.sumOf { it.amount }
            RuntimeServices.messages.debug { "Dry-run dense cleanup in chunk ${chunk.x},${chunk.z} (${wouldClean} would be removed)" }
            return ChunkDensityChunkReport(cleaned = wouldClean, denseEntries = decision.denseEntries)
        }
    }
}
