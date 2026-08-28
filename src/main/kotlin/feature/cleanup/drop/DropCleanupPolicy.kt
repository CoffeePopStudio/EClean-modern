package top.e404.eclean.feature.cleanup.drop

import top.e404.eclean.util.filterByMatchers
import java.util.UUID

data class DropCleanupDecision(
    val total: Int,
    val itemIdsToRemove: List<UUID>,
)

class DropCleanupPolicy {
    fun decide(
        collection: DropCleanupCollection,
        rule: DropCleanupRule,
        matchers: List<Regex>,
    ): DropCleanupDecision {
        val candidates = collection.candidates.filterNot { candidate ->
            rule.protectEnchanted && candidate.enchanted ||
                    rule.protectWrittenBook && candidate.writtenBook ||
                    rule.protectLore && candidate.lore
        }
        val grouped = candidates.groupBy(DropCleanupCandidate::type)
        val selected = grouped.filterByMatchers(matchers, rule.blackList)
        return DropCleanupDecision(
            total = candidates.size,
            itemIdsToRemove = selected.values.flatten().map(DropCleanupCandidate::id),
        )
    }
}
