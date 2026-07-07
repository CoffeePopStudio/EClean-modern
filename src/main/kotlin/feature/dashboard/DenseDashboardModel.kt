package top.e404.eclean.feature.dashboard

import org.bukkit.Chunk
import org.bukkit.entity.EntityType

data class DenseDashboardModel(
    val chunk: Chunk,
    val entityType: EntityType,
    val amount: Int,
)
