package top.e404.eclean.feature.stats

import org.bukkit.entity.EntityType

data class WorldStatsResult(
    val entityCounts: Map<EntityType, Int>,
    val loadedChunks: Int,
    val forceLoadedChunks: Int,
) {
    val totalEntities: Int get() = entityCounts.values.sum()

    fun sortedEntries(): List<Pair<EntityType, Int>> =
        entityCounts.entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }
}
