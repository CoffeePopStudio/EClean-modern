package top.e404.eclean.feature.cleanup.living

import org.bukkit.World
import top.e404.eclean.PL

class LivingCleanupService(
    private val planner: LivingCleanupPlanner = LivingCleanupPlanner(),
    private val collector: LivingCleanupCollector = LivingCleanupCollector(),
    private val policy: LivingCleanupPolicy = LivingCleanupPolicy(),
    private val executor: LivingCleanupExecutor = LivingCleanupExecutor(),
) {
    fun cleanAllWorlds(): List<LivingCleanupResult> {
        return planner.planWorlds().map(::cleanWorld)
    }

    fun cleanWorld(world: World): LivingCleanupResult {
        val collection = collector.collect(world)
        val decision = policy.decide(
            collection = collection,
            rule = LivingCleanupRule.fromConfig(),
            matchers = top.e404.eclean.config.Config.current.living.matchers,
        )
        val report = LivingCleanupReport(
            cleaned = executor.execute(collection, decision),
            total = decision.total,
            remainingCandidates = decision.remainingCandidates,
        )
        PL.debug { "世界${world.name}生物清理完成(${report.cleaned}/${report.total})" }
        PL.buildDebug {
            append("世界").append(world.name).append("生物统计: ")
            report.remainingCandidates
                .groupingBy(LivingCleanupCandidate::type)
                .eachCount()
                .entries
                .joinTo(this, ", ") { (type, count) -> "$type: $count" }
        }
        return report.toResult()
    }
}
