package top.e404.eclean.feature.cleanup.living

import org.bukkit.Bukkit
import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.Config
import top.e404.eclean.feature.cleanup.ChunkCleanupOutcome
import top.e404.eclean.feature.cleanup.ChunkCleanupRunner

class LivingCleanupService(
    private val planner: LivingCleanupPlanner = LivingCleanupPlanner(),
    private val collector: LivingCleanupCollector = LivingCleanupCollector(),
    private val policy: LivingCleanupPolicy = LivingCleanupPolicy(),
    private val executor: LivingCleanupExecutor = LivingCleanupExecutor(),
    private val runner: ChunkCleanupRunner = ChunkCleanupRunner(),
) {
    fun cleanAllWorlds(dryRun: Boolean = false, onComplete: (List<LivingCleanupResult>) -> Unit) {
        val worldNames = planner.planWorldNames()
        runner.cleanAllWorlds(worldNames, onComplete) { name, callback ->
            cleanWorld(name, dryRun = dryRun, onComplete = callback)
        }
    }

    fun cleanWorld(worldName: String, dryRun: Boolean = false, onComplete: (LivingCleanupResult) -> Unit) {
        val rule = LivingCleanupRule.fromConfig()
        val matchers = Config.current.living.matchers
        val remaining = mutableListOf<LivingCleanupCandidate>()
        runner.cleanWorld(
            worldName = worldName,
            onEmpty = { onComplete(LivingCleanupResult(0, 0)) },
            perChunk = { w, ref ->
                val chunk = w.getChunkAt(ref.x, ref.z)
                val collection = collector.collectFromChunk(chunk)
                if (collection.candidates.isEmpty()) return@cleanWorld ChunkCleanupOutcome(0, 0)
                val decision = policy.decide(collection, rule, matchers)
                val cleaned = if (!dryRun) executor.execute(collection, decision) else decision.total
                synchronized(remaining) { remaining += decision.remainingCandidates }
                ChunkCleanupOutcome(cleaned, decision.total)
            },
            onComplete = { cleaned, total ->
                RuntimeServices.messages.debug { "Living cleanup complete in ${worldName} (${cleaned}/${total})" }
                RuntimeServices.messages.buildDebug {
                    append("Living entity stats for ").append(worldName).append(": ")
                    remaining
                        .groupingBy(LivingCleanupCandidate::type)
                        .eachCount()
                        .entries
                        .joinTo(this, ", ") { (type, count) -> "$type: $count" }
                }
                onComplete(LivingCleanupResult(cleaned, total))
            },
        )
    }
}
