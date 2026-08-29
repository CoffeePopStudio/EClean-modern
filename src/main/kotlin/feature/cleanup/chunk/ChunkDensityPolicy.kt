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

        val byType = candidates.groupBy(ChunkEntityState::type).mapValues { it.value.toMutableList() }
        val orderIndex = HashMap<UUID, Int>().apply {
            candidates.forEachIndexed { index, state -> put(state.uuid, index) }
        }

        val entityIdsToRemove = mutableListOf<UUID>()
        rule.entityLimits.forEach { (regex, limit) ->
            val matches = byType
                .filterKeys { type -> type.matches(regex) }
                .values
                .flatten()
                .sortedBy { orderIndex.getValue(it.uuid) }
                .toMutableList()
            if (matches.size <= limit) return@forEach
            val overflow = matches.subList(limit, matches.size).toList()
            entityIdsToRemove += overflow.map(ChunkEntityState::uuid)
            candidates.removeAll(overflow)
            overflow.groupBy(ChunkEntityState::type).forEach { (type, removed) ->
                byType[type]?.removeAll(removed)
            }
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
