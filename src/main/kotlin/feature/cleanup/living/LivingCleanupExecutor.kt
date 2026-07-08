package top.e404.eclean.feature.cleanup.living

class LivingCleanupExecutor {
    fun execute(
        collection: LivingCleanupCollection,
        decision: LivingCleanupDecision,
    ): Int {
        if (decision.entityIdsToRemove.isEmpty()) return 0
        val ids = decision.entityIdsToRemove.toHashSet()
        val selected = collection.candidates
            .filter { it.id in ids }
            .mapNotNull { candidate -> candidate.entity?.let { entity -> candidate to entity } }
        selected.forEach { (_, entity) -> entity.remove() }
        return selected.size
    }
}
