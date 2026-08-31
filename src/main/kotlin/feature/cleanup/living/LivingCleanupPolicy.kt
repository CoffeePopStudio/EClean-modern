package top.e404.eclean.feature.cleanup.living

import org.bukkit.entity.EntityType
import org.bukkit.entity.Tameable
import top.e404.eclean.util.distanceToNearestPlayer
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
        val candidates = collection.candidates
            .filterNot { candidate ->
                !rule.cleanNamed && candidate.named ||
                        !rule.cleanLeashed && candidate.leashed ||
                        !rule.cleanMounted && candidate.mounted
            }
            .filter { candidate -> passesTypeAndDistanceRule(candidate, rule) }
        val grouped = candidates.groupBy(LivingCleanupCandidate::type)
        val selected = grouped.filterByMatchers(matchers, rule.blackList)
        return LivingCleanupDecision(
            total = collection.candidates.size,
            entityIdsToRemove = selected.values.flatten().map(LivingCleanupCandidate::id),
            remainingCandidates = candidates,
        )
    }

    private fun passesTypeAndDistanceRule(candidate: LivingCleanupCandidate, rule: LivingCleanupRule): Boolean {
        val entity = candidate.entity
        if (entity == null) return true
        if (rule.protectTamed && entity is Tameable && entity.isTamed) return false
        if (rule.protectAllay && entity.type == EntityType.ALLAY) return false
        val typeRule = rule.typeRules[candidate.type]
        if (typeRule?.enabled == false) return false
        val maxDistance = typeRule?.maxDistance ?: rule.maxDistance ?: return true
        return entity.location.distanceToNearestPlayer() > maxDistance
    }
}
