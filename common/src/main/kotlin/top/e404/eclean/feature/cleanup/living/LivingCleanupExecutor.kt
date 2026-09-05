package top.e404.eclean.feature.cleanup.living

import java.util.UUID

class LivingCleanupExecutor {
    fun execute(
        collection: LivingCleanupCollection,
        decision: LivingCleanupDecision,
        remover: (List<UUID>) -> Int,
    ): Int {
        if (decision.entityIdsToRemove.isEmpty()) return 0
        val ids = decision.entityIdsToRemove.toHashSet()
        val selectedIds = collection.candidates
            .filter { it.id in ids }
            .map { it.id }
        return remover(selectedIds)
    }
}