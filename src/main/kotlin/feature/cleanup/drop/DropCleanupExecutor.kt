package top.e404.eclean.feature.cleanup.drop

import top.e404.eclean.app.RuntimeServices
import top.e404.eclean.config.Config

class DropCleanupExecutor {
    fun execute(
        collection: DropCleanupCollection,
        decision: DropCleanupDecision,
    ): Int {
        if (decision.itemIdsToRemove.isEmpty()) return 0
        val ids = decision.itemIdsToRemove.toHashSet()
        val selected = collection.candidates
            .filter { it.id in ids }
            .mapNotNull { candidate -> candidate.item?.let { item -> candidate to item } }
        val trashCfg = Config.current.trashcan
        if (trashCfg.enabled && trashCfg.collectFromDropCleanup) {
            RuntimeServices.trashcanManager.collectStacks(selected.map { (_, item) -> item.itemStack })
        }
        selected.forEach { (_, item) -> item.remove() }
        return selected.size
    }
}
