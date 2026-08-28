package top.e404.eclean.feature.cleanup.living

import top.e404.eclean.util.filterByMatchers
import java.util.UUID

data class LivingCleanupDecision(
    val total: Int,
    val entityIdsToRemove: List<UUID>,
    val remainingCandidates: List<LivingCleanupCandidate>,
)

class LivingCleanupPolicy {
    fun decide(
        collection: LivingCleanupCollection,
        rule: LivingCleanupRule,
        matchers: List<Regex>,
    ): LivingCleanupDecision {
        val candidates = collection.candidates.filterNot { candidate ->
            !rule.cleanNamed && candidate.named ||
                    !rule.cleanLeashed && candidate.leashed ||
                    !rule.cleanMounted && candidate.mounted
        }
        val grouped = candidates.groupBy(LivingCleanupCandidate::type)
        val selected = grouped.filterByMatchers(matchers, rule.blackList)
        return LivingCleanupDecision(
            total = collection.candidates.size,
            entityIdsToRemove = selected.values.flatten().map(LivingCleanupCandidate::id),
            remainingCandidates = candidates,
        )
    }
}
