package top.e404.eclean.feature.cleanup.drop

import org.bukkit.Bukkit
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.Config
import top.e404.eclean.feature.cleanup.ChunkCleanupOutcome
import top.e404.eclean.feature.cleanup.ChunkCleanupRunner

class DropCleanupService(
    private val planner: DropCleanupPlanner = DropCleanupPlanner(),
    private val collector: DropCleanupCollector = DropCleanupCollector(),
    private val policy: DropCleanupPolicy = DropCleanupPolicy(),
    private val executor: DropCleanupExecutor = DropCleanupExecutor(),
    private val runner: ChunkCleanupRunner = ChunkCleanupRunner(),
) {
    fun cleanAllWorlds(dryRun: Boolean = false, onComplete: (List<DropCleanupResult>) -> Unit) {
        val worldNames = planner.planWorldNames()
        runner.cleanAllWorlds(worldNames, onComplete) { name, callback ->
            cleanWorld(name, dryRun = dryRun, onComplete = callback)
        }
    }

    fun cleanWorld(worldName: String, dryRun: Boolean = false, onComplete: (DropCleanupResult) -> Unit) {
        val rule = DropCleanupRule.fromConfig()
        val matchers = Config.current.drop.matchers
        runner.cleanWorld(
            worldName = worldName,
            onEmpty = { onComplete(DropCleanupResult(0, 0)) },
            perChunk = { w, ref ->
                val chunk = w.getChunkAt(ref.x, ref.z)
                val collection = collector.collectFromChunk(chunk)
                if (collection.candidates.isEmpty()) return@cleanWorld ChunkCleanupOutcome(0, 0)
                val decision = policy.decide(collection, rule, matchers)
                val cleaned = if (!dryRun) executor.execute(collection, decision) else decision.total
                ChunkCleanupOutcome(cleaned, decision.total)
            },
            onComplete = { cleaned, total ->
                RuntimeServices.messages.debug { "Drop cleanup complete in ${worldName} (${cleaned}/${total})" }
                onComplete(DropCleanupResult(cleaned, total))
            },
        )
    }
}
