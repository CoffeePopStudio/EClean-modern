package top.e404.eclean.feature.cleanup.drop

import java.util.UUID

class DropCleanupExecutor {
    fun execute(
        collection: DropCleanupCollection,
        decision: DropCleanupDecision,
        remover: (List<UUID>) -> Int,
    ): Int {
        if (decision.itemIdsToRemove.isEmpty()) return 0
        val ids = decision.itemIdsToRemove.toHashSet()
        val selectedIds = collection.candidates
            .filter { it.id in ids }
            .map { it.id }
        return remover(selectedIds)
    }
}