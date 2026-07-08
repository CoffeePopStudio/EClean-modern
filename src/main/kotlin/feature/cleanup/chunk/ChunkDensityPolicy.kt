package top.e404.eclean.feature.cleanup.chunk

import org.bukkit.entity.EntityType
import top.e404.eclean.platform.snapshot.ChunkEntitySnapshot
import top.e404.eclean.platform.snapshot.ChunkEntityState
import java.util.UUID

class ChunkDensityPolicy {
    fun decide(
        snapshot: ChunkEntitySnapshot,
        rule: ChunkDensityRule,
    ): ChunkDensityDecision {
        val candidates = snapshot.entities.toMutableList()
        if (!rule.cleanNamed) candidates.removeIf(ChunkEntityState::named)
        if (!rule.cleanLeashed) candidates.removeIf(ChunkEntityState::leashed)
        if (!rule.cleanMounted) candidates.removeIf(ChunkEntityState::mounted)

        val entityIdsToRemove = mutableListOf<UUID>()
        rule.entityLimits.forEach { (regex, limit) ->
            val matches = candidates.filter { it.type.matches(regex) }.toMutableList()
            if (matches.size <= limit) return@forEach
            val overflow = matches.subList(limit, matches.size).toList()
            entityIdsToRemove += overflow.map(ChunkEntityState::uuid)
            candidates.removeAll(overflow)
        }

        val denseEntries = snapshot.counts.entries
            .asSequence()
            .filter { (_, amount) -> amount > rule.alertThreshold }
            .mapNotNull { (name, amount) ->
                val type = runCatching { EntityType.valueOf(name) }.getOrNull() ?: return@mapNotNull null
                ChunkDensityEntry(snapshot.chunk, type, amount)
            }
            .sortedByDescending(ChunkDensityEntry::amount)
            .toList()

        return ChunkDensityDecision(
            chunk = snapshot.chunk,
            entityIdsToRemove = entityIdsToRemove,
            denseEntries = denseEntries,
        )
    }
}
