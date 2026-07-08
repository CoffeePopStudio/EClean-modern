package top.e404.eclean.feature.cleanup.drop

import org.bukkit.World
import top.e404.eclean.PL

class DropCleanupService(
    private val planner: DropCleanupPlanner = DropCleanupPlanner(),
    private val collector: DropCleanupCollector = DropCleanupCollector(),
    private val policy: DropCleanupPolicy = DropCleanupPolicy(),
    private val executor: DropCleanupExecutor = DropCleanupExecutor(),
) {
    fun cleanAllWorlds(): List<DropCleanupResult> {
        return planner.planWorlds().map(::cleanWorld)
    }

    fun cleanWorld(world: World): DropCleanupResult {
        val collection = collector.collect(world)
        val decision = policy.decide(
            collection = collection,
            rule = DropCleanupRule.fromConfig(),
            matchers = top.e404.eclean.config.Config.current.drop.matchers,
        )
        val report = DropCleanupReport(
            cleaned = executor.execute(collection, decision),
            total = decision.total,
        )
        PL.debug { "世界${world.name}掉落物清理完成(${report.cleaned}/${report.total})" }
        return report.toResult()
    }
}
