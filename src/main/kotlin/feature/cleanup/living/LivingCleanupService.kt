package top.e404.eclean.feature.cleanup.living

import org.bukkit.Bukkit
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.platform.Schedulers
import top.e404.eclean.platform.dispatch.ChunkTaskCoordinator
import java.util.concurrent.atomic.AtomicInteger

class LivingCleanupService(
    private val planner: LivingCleanupPlanner = LivingCleanupPlanner(),
    private val collector: LivingCleanupCollector = LivingCleanupCollector(),
    private val policy: LivingCleanupPolicy = LivingCleanupPolicy(),
    private val executor: LivingCleanupExecutor = LivingCleanupExecutor(),
    private val coordinator: ChunkTaskCoordinator = ChunkTaskCoordinator(),
) {
    fun cleanAllWorlds(onComplete: (List<LivingCleanupResult>) -> Unit) {
        val worldNames = planner.planWorldNames()
        if (worldNames.isEmpty()) {
            Schedulers.runGlobal { onComplete(emptyList()) }
            return
        }
        val results = mutableListOf<LivingCleanupResult>()
        val pending = AtomicInteger(worldNames.size)
        worldNames.forEach { name ->
            cleanWorld(name) { result ->
                synchronized(results) { results += result }
                if (pending.decrementAndGet() == 0) onComplete(results.toList())
            }
        }
    }

    fun cleanWorld(worldName: String, onComplete: (LivingCleanupResult) -> Unit) {
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            Schedulers.runGlobal { onComplete(LivingCleanupResult(0, 0)) }
            return
        }
        val chunkRefs = coordinator.getLoadedChunkRefs(world)
        if (chunkRefs.isEmpty()) {
            Schedulers.runGlobal { onComplete(LivingCleanupResult(0, 0)) }
            return
        }
        val rule = LivingCleanupRule.fromConfig()
        val matchers = top.e404.eclean.config.Config.current.living.matchers
        val cleaned = AtomicInteger(0)
        val total = AtomicInteger(0)
        val remaining = mutableListOf<LivingCleanupCandidate>()
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
                synchronized(remaining) { remaining += decision.remainingCandidates }
            },
            onComplete = {
                RuntimeServices.messages.debug { "Living cleanup complete in ${worldName} (${cleaned.get()}/${total.get()})" }
                RuntimeServices.messages.buildDebug {
                    append("Living entity stats for ").append(worldName).append(": ")
                    remaining
                        .groupingBy(LivingCleanupCandidate::type)
                        .eachCount()
                        .entries
                        .joinTo(this, ", ") { (type, count) -> "$type: $count" }
                }
                onComplete(LivingCleanupResult(cleaned.get(), total.get()))
            },
        )
    }
}
