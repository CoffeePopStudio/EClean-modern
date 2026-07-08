package top.e404.eclean.feature.cleanup.living

import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.UUID

data class LivingCleanupCandidate(
    val id: UUID,
    val type: String,
    val named: Boolean,
    val leashed: Boolean,
    val mounted: Boolean,
    val entity: LivingEntity? = null,
)

data class LivingCleanupCollection(
    val candidates: List<LivingCleanupCandidate>,
)

class LivingCleanupCollector {
    fun collect(world: World): LivingCleanupCollection = LivingCleanupCollection(
        candidates = world.livingEntities
            .filterNot { it is Player }
            .map { entity ->
                LivingCleanupCandidate(
                    id = entity.uniqueId,
                    type = entity.type.name,
                    named = entity.customName != null,
                    leashed = entity.isLeashed,
                    mounted = entity.isInsideVehicle || entity.passengers.isNotEmpty(),
                    entity = entity,
                )
            }
    )
}
