package top.e404.eclean.feature.cleanup.drop

import org.bukkit.Bukkit
import top.e404.eclean.PL
import top.e404.eclean.platform.dispatch.ChunkTaskCoordinator
import top.e404.eclean.platform.SchedulerFacade
import java.util.concurrent.atomic.AtomicInteger

class DropCleanupService(
    private val scheduler: SchedulerFacade,
    private val planner: DropCleanupPlanner = DropCleanupPlanner(),
    private val collector: DropCleanupCollector = DropCleanupCollector(),
    private val policy: DropCleanupPolicy = DropCleanupPolicy(),
    private val executor: DropCleanupExecutor = DropCleanupExecutor(),
    private val coordinator: ChunkTaskCoordinator = ChunkTaskCoordinator(scheduler),
) {
    fun cleanAllWorlds(onComplete: (List<DropCleanupResult>) -> Unit) {
        val worldNames = planner.planWorldNames()
        if (worldNames.isEmpty()) {
            scheduler.runGlobal { onComplete(emptyList()) }
            return
        }
        val results = mutableListOf<DropCleanupResult>()
        val pending = AtomicInteger(worldNames.size)
        worldNames.forEach { name ->
            cleanWorld(name) { result ->
                synchronized(results) { results += result }
                if (pending.decrementAndGet() == 0) onComplete(results.toList())
            }
        }
    }

    fun cleanWorld(worldName: String, onComplete: (DropCleanupResult) -> Unit) {
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            scheduler.runGlobal { onComplete(DropCleanupResult(0, 0)) }
            return
        }
        val chunkRefs = coordinator.getLoadedChunkRefs(world)
        if (chunkRefs.isEmpty()) {
            scheduler.runGlobal { onComplete(DropCleanupResult(0, 0)) }
            return
        }
        val rule = DropCleanupRule.fromConfig()
        val matchers = top.e404.eclean.config.Config.current.drop.matchers
        val cleaned = AtomicInteger(0)
        val total = AtomicInteger(0)
        coordinator.dispatchToChunks(
            chunkRefs = chunkRefs,
            resolveWorld = { Bukkit.getWorld(it) },
            perChunk = { w, ref ->
                val chunk = w.getChunkAt(ref.x, ref.z)
                val collection = collector.collectFromChunk(chunk)
                if (collection.candidates.isEmpty()) return@dispatchToChunks
                val decision = policy.decide(collection, rule, matchers)
                cleaned.addAndGet(executor.execute(collection, decision))
                total.addAndGet(decision.total)
            },
            onComplete = {
                PL.debug { "世界${worldName}掉落物清理完成(${cleaned.get()}/${total.get()})" }
                onComplete(DropCleanupResult(cleaned.get(), total.get()))
            },
        )
    }
}
